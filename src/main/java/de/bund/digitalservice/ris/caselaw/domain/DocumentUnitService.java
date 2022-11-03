package de.bund.digitalservice.ris.caselaw.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
  private final DocumentUnitListEntryRepository listEntryRepository;
  private final DocumentNumberCounterRepository counterRepository;
  private final PreviousDecisionRepository previousDecisionRepository;
  private final S3AsyncClient s3AsyncClient;
  private final EmailPublishService publishService;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentUnitListEntryRepository listEntryRepository,
      DocumentNumberCounterRepository counterRepository,
      PreviousDecisionRepository previousDecisionRepository,
      S3AsyncClient s3AsyncClient,
      EmailPublishService publishService) {
    this.repository = repository;
    this.listEntryRepository = listEntryRepository;
    this.counterRepository = counterRepository;
    this.s3AsyncClient = s3AsyncClient;
    this.publishService = publishService;
    this.previousDecisionRepository = previousDecisionRepository;
  }

  public Mono<DocumentUnit> generateNewDocumentUnit(
      DocumentUnitCreationInfo documentUnitCreationInfo) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    return counterRepository
        .getDocumentNumberCounterEntry()
        .flatMap(
            outdatedDocumentNumberCounter -> {
              // this is the switch happening when the first new DocumentUnit in a new year gets
              // created
              if (outdatedDocumentNumberCounter.currentyear != currentYear) {
                outdatedDocumentNumberCounter.currentyear = currentYear;
                outdatedDocumentNumberCounter.nextnumber = 1;
              }
              outdatedDocumentNumberCounter.nextnumber += 1;
              return counterRepository.save(outdatedDocumentNumberCounter);
            })
        .flatMap(
            updatedDocumentNumberCounter ->
                repository.save(
                    DocumentUnitDTO.createNew(
                        documentUnitCreationInfo, updatedDocumentNumberCounter.nextnumber - 1)))
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build())
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).jitter(0.75))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex));
  }

  public Mono<DocumentUnit> attachFileToDocumentUnit(
      UUID documentUnitId, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {
    var fileUuid = UUID.randomUUID().toString();
    checkDocx(byteBuffer);
    return putObjectIntoBucket(fileUuid, byteBuffer, httpHeaders)
        .doOnNext(putObjectResponse -> log.debug("generate doc unit for {}", fileUuid))
        .flatMap(
            putObjectResponse ->
                repository
                    .findByUuid(documentUnitId)
                    .map(
                        documentUnitDTO -> {
                          documentUnitDTO.setFileuploadtimestamp(Instant.now());
                          documentUnitDTO.setS3path(fileUuid);
                          documentUnitDTO.setFiletype("docx");
                          documentUnitDTO.setFilename(
                              httpHeaders.containsKey("X-Filename")
                                  ? httpHeaders.getFirst("X-Filename")
                                  : "Kein Dateiname gefunden");
                          return documentUnitDTO;
                        }))
        .doOnNext(documentUnitDTO -> log.debug("save documentUnitDTO"))
        .flatMap(repository::save)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build())
        .doOnError(ex -> log.error("Couldn't upload the file to bucket", ex));
  }

  public Mono<DocumentUnit> removeFileFromDocumentUnit(UUID documentUnitId) {
    return repository
        .findByUuid(documentUnitId)
        .flatMap(
            documentUnitDTO -> {
              var fileUuid = documentUnitDTO.getS3path();
              return deleteObjectFromBucket(fileUuid)
                  .doOnNext(
                      deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid))
                  .map(
                      deleteObjectResponse -> {
                        documentUnitDTO.setS3path(null);
                        documentUnitDTO.setFilename(null);
                        documentUnitDTO.setFileuploadtimestamp(null);
                        return documentUnitDTO;
                      });
            })
        .doOnNext(
            documentUnitDTO -> log.debug("removed file from DocumentUnitDTO {}", documentUnitId))
        .flatMap(repository::save)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build())
        .doOnError(ex -> log.error("Couldn't remove the file from the DocumentUnit", ex));
  }

  void checkDocx(ByteBuffer byteBuffer) {
    var zip = new ZipInputStream(new ByteArrayInputStream(byteBuffer.array()));
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

  public Mono<Flux<DocumentUnitListEntry>> getAll() {
    return Mono.just(listEntryRepository.findAll(Sort.by(Order.desc("documentnumber"))));
  }

  public Mono<DocumentUnit> getByDocumentnumber(String documentnumber) {
    return repository
        .findByDocumentnumber(documentnumber)
        .flatMap(
            documentUnitDTO ->
                previousDecisionRepository
                    .findAllByDocumentnumber(documentnumber)
                    .collectList()
                    .flatMap(
                        previousDecisions ->
                            Mono.just(documentUnitDTO.setPreviousDecisions(previousDecisions))))
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  public Mono<String> deleteByUuid(UUID documentUnitId) {
    return repository
        .findByUuid(documentUnitId)
        .flatMap(
            documentUnitDTO -> {
              if (documentUnitDTO.hasFileAttached()) {
                var fileUuid = documentUnitDTO.getS3path();
                return deleteObjectFromBucket(fileUuid)
                    .doOnNext(
                        deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid))
                    .flatMap(deleteObjectResponse -> repository.delete(documentUnitDTO));
              }
              return repository.delete(documentUnitDTO);
            })
        .doOnNext(v -> log.debug("deleted DocumentUnitDTO"))
        .map(v -> "done")
        .doOnError(ex -> log.error("Couldn't delete the DocumentUnit", ex));
  }

  public Mono<DocumentUnit> updateDocumentUnit(DocumentUnit documentUnit) {
    DocumentUnitDTO documentUnitDTO = DocumentUnitDTO.buildFromDocumentUnit(documentUnit);
    if (documentUnitDTO.previousDecisions == null)
      return repository
          .save(documentUnitDTO)
          .map(duDTO -> DocumentUnitBuilder.newInstance().setDocumentUnitDTO(duDTO).build())
          .doOnError(ex -> log.error("Couldn't update the DocumentUnit", ex));

    /* Passing foreign key to object */
    List<PreviousDecision> previousDecisionsList =
        documentUnitDTO.previousDecisions.stream()
            .map(
                previousDecision ->
                    previousDecision.setDocumentnumber(documentUnitDTO.documentnumber))
            .toList();
    /* Get all id of previous decisions from font-end */
    List<Long> incomingIds =
        previousDecisionsList.stream()
            .filter(previousDecision -> previousDecision.id != null)
            .map(previousDecision -> previousDecision.id)
            .toList();
    /* Create mono publisher object to loop through */
    Mono<List<PreviousDecision>> listPreviousDecisionMonoObj = Mono.just(previousDecisionsList);
    return listPreviousDecisionMonoObj
        .flatMap(
            previousDecisions ->
                previousDecisionRepository
                    .getAllIdsByDocumentnumber(documentUnitDTO.documentnumber)
                    .collectList()
                    .flatMap(
                        inDatabaseIds -> {
                          /* Get all id of deleted decisions */
                          List<Long> deletedIndexes =
                              getDeletedPreviousDecisionIds(incomingIds, inDatabaseIds);
                          /* Insert/Update decisions */
                          return previousDecisionRepository
                              .saveAll(
                                  previousDecisions.stream()
                                      .filter(
                                          previousDecision ->
                                              !deletedIndexes.contains(previousDecision.id))
                                      .toList())
                              .then(
                                  /* Delete decisions */
                                  previousDecisionRepository.deleteAllById(
                                      deletedIndexes.stream().map(String::valueOf).toList()));
                        }))
        .then(repository.save(documentUnitDTO))
        .map(duDTO -> DocumentUnitBuilder.newInstance().setDocumentUnitDTO(duDTO).build())
        .doOnError(ex -> log.error("Couldn't update the DocumentUnit", ex));
  }

  private List<Long> getDeletedPreviousDecisionIds(
      List<Long> incomingIds, List<Long> inDatabaseIds) {
    /* Return all ids in database but not in incoming Id from front end */
    return inDatabaseIds.stream().filter(index -> !incomingIds.contains(index)).toList();
  }

  public Mono<MailResponse> publishAsEmail(UUID documentUnitUuid, String receiverAddress) {
    return repository
        .findByUuid(documentUnitUuid)
        .flatMap(
            documentUnit ->
                previousDecisionRepository
                    .findAllByDocumentnumber(documentUnit.documentnumber)
                    .collectList()
                    .flatMap(
                        previousDecisions ->
                            publishService.publish(
                                documentUnit.setPreviousDecisions(previousDecisions),
                                receiverAddress)));
  }

  public Mono<MailResponse> getLastPublishedXmlMail(UUID documentUuid) {
    return repository
        .findByUuid(documentUuid)
        .flatMap(
            documentUnit -> publishService.getLastPublishedXml(documentUnit.getId(), documentUuid));
  }
}
