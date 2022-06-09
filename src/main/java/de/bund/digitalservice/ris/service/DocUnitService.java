package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.repository.DocUnitRepository;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@Slf4j
public class DocUnitService {
  private final DocUnitRepository repository;

  private final S3AsyncClient s3AsyncClient;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocUnitService(DocUnitRepository repository, S3AsyncClient s3AsyncClient) {
    Assert.notNull(repository, "doc unit repository is null");
    Assert.notNull(s3AsyncClient, "s3 async client is null");

    this.repository = repository;
    this.s3AsyncClient = s3AsyncClient;
  }

  public Mono<ResponseEntity<DocUnit>> generateNewDocUnit() {
    return repository
        .save(generateDateObject())
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  public Mono<ResponseEntity<DocUnit>> attachFileToDocUnit(
      int docUnitId, Flux<ByteBuffer> byteBufferFlux, HttpHeaders httpHeaders) {
    var fileUuid = UUID.randomUUID().toString();

    return putObjectIntoBucket(fileUuid, byteBufferFlux, httpHeaders)
        .doOnNext(putObjectResponse -> log.debug("generate doc unit for {}", fileUuid))
        .flatMap(
            putObjectResponse ->
                repository
                    .findById(docUnitId)
                    .map(
                        docUnit -> {
                          docUnit.setS3path(fileUuid);
                          docUnit.setFilename(
                              httpHeaders.containsKey("filename")
                                  ? httpHeaders.getFirst("filename")
                                  : "no filename found");
                          return docUnit;
                        }))
        .doOnNext(docUnit -> log.debug("save doc unit"))
        .flatMap(repository::save)
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .doOnError(ex -> log.error("Couldn't upload the file to bucket", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  public Mono<ResponseEntity<DocUnit>> removeFileFromDocUnit(int docUnitId) {
    // TODO delete from bucket
    return repository
        .findById(docUnitId)
        .flatMap(
            docUnit -> {
              docUnit.setS3path(null);
              docUnit.setFilename(null);
              return repository.save(docUnit);
            })
        .doOnNext(docUnit -> log.debug("removed file from doc unit"))
        .map(docUnit -> ResponseEntity.status(HttpStatus.OK).body(docUnit))
        .doOnError(ex -> log.error("Couldn't remove the file from the doc unit", ex))
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

  private DocUnit generateDateObject() {
    var docUnit = new DocUnit();
    // if I don't set anything, I get the error "Column count does not match; SQL statement"
    // it should be possible to not set anything though TODO
    docUnit.setFiletype("docx");
    return docUnit;
  }

  public Mono<ResponseEntity<Flux<DocUnit>>> getAll() {
    return Mono.just(ResponseEntity.ok(repository.findAll()));
  }

  public Mono<ResponseEntity<DocUnit>> getById(int id) {
    return repository.findById(id).map(ResponseEntity::ok);
  }

  public Mono<ResponseEntity<DocUnit>> updateDocUnit(DocUnit docUnit) {
    return repository
        .save(docUnit)
        .map(ResponseEntity::ok)
        .doOnError(ex -> log.error("Couldn't update the DocUnit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }
}
