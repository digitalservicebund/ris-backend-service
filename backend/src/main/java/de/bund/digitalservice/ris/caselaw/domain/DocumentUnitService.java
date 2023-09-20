package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
      Validator validator) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.s3AsyncClient = s3AsyncClient;
    this.publicationService = publicationService;
    this.documentUnitStatusService = documentUnitStatusService;
    this.publicationReportRepository = publicationReportRepository;
    this.validator = validator;
  }

  public Mono<DocumentUnit> generateNewDocumentUnit(DocumentationOffice documentationOffice) {
    return Mono.just(documentationOffice)
        .flatMap(documentNumberService::generateNextDocumentNumber)
        .flatMap(
            documentNumber -> repository.createNewDocumentUnit(documentNumber, documentationOffice))
        .flatMap(documentUnitStatusService::setInitialStatus)
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).jitter(0.75))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex));
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
        .flatMap(putObjectResponse -> repository.findByUuid(documentUnitUuid))
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
    return repository
        .findByUuid(documentUnitId)
        .flatMap(
            documentUnit -> {
              var fileUuid = documentUnit.s3path();
              return deleteObjectFromBucket(fileUuid)
                  .doOnNext(
                      deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid));
            })
        .doOnNext(response -> log.debug("removed file from DocumentUnitDTO {}", documentUnitId))
        .flatMap(response -> repository.removeFile(documentUnitId))
        .doOnError(ex -> log.error("Couldn't remove the file from the DocumentUnit", ex));
  }

  void checkDocx(ByteBuffer byteBuffer) {
    var zip =
        new ZipInputStream(new ByteArrayInputStream(ServiceUtils.byteBufferToArray(byteBuffer)));
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
    var deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(fileUuid).build();
    return Mono.fromCallable(() -> Mono.fromFuture(s3AsyncClient.deleteObject(deleteObjectRequest)))
        .flatMap(Function.identity());
  }

  public Page<DocumentationUnitSearchEntry> searchByDocumentUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      Optional<String> documentNumberOrFileNumber,
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<String> decisionDate,
      Optional<String> decisionDateEnd,
      Optional<String> publicationStatus,
      Optional<Boolean> withError,
      Optional<Boolean> myDocOfficeOnly) {

    DocumentUnitSearchInput searchInput =
        DocumentUnitSearchInput.builder()
            .documentNumberOrFileNumber(documentNumberOrFileNumber.orElse(null))
            .courtType(courtType.orElse(null))
            .courtLocation(courtLocation.orElse(null))
            .decisionDate(decisionDate.map(Instant::parse).orElse(null))
            .decisionDateEnd(decisionDateEnd.map(Instant::parse).orElse(null))
            .status(
                (publicationStatus.isPresent() || withError.isPresent())
                    ? DocumentUnitStatus.builder()
                        .publicationStatus(
                            publicationStatus.map(PublicationStatus::valueOf).orElse(null))
                        .withError(withError.orElse(false))
                        .build()
                    : null)
            .myDocOfficeOnly(myDocOfficeOnly.orElse(false))
            .build();

    return repository.searchByDocumentUnitSearchInput(
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
        documentationOffice,
        searchInput);
  }

  public Mono<DocumentUnit> getByDocumentNumber(String documentNumber) {
    return repository.findByDocumentNumber(documentNumber);
  }

  public Mono<DocumentUnit> getByUuid(UUID documentUnitUuid) {
    return repository.findByUuid(documentUnitUuid);
  }

  public Mono<String> deleteByUuid(UUID documentUnitUuid) {
    AtomicInteger documentUnitsThisOneIsAChildOf = new AtomicInteger();

    return repository
        .countLinksByChildDocumentUnitUuid(documentUnitUuid)
        .flatMap(
            count -> {
              documentUnitsThisOneIsAChildOf.set(count.intValue());
              return repository.findByUuid(documentUnitUuid);
            })
        .flatMap(
            documentUnit -> {
              log.debug("Deleting DocumentUnitDTO " + documentUnitUuid);

              Mono<Void> deleteAttachedFile =
                  documentUnit.s3path() == null
                      ? Mono.empty()
                      : deleteObjectFromBucket(documentUnit.s3path())
                          .doOnNext(
                              d -> log.debug("Deleted file {} in bucket", documentUnit.s3path()))
                          .flatMap(d -> Mono.empty());

              Flux<ProceedingDecision> proceedingDecisions =
                  documentUnit.proceedingDecisions() == null
                      ? Flux.empty()
                      : Flux.fromIterable(documentUnit.proceedingDecisions());

              String logMsg =
                  "Dokumentationseinheit gelöscht: "
                      + documentUnitUuid
                      + (documentUnit.proceedingDecisions() == null
                              || documentUnit.proceedingDecisions().isEmpty()
                          ? ""
                          : ", zudem die Verknüpfungen mit "
                              + documentUnit.proceedingDecisions().size()
                              + " vorgehenden Entscheidungen");

              return deleteAttachedFile
                  .thenMany(
                      proceedingDecisions.flatMap(
                          proceedingDecision ->
                              removeLinkedDocumentationUnit(
                                  documentUnitUuid,
                                  proceedingDecision.getUuid(),
                                  DocumentationUnitLinkType.PREVIOUS_DECISION)))
                  .then(repository.delete(documentUnit))
                  .thenReturn(logMsg);
            })
        .onErrorResume(
            ex -> {
              log.error("Couldn't delete the DocumentUnit", ex);
              if (ex instanceof DataIntegrityViolationException) {
                return Mono.error(
                    new DocumentUnitDeletionException(
                        "die Dokumentationseinheit konnte nicht gelöscht werden, "
                            + "da sie eine vorgehende Entscheidung für "
                            + documentUnitsThisOneIsAChildOf.get()
                            + " andere Dokumentationseinheiten darstellt"));
              }
              return Mono.error(
                  new DocumentUnitDeletionException("Couldn't delete the DocumentUnit"));
            });
  }

  public Mono<DocumentUnit> updateDocumentUnit(
      DocumentUnit documentUnit, DocumentationOffice documentationOffice) {
    Mono<DocumentUnit> documentUnitMono = Mono.just(documentUnit);

    return documentUnitMono
        .flatMap(
            documentUnit1 -> {
              if (documentUnit1.proceedingDecisions() == null) return Mono.just(documentUnit1);
              return Flux.fromIterable(documentUnit1.proceedingDecisions())
                  .flatMap(
                      proceedingDecision -> {
                        if (proceedingDecision.getUuid() == null && !proceedingDecision.isEmpty()) {
                          return createLinkedDocumentationUnit(
                                  documentUnit1.uuid(),
                                  proceedingDecision,
                                  documentationOffice,
                                  DocumentationUnitLinkType.PREVIOUS_DECISION)
                              .map(
                                  documentationUnitLink ->
                                      proceedingDecision.toBuilder()
                                          .uuid(documentationUnitLink.childDocumentationUnitUuid())
                                          .build());
                        } else {
                          return linkLinkedDocumentationUnit(
                                  documentUnit1.uuid(),
                                  proceedingDecision.getUuid(),
                                  DocumentationUnitLinkType.PREVIOUS_DECISION)
                              .thenReturn(proceedingDecision);
                        }
                      })
                  .collectList()
                  .map(
                      proceedingDecisions ->
                          documentUnit1.toBuilder()
                              .proceedingDecisions(proceedingDecisions)
                              .build());
            })
        .flatMap(
            documentUnit1 -> {
              if (documentUnit1.contentRelatedIndexing() == null
                  || documentUnit1.contentRelatedIndexing().activeCitations() == null)
                return Mono.just(documentUnit1);
              return Flux.fromIterable(documentUnit1.contentRelatedIndexing().activeCitations())
                  .flatMap(
                      activeCitation -> {
                        if (activeCitation.getUuid() == null && !activeCitation.isEmpty()) {
                          return createLinkedDocumentationUnit(
                                  documentUnit1.uuid(),
                                  activeCitation,
                                  documentationOffice,
                                  DocumentationUnitLinkType.ACTIVE_CITATION)
                              .map(
                                  documentationUnitLink ->
                                      activeCitation.toBuilder()
                                          .uuid(documentationUnitLink.childDocumentationUnitUuid())
                                          .build());
                        } else {
                          return linkLinkedDocumentationUnit(
                                  documentUnit1.uuid(),
                                  activeCitation.getUuid(),
                                  DocumentationUnitLinkType.ACTIVE_CITATION)
                              .thenReturn(activeCitation);
                        }
                      })
                  .collectList()
                  .map(
                      activeCitations -> {
                        ContentRelatedIndexing contentRelatedIndexing =
                            documentUnit1.contentRelatedIndexing().toBuilder()
                                .activeCitations(activeCitations)
                                .build();
                        return documentUnit1.toBuilder()
                            .contentRelatedIndexing(contentRelatedIndexing)
                            .build();
                      });
            })
        .flatMap(repository::save)
        .doOnError(ex -> log.error("Couldn't update the DocumentUnit", ex));
  }

  public Mono<Publication> publishAsEmail(UUID documentUnitUuid, String issuerAddress) {
    return repository
        .findByUuid(documentUnitUuid)
        .flatMap(
            documentUnit ->
                publicationService
                    .publish(documentUnit, recipientAddress)
                    .flatMap(
                        mailResponse -> {
                          if (mailResponse
                              .getStatusCode()
                              .equals(String.valueOf(HttpStatus.OK.value()))) {
                            return documentUnitStatusService
                                .setToPublishing(
                                    documentUnit, mailResponse.getPublishDate(), issuerAddress)
                                .thenReturn(mailResponse);
                          } else {
                            return Mono.just(mailResponse);
                          }
                        }));
  }

  public Flux<PublicationHistoryRecord> getPublicationHistory(UUID documentUuid) {
    return Flux.concat(
            publicationService.getPublications(documentUuid),
            publicationReportRepository.getAllByDocumentUnitUuid(documentUuid))
        .sort(Comparator.comparing(PublicationHistoryRecord::getDate).reversed());
  }

  public <T extends LinkedDocumentationUnit> Mono<Page<T>> searchByLinkedDocumentationUnit(
      T linkedDocumentationUnit, Pageable pageable) {

    return repository
        .searchByLinkedDocumentationUnit(linkedDocumentationUnit, pageable)
        .collectList()
        .zipWith(repository.countSearchByLinkedDocumentationUnit(linkedDocumentationUnit))
        .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
  }

  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public <T extends LinkedDocumentationUnit>
      Mono<DocumentationUnitLink> createLinkedDocumentationUnit(
          UUID parentDocumentUnitUuid,
          T linkedDocumentationUnit,
          DocumentationOffice documentationOffice,
          DocumentationUnitLinkType type) {

    return generateNewDocumentUnit(documentationOffice)
        .flatMap(
            childDocumentUnit ->
                updateDocumentUnit(
                    enrichNewDocumentUnitWithData(childDocumentUnit, linkedDocumentationUnit),
                    documentationOffice))
        .flatMap(
            childDocumentUnit ->
                repository.linkDocumentUnits(
                    parentDocumentUnitUuid, childDocumentUnit.uuid(), type));
  }

  public Mono<DocumentUnit> linkLinkedDocumentationUnit(
      UUID parentUuid, UUID childUuid, DocumentationUnitLinkType type) {
    return repository
        .linkDocumentUnits(parentUuid, childUuid, type)
        .flatMap(documentationUnitLink -> repository.findByUuid(parentUuid));
  }

  public Mono<String> removeLinkedDocumentationUnit(
      UUID parentUuid, UUID childUuid, DocumentationUnitLinkType type) {
    return repository
        .unlinkDocumentUnit(parentUuid, childUuid, type)
        .doOnError(ex -> log.error("Couldn't unlink the documentation unit", ex))
        .then(deleteIfOrphanedLinkedDocumentationUnit(childUuid))
        .thenReturn("done");
  }

  private DocumentUnit enrichNewDocumentUnitWithData(
      DocumentUnit documentUnit, LinkedDocumentationUnit linkedDocumentationUnit) {
    List<String> fileNumbers = null;
    if (!StringUtils.isBlank(linkedDocumentationUnit.getFileNumber())) {
      fileNumbers = List.of(linkedDocumentationUnit.getFileNumber());
    }

    CoreData coreData =
        documentUnit.coreData().toBuilder()
            .fileNumbers(fileNumbers)
            .documentType(linkedDocumentationUnit.getDocumentType())
            .decisionDate(linkedDocumentationUnit.getDecisionDate())
            .court(linkedDocumentationUnit.getCourt())
            .dateKnown(linkedDocumentationUnit.isDateKnown())
            .build();

    return documentUnit.toBuilder()
        .dataSource(getDatasource(linkedDocumentationUnit))
        .coreData(coreData)
        .build();
  }

  private Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid) {
    return repository.deleteIfOrphanedLinkedDocumentationUnit(documentUnitUuid);
  }

  private static <T extends LinkedDocumentationUnit> DataSource getDatasource(
      T linkedDocumentationUnit) {
    if (linkedDocumentationUnit instanceof ActiveCitation) {
      return DataSource.ACTIVE_CITATION;
    } else if (linkedDocumentationUnit instanceof ProceedingDecision) {
      return DataSource.PROCEEDING_DECISION;
    } else {
      throw new DocumentationUnitException(
          "Couldn't find data source for " + linkedDocumentationUnit.getClass());
    }
  }

  public <T extends LinkedDocumentationUnit>
      Flux<T> findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
          UUID parentDocumentUnitUuid, DocumentationUnitLinkType type) {
    return repository.findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
        parentDocumentUnitUuid, type);
  }

  public Mono<String> validateSingleNorm(SingleNormValidationInfo singleNormValidationInfo) {
    Set<ConstraintViolation<SingleNormValidationInfo>> violations =
        validator.validate(singleNormValidationInfo);

    if (violations.isEmpty()) {
      return Mono.just("Ok");
    }
    return Mono.just("Validation error");
  }
}
