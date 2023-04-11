package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.ServiceUtils.byteBufferToArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
  private final DocumentNumberService documentNumberService;
  private final S3AsyncClient s3AsyncClient;
  private final EmailPublishService publishService;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentNumberService documentNumberService,
      S3AsyncClient s3AsyncClient,
      EmailPublishService publishService) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.s3AsyncClient = s3AsyncClient;
    this.publishService = publishService;
  }

  public Mono<DocumentUnit> generateNewDocumentUnit(
      DocumentUnitCreationInfo documentUnitCreationInfo) {
    return documentNumberService
        .generateNextDocumentNumber(documentUnitCreationInfo)
        .flatMap(repository::createNewDocumentUnit)
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
    var deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(fileUuid).build();
    return Mono.fromCallable(() -> Mono.fromFuture(s3AsyncClient.deleteObject(deleteObjectRequest)))
        .flatMap(Function.identity());
  }

  public Flux<DocumentUnitListEntry> getAll() {
    return repository.findAll(Sort.by(Order.desc("creationtimestamp")));
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
                              removeProceedingDecision(
                                  documentUnitUuid, proceedingDecision.uuid())))
                  .then(repository.delete(documentUnit))
                  .thenReturn(logMsg);
            })
        .onErrorResume(
            ex -> {
              log.error("Couldn't delete the DocumentUnit");
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

  public Mono<DocumentUnit> updateDocumentUnit(DocumentUnit documentUnit) {
    return repository
        .save(documentUnit)
        .doOnError(ex -> log.error("Couldn't update the DocumentUnit", ex));
  }

  public Mono<MailResponse> publishAsEmail(UUID documentUnitUuid, String receiverAddress) {
    return repository
        .findByUuid(documentUnitUuid)
        .flatMap(documentUnit -> publishService.publish(documentUnit, receiverAddress));
  }

  public Mono<MailResponse> getLastPublishedXmlMail(UUID documentUuid) {
    return publishService.getLastPublishedXml(documentUuid);
  }

  public Flux<ProceedingDecision> searchForDocumentUnitsByProceedingDecisionInput(
      ProceedingDecision proceedingDecision) {
    return repository.searchForDocumentUnitsByProceedingDecisionInput(proceedingDecision);
  }

  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Flux<ProceedingDecision> createProceedingDecision(
      UUID parentDocumentUnitUuid, ProceedingDecision proceedingDecision) {

    return generateNewDocumentUnit(new DocumentUnitCreationInfo("KO", "RE"))
        .flatMap(
            childDocumentUnit ->
                updateDocumentUnit(
                    enrichNewDocumentUnitWithData(childDocumentUnit, proceedingDecision)))
        .flatMap(
            childDocumentUnit ->
                repository.linkDocumentUnits(parentDocumentUnitUuid, childDocumentUnit.uuid()))
        .flatMapMany(
            documentUnit ->
                repository.findAllLinkedDocumentUnitsByParentDocumentUnitId(
                    parentDocumentUnitUuid));
  }

  public Mono<DocumentUnit> linkProceedingDecision(UUID parentUuid, UUID childUuid) {
    return repository.linkDocumentUnits(parentUuid, childUuid);
  }

  public Mono<String> removeProceedingDecision(UUID parentUuid, UUID childUuid) {
    return repository
        .unlinkDocumentUnits(parentUuid, childUuid)
        .doOnError(ex -> log.error("Couldn't unlink the ProceedingDecision", ex))
        .then(deleteIfOrphanedProceedingDecision(childUuid))
        .thenReturn("done");
  }

  private DocumentUnit enrichNewDocumentUnitWithData(
      DocumentUnit documentUnit, ProceedingDecision proceedingDecision) {
    List<String> fileNumbers = null;
    if (!StringUtils.isBlank(proceedingDecision.fileNumber())) {
      fileNumbers = List.of(proceedingDecision.fileNumber());
    }

    CoreData coreData =
        documentUnit.coreData().toBuilder()
            .fileNumbers(fileNumbers)
            .documentType(proceedingDecision.documentType())
            .decisionDate(proceedingDecision.date())
            .court(proceedingDecision.court())
            .build();

    return documentUnit.toBuilder()
        .dataSource(DataSource.PROCEEDING_DECISION)
        .coreData(coreData)
        .build();
  }

  private Mono<Void> deleteIfOrphanedProceedingDecision(UUID documentUnitUuid) {
    return repository
        .findByUuid(documentUnitUuid)
        .filter(
            childDocumentUnit ->
                DataSource.PROCEEDING_DECISION.equals(childDocumentUnit.dataSource()))
        .flatMap(repository::filterUnlinkedDocumentUnit)
        .flatMap(repository::delete);
  }
}
