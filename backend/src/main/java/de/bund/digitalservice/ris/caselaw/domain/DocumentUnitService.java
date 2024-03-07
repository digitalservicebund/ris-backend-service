package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@Slf4j
public class DocumentUnitService {

  private final DocumentUnitRepository repository;
  private final PublicationReportRepository publicationReportRepository;
  private final DocumentNumberService documentNumberService;
  private final S3AsyncClient s3AsyncClient;
  private final EmailPublishService publicationService;
  private final DocumentUnitStatusService documentUnitStatusService;

  private final DocumentNumberRecyclingService documentNumberRecyclingService;
  private final Validator validator;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  @Value("${mail.exporter.recipientAddress:neuris@example.com}")
  private String recipientAddress;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentNumberService documentNumberService,
      S3AsyncClient s3AsyncClient,
      EmailPublishService publicationService,
      DocumentUnitStatusService documentUnitStatusService,
      PublicationReportRepository publicationReportRepository,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      Validator validator) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.s3AsyncClient = s3AsyncClient;
    this.publicationService = publicationService;
    this.documentUnitStatusService = documentUnitStatusService;
    this.publicationReportRepository = publicationReportRepository;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
    this.validator = validator;
  }

  public Mono<DocumentUnit> generateNewDocumentUnit(DocumentationOffice documentationOffice) {
    return Mono.just(documentationOffice)
        .flatMap(this::generateDocumentNumber)
        .flatMap(
            documentNumber -> repository.createNewDocumentUnit(documentNumber, documentationOffice))
        .flatMap(documentUnitStatusService::setInitialStatus)
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).jitter(0.75))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex));
  }

  private Mono<String> generateDocumentNumber(DocumentationOffice documentationOffice) {
    try {
      return Mono.just(
          documentNumberService.generateDocumentNumber(documentationOffice.abbreviation(), 5));
    } catch (Exception e) {
      throw new DocumentationUnitException("Could not generate document number", e);
    }
  }

  public Mono<DocumentUnit> attachFileToDocumentUnit(
      UUID documentUnitUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {
    var fileUuid = UUID.randomUUID().toString();
    String fileName =
        httpHeaders.containsKey("X-Filename")
            ? httpHeaders.getFirst("X-Filename")
            : "Kein Dateiname gefunden";

    checkDocx(byteBuffer);

    return putObjectIntoBucket(fileUuid, byteBuffer, httpHeaders)
        .doOnNext(putObjectResponse -> log.debug("generate doc unit for {}", fileUuid))
        .map(putObjectResponse -> repository.findByUuid(documentUnitUuid))
        .doOnNext(
            documentUnit ->
                log.debug(
                    "attach file '{}' to documentUnit: {}",
                    fileName,
                    documentUnit.documentNumber()))
        .flatMap(
            documentUnit -> repository.attachFile(documentUnitUuid, fileUuid, "docx", fileName))
        .doOnError(ex -> log.error("Couldn't upload the file to bucket", ex));
  }

  public Mono<DocumentUnit> removeFileFromDocumentUnit(UUID documentUnitId) {
    DocumentUnit documentUnit = repository.findByUuid(documentUnitId);

    return deleteObjectFromBucket(documentUnit.s3path())
        .map(
            response -> {
              log.debug("deleted file {} in bucket", documentUnit.s3path());
              return repository.removeFile(documentUnitId);
            })
        .doOnError(ex -> log.error("Couldn't remove the file from the DocumentUnit", ex));
  }

  void checkDocx(ByteBuffer byteBuffer) {
    var zip = new ZipInputStream(new ByteArrayInputStream(byteBufferToArray(byteBuffer)));
    ZipEntry entry;
    try {
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.getName().startsWith("word/document") && entry.getName().endsWith(".xml")) {
          return;
        }
      }
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  private Mono<PutObjectResponse> putObjectIntoBucket(
      String fileUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {

    var contentLength = httpHeaders.getContentLength();

    Map<String, String> metadata = new HashMap<>();
    MediaType mediaType = httpHeaders.getContentType();
    if (mediaType == null) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    log.debug("upload header information: mediaType{}, contentLength={}", mediaType, contentLength);

    var asyncRequestBody = AsyncRequestBody.fromPublisher(Mono.just(byteBuffer));
    var putObjectRequestBuilder =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileUuid)
            .contentType(mediaType.toString())
            .metadata(metadata);

    if (contentLength >= 0) {
      putObjectRequestBuilder.contentLength(contentLength);
    }

    var putObjectRequest = putObjectRequestBuilder.build();

    return Mono.fromCallable(
            () -> Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, asyncRequestBody)))
        .flatMap(Function.identity());
  }

  private Mono<DeleteObjectResponse> deleteObjectFromBucket(String fileUuid) {
    if (fileUuid == null) {
      return Mono.empty();
    }

    var deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(fileUuid).build();
    return Mono.fromCallable(() -> Mono.fromFuture(s3AsyncClient.deleteObject(deleteObjectRequest)))
        .flatMap(Function.identity());
  }

  public Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      Optional<String> documentNumber,
      Optional<String> fileNumber,
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<LocalDate> decisionDate,
      Optional<LocalDate> decisionDateEnd,
      Optional<String> publicationStatus,
      Optional<Boolean> withError,
      Optional<Boolean> myDocOfficeOnly) {

    DocumentationUnitSearchInput searchInput =
        DocumentationUnitSearchInput.builder()
            .documentNumber(documentNumber.orElse(null))
            .fileNumber(fileNumber.orElse(null))
            .courtType(courtType.orElse(null))
            .courtLocation(courtLocation.orElse(null))
            .decisionDate(decisionDate.orElse(null))
            .decisionDateEnd(decisionDateEnd.orElse(null))
            .status(
                (publicationStatus.isPresent() || withError.isPresent())
                    ? Status.builder()
                        .publicationStatus(
                            publicationStatus.map(PublicationStatus::valueOf).orElse(null))
                        .withError(withError.orElse(false))
                        .build()
                    : null)
            .myDocOfficeOnly(myDocOfficeOnly.orElse(false))
            .build();

    return repository.searchByDocumentationUnitSearchInput(
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
        documentationOffice,
        searchInput);
  }

  public Mono<DocumentUnit> getByDocumentNumber(String documentNumber) {
    return repository.findByDocumentNumber(documentNumber);
  }

  public Mono<DocumentUnit> getByUuid(UUID documentUnitUuid) {
    return Mono.just(repository.findByUuid(documentUnitUuid));
  }

  public Mono<String> deleteByUuid(UUID documentUnitUuid) {
    Map<RelatedDocumentationType, Long> relatedEntities =
        repository.getAllDocumentationUnitWhichLink(documentUnitUuid);

    if (!(relatedEntities == null
        || relatedEntities.isEmpty()
        || relatedEntities.values().stream().mapToLong(Long::longValue).sum() == 0)) {

      log.debug(
          "Could not delete document unit {} cause of related entities: {}",
          documentUnitUuid,
          relatedEntities);

      return Mono.error(
          new DocumentUnitDeletionException(
              "Die Dokumentationseinheit konnte nicht gelöscht werden, da", relatedEntities));
    }

    DocumentUnit documentUnit = repository.findByUuid(documentUnitUuid);

    log.debug("Deleting DocumentUnitDTO " + documentUnitUuid);

    return deleteObjectFromBucket(documentUnit.s3path())
        .doOnNext(
            deleteObjectResponse -> log.debug("Deleted file {} in bucket", documentUnit.s3path()))
        .doOnError(
            exception ->
                log.error("Error by deleting file {} in bucket", documentUnit.s3path(), exception))
        .defaultIfEmpty(DeleteObjectResponse.builder().build())
        .map(
            deleteObjectResponse -> {
              saveForRecycling(documentUnit);
              repository.delete(documentUnit);
              return "Dokumentationseinheit gelöscht: " + documentUnitUuid;
            });
  }

  public Mono<DocumentUnit> updateDocumentUnit(DocumentUnit documentUnit) {
    repository.saveKeywords(documentUnit);
    repository.saveFieldsOfLaw(documentUnit);
    repository.saveProcedures(documentUnit);

    repository.save(documentUnit);

    return Mono.just(repository.findByUuid(documentUnit.uuid()));
  }

  public Mono<Publication> publishAsEmail(UUID documentUnitUuid, String issuerAddress) {
    DocumentUnit documentUnit = repository.findByUuid(documentUnitUuid);

    if (documentUnit == null) {
      return Mono.empty();
    }

    return publicationService
        .publish(documentUnit, recipientAddress)
        .flatMap(
            mailResponse -> {
              if (mailResponse.getStatusCode().equals(String.valueOf(HttpStatus.OK.value()))) {
                return documentUnitStatusService
                    .setToPublishing(documentUnit, mailResponse.getPublishDate(), issuerAddress)
                    .thenReturn(mailResponse);
              } else {
                return Mono.just(mailResponse);
              }
            });
  }

  public Mono<XmlResultObject> previewPublication(UUID documentUuid) {
    DocumentUnit documentUnit = repository.findByUuid(documentUuid);
    return publicationService.getPublicationPreview(documentUnit);
  }

  public Flux<PublicationHistoryRecord> getPublicationHistory(UUID documentUuid) {
    return Flux.concat(
            publicationService.getPublications(documentUuid),
            Flux.fromIterable(publicationReportRepository.getAllByDocumentUnitUuid(documentUuid)))
        .sort(Comparator.comparing(PublicationHistoryRecord::getDate).reversed());
  }

  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {

    return repository.searchLinkableDocumentationUnits(
        relatedDocumentationUnit, documentationOffice, documentNumberToExclude, pageable);
  }

  public Mono<String> validateSingleNorm(SingleNormValidationInfo singleNormValidationInfo) {
    Set<ConstraintViolation<SingleNormValidationInfo>> violations =
        validator.validate(singleNormValidationInfo);

    if (violations.isEmpty()) {
      return Mono.just("Ok");
    }
    return Mono.just("Validation error");
  }

  public void updateECLI(UUID uuid, Docx2Html docx2html) {
    if (docx2html.ecliList().size() == 1) {
      repository.updateECLI(uuid, docx2html.ecliList().get(0));
    }
  }

  private byte[] byteBufferToArray(ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    byte[] byteBufferArray = new byte[byteBuffer.remaining()];
    byteBuffer.get(byteBufferArray);
    byteBuffer.rewind();
    return byteBufferArray;
  }

  private void saveForRecycling(DocumentUnit documentUnit) {
    try {
      documentNumberRecyclingService.addForRecycling(
          documentUnit.uuid(),
          documentUnit.documentNumber(),
          documentUnit.coreData().documentationOffice().abbreviation());

    } catch (Exception e) {
      log.info("Did not save for recycling", e);
    }
  }
}
