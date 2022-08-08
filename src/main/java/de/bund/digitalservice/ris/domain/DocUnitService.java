package de.bund.digitalservice.ris.domain;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@Slf4j
public class DocUnitService {
  private final DocUnitRepository repository;
  private final DocumentNumberCounterRepository counterRepository;
  private final S3AsyncClient s3AsyncClient;
  private final DocumentUnitPublishService publishService;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocUnitService(
      DocUnitRepository repository,
      DocumentNumberCounterRepository counterRepository,
      S3AsyncClient s3AsyncClient,
      DocumentUnitPublishService publishService) {
    this.repository = repository;
    this.counterRepository = counterRepository;
    this.s3AsyncClient = s3AsyncClient;
    this.publishService = publishService;
  }

  public Mono<DocUnit> generateNewDocUnit(DocUnitCreationInfo docUnitCreationInfo) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    return counterRepository
        .getDocumentNumberCounterEntry()
        .flatMap(
            outdatedDocumentNumberCounter -> {
              // this is the switch happening when the first new DocUnit in a new year gets created
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
                    DocUnit.createNew(
                        docUnitCreationInfo, updatedDocumentNumberCounter.nextnumber - 1)))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex));
  }

  public Mono<ResponseEntity<DocUnit>> attachFileToDocUnit(
      UUID docUnitId, Flux<ByteBuffer> byteBufferFlux, HttpHeaders httpHeaders) {
    var fileUuid = UUID.randomUUID().toString();

    return putObjectIntoBucket(fileUuid, byteBufferFlux, httpHeaders)
        .doOnNext(putObjectResponse -> log.debug("generate doc unit for {}", fileUuid))
        .flatMap(
            putObjectResponse ->
                repository
                    .findByUuid(docUnitId)
                    .map(
                        docUnit -> {
                          docUnit.setFileuploadtimestamp(Instant.now());
                          docUnit.setS3path(fileUuid);
                          docUnit.setFiletype("docx");
                          docUnit.setFilename(
                              httpHeaders.containsKey("X-Filename")
                                  ? httpHeaders.getFirst("X-Filename")
                                  : "Kein Dateiname gefunden");
                          return docUnit;
                        }))
        .doOnNext(docUnit -> log.debug("save doc unit"))
        .flatMap(repository::save)
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .doOnError(ex -> log.error("Couldn't upload the file to bucket", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  public Mono<ResponseEntity<DocUnit>> removeFileFromDocUnit(UUID docUnitId) {
    return repository
        .findByUuid(docUnitId)
        .flatMap(
            docUnit -> {
              var fileUuid = docUnit.getS3path();
              return deleteObjectFromBucket(fileUuid)
                  .doOnNext(
                      deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid))
                  .map(
                      deleteObjectResponse -> {
                        docUnit.setS3path(null);
                        docUnit.setFilename(null);
                        docUnit.setFileuploadtimestamp(null);
                        return docUnit;
                      });
            })
        .doOnNext(docUnit -> log.debug("removed file from DocUnit {}", docUnitId))
        .flatMap(repository::save)
        .map(docUnit -> ResponseEntity.status(HttpStatus.OK).body(docUnit))
        .doOnError(ex -> log.error("Couldn't remove the file from the DocUnit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  private Mono<PutObjectResponse> putObjectIntoBucket(
      String fileUuid, Flux<ByteBuffer> byteBufferFlux, HttpHeaders httpHeaders) {

    var contentLength = httpHeaders.getContentLength();

    Map<String, String> metadata = new HashMap<>();
    MediaType mediaType = httpHeaders.getContentType();
    if (mediaType == null) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    log.debug("upload header information: mediaType{}, contentLength={}", mediaType, contentLength);

    var asyncRequestBody = AsyncRequestBody.fromPublisher(byteBufferFlux);
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

  public Mono<ResponseEntity<Flux<DocUnit>>> getAll() {
    return Mono.just(ResponseEntity.ok(repository.findAll(Sort.by(Order.desc("documentnumber")))));
  }

  public Mono<ResponseEntity<DocUnit>> getByDocumentnumber(String documentnumber) {
    return repository.findByDocumentnumber(documentnumber).map(ResponseEntity::ok);
  }

  public Mono<ResponseEntity<String>> deleteByUuid(UUID docUnitId) {
    return repository
        .findByUuid(docUnitId)
        .flatMap(
            docUnit -> {
              if (docUnit.hasFileAttached()) {
                var fileUuid = docUnit.getS3path();
                return deleteObjectFromBucket(fileUuid)
                    .doOnNext(
                        deleteObjectResponse -> log.debug("deleted file {} in bucket", fileUuid))
                    .flatMap(deleteObjectResponse -> repository.delete(docUnit));
              }
              return repository.delete(docUnit);
            })
        .doOnNext(v -> log.debug("deleted doc unit"))
        .map(v -> ResponseEntity.status(HttpStatus.OK).body("done"))
        .doOnError(ex -> log.error("Couldn't delete the DocUnit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body("Couldn't delete the DocUnit"));
  }

  public Mono<ResponseEntity<DocUnit>> updateDocUnit(DocUnit docUnit) {
    return repository
        .save(docUnit)
        .map(ResponseEntity::ok)
        .doOnError(ex -> log.error("Couldn't update the DocUnit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  public Mono<ExportObject> publish(UUID uuid) {
    return repository.findByUuid(uuid).flatMap(publishService::publish);
  }
}
