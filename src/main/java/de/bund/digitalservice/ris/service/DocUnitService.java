package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.repository.DocUnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocUnitService {
  private final DocUnitRepository repository;

  private final S3AsyncClient s3AsyncClient;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocUnitService(
      DocUnitRepository repository, S3AsyncClient s3AsyncClient) {
    Assert.notNull(repository, "doc unit repository is null");
    Assert.notNull(s3AsyncClient, "s3 async client is null");

    this.repository = repository;
    this.s3AsyncClient = s3AsyncClient;
  }

  public Mono<ResponseEntity<DocUnit>> generateNewDocUnit(Mono<FilePart> filePartMono) {
    var fileKey = UUID.randomUUID().toString();

    return filePartMono
        .doOnNext(filePart -> log.info("uploaded file name {}", filePart.filename()))
        .map(filePart ->filePart.content().map(DataBuffer::asByteBuffer))
        .map(byteBufferFlux -> putObjectIntoBucket(fileKey, byteBufferFlux))
        .doOnNext(putObjectResponseMono -> log.debug("generate doc unit for {}", fileKey))
        .map(putObjectResponseMono -> generateDataObject(fileKey, "docx"))
        .doOnNext(docUnitMono -> log.debug("save doc unit"))
        .flatMap(repository::save)
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .doOnError(ex -> log.error("Couldn't upload the file to bucket", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  private Mono<PutObjectResponse> putObjectIntoBucket(String fileKey, Flux<ByteBuffer> byteBufferFlux) {
    var putObjectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(fileKey).build();
    var asyncRequestBody = AsyncRequestBody.fromPublisher(byteBufferFlux);
    return Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, asyncRequestBody));
  }

  private DocUnit generateDataObject(String filename, String type) {
    var docUnit = new DocUnit();
    docUnit.setS3path(filename);
    docUnit.setFiletype(type);
    return docUnit;
  }

  public Mono<ResponseEntity<Flux<DocUnit>>> getAll() {
    return Mono.just(ResponseEntity.ok(repository.findAll()));
  }
}
