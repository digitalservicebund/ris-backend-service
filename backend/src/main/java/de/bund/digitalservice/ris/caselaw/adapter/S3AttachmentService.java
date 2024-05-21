package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentException;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Service
public class S3AttachmentService implements AttachmentService {
  private final AttachmentRepository repository;
  private final S3AsyncClient s3AsyncClient;
  private final DatabaseDocumentationUnitRepository documentUnitRepository;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public S3AttachmentService(
      AttachmentRepository repository,
      S3AsyncClient s3AsyncClient,
      DatabaseDocumentationUnitRepository documentUnitRepository) {
    this.repository = repository;
    this.s3AsyncClient = s3AsyncClient;
    this.documentUnitRepository = documentUnitRepository;
  }

  public Attachment attachFileToDocumentationUnit(
      UUID documentationUnitUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {
    var fileUuid = UUID.randomUUID();
    String fileName =
        httpHeaders.containsKey("X-Filename")
            ? httpHeaders.getFirst("X-Filename")
            : "Kein Dateiname gefunden";

    checkDocx(byteBuffer);

    putObjectIntoBucket(fileUuid.toString(), byteBuffer, httpHeaders);

    AttachmentDTO attachmentDTO =
        AttachmentDTO.builder()
            .id(fileUuid)
            .s3ObjectPath(fileUuid.toString())
            .documentationUnit(documentUnitRepository.findById(documentationUnitUuid).orElseThrow())
            .filename(fileName)
            .format("docx")
            .uploadTimestamp(Instant.now())
            .build();

    return AttachmentTransformer.transformToDomain(repository.save(attachmentDTO));
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public void deleteByS3Path(String s3Path) {
    deleteObjectFromBucket(s3Path);
    repository.deleteByS3ObjectPath(s3Path);
  }

  public void deleteAllObjectsFromBucketForDocumentationUnit(UUID uuid) {
    repository.findAllByDocumentationUnitId(uuid).stream()
        .map(AttachmentDTO::getS3ObjectPath)
        .forEach(this::deleteObjectFromBucket);
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

  private byte[] byteBufferToArray(ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    byte[] byteBufferArray = new byte[byteBuffer.remaining()];
    byteBuffer.get(byteBufferArray);
    byteBuffer.rewind();
    return byteBufferArray;
  }

  private PutObjectResponse putObjectIntoBucket(
      String fileUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {

    var contentLength = httpHeaders.getContentLength();

    Map<String, String> metadata = new HashMap<>();
    MediaType mediaType = httpHeaders.getContentType();
    if (mediaType == null) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    log.debug("upload header information: mediaType{}, contentLength={}", mediaType, contentLength);

    var asyncRequestBody = AsyncRequestBody.fromByteBuffer(byteBuffer);
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

    try {
      return s3AsyncClient.putObject(putObjectRequest, asyncRequestBody).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AttachmentException("Could not save object to bucket: " + fileUuid);
    } catch (ExecutionException e) {
      throw new AttachmentException("Could not save object to bucket: " + fileUuid);
    }
  }

  private void deleteObjectFromBucket(String s3Path) {
    if (StringsUtil.returnTrueIfNullOrBlank(s3Path)) {
      throw new AttachmentException("s3Path cant be null");
    }

    var deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(s3Path).build();
    s3AsyncClient.deleteObject(deleteObjectRequest);
  }
}
