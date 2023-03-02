package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.utils.ServiceUtils.byteBufferToArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
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
  private final DocumentNumberService documentNumberService;
  private final S3AsyncClient s3AsyncClient;
  private final EmailPublishService publishService;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentUnitListEntryRepository listEntryRepository,
      DocumentNumberService documentNumberService,
      S3AsyncClient s3AsyncClient,
      EmailPublishService publishService) {

    this.repository = repository;
    this.listEntryRepository = listEntryRepository;
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
    return listEntryRepository.findAll(Sort.by(Order.desc("documentNumber")));
  }

  public Mono<DocumentUnit> getByDocumentNumber(String documentNumber) {
    return repository.findByDocumentNumber(documentNumber);
  }

  public Mono<String> deleteByUuid(UUID documentUnitId) {
    return repository
        .findByUuid(documentUnitId)
        .flatMap(
            documentUnit -> {
              if (documentUnit.s3path() != null) {
                var fileUuid = documentUnit.s3path();
                return deleteObjectFromBucket(fileUuid)
                    .doOnNext(
                        deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid))
                    .flatMap(deleteObjectResponse -> repository.delete(documentUnit));
              }
              return repository.delete(documentUnit);
            })
        .doOnNext(v -> log.debug("deleted DocumentUnitDTO"))
        .map(v -> "done")
        .doOnError(ex -> log.error("Couldn't delete the DocumentUnit", ex));
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
}
