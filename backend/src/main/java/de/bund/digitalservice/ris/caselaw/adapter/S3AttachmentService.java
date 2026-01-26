package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentInlineTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentException;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.Image;
import de.bund.digitalservice.ris.caselaw.domain.StreamedFileResponse;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

@Slf4j
@Service
public class S3AttachmentService implements AttachmentService {
  private final AttachmentRepository repository;
  private final DatabaseAttachmentInlineRepository attachmentInlineRepository;
  private final S3Client s3Client;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DocumentationUnitHistoryLogService documentationUnitHistoryLogService;
  private static final String UNKNOWN_YET = "unknown yet";
  private final MediaType wordMediaType =
      MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public static boolean equalsMediaType(MediaType expected, MediaType actual) {
    return expected.getType().equalsIgnoreCase(actual.getType())
        && expected.getSubtype().equalsIgnoreCase(actual.getSubtype());
  }

  public S3AttachmentService(
      AttachmentRepository repository,
      DatabaseAttachmentInlineRepository attachmentInlineRepository,
      @Qualifier("docxS3Client") S3Client s3Client,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DocumentationUnitHistoryLogService documentationUnitHistoryLogService) {
    this.repository = repository;
    this.attachmentInlineRepository = attachmentInlineRepository;
    this.s3Client = s3Client;
    this.documentationUnitRepository = documentationUnitRepository;
    this.documentationUnitHistoryLogService = documentationUnitHistoryLogService;
  }

  public Attachment attachFileToDocumentationUnit(
      UUID documentationUnitId, ByteBuffer byteBuffer, HttpHeaders httpHeaders, User user) {
    String fileName =
        httpHeaders.containsHeader("X-Filename")
            ? httpHeaders.getFirst("X-Filename")
            : "Kein Dateiname gefunden";

    DocumentationUnitDTO documentationUnit =
        documentationUnitRepository.findById(documentationUnitId).orElseThrow();

    MediaType contentType = httpHeaders.getContentType();

    if (contentType == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Missing / invalid Content-Type header");
    }

    if (equalsMediaType(wordMediaType, contentType)) {
      return attachDocx(
          documentationUnitId, byteBuffer, httpHeaders, user, documentationUnit, fileName);
    } else if (ImageUtil.getSupportedMediaTypes().stream()
        .anyMatch(type -> equalsMediaType(type, contentType))) {

      return attachImage(byteBuffer, contentType, documentationUnit);

    } else {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only images and docx are supported");
    }
  }

  public Attachment streamFileToDocumentationUnit(
      UUID documentationUnitId, InputStream file, String filename, User user, AttachmentType type) {

    var documentationUnit = documentationUnitRepository.findById(documentationUnitId).orElseThrow();
    var documentationUnitNumber = documentationUnit.getDocumentNumber();

    var extension = getExtension(filename);

    var attachmentDTO =
        AttachmentDTO.builder()
            .s3ObjectPath(UNKNOWN_YET)
            .documentationUnit(documentationUnit)
            .filename(filename)
            .format(extension)
            .uploadTimestamp(Instant.now())
            .attachmentType(type.name())
            .build();

    attachmentDTO = repository.save(attachmentDTO);
    var s3ObjectPath =
        "%s/%s.%s"
            .formatted(
                documentationUnitNumber,
                attachmentDTO.getId().toString(),
                attachmentDTO.getFormat());

    try {
      streamFileToBucket(s3ObjectPath, file);
    } catch (Exception e) {
      log.error("Failed to upload file to S3", e);
      try {
        repository.delete(attachmentDTO);
      } catch (Exception deleteEx) {
        log.error("Failed to delete attachment record after failed multipart upload", deleteEx);
      }
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
    }

    attachmentDTO.setS3ObjectPath(s3ObjectPath);
    var attachment = AttachmentTransformer.transformToDomain(repository.save(attachmentDTO));

    setLastUpdated(user, documentationUnit);
    documentationUnitHistoryLogService.saveHistoryLog(
        documentationUnitId, user, HistoryLogEventType.FILES, "File uploaded");

    return attachment;
  }

  public String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }

    int lastDotIndex = filename.lastIndexOf('.');

    if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
      return filename.substring(lastDotIndex + 1);
    }

    return "";
  }

  private void streamFileToBucket(String s3ObjectPath, InputStream file) {

    var createdMultipartUpload =
        s3Client.createMultipartUpload(
            c ->
                c.bucket(bucketName)
                    .key(s3ObjectPath)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
    String uploadId = createdMultipartUpload.uploadId();

    final var PART_SIZE = 5 * 1024 * 1024;
    List<CompletedPart> completedParts = new ArrayList<>();
    var partNumber = 1;

    try (file) {
      while (true) {
        byte[] buffer = file.readNBytes(PART_SIZE);

        if (buffer.length == 0) {
          break;
        }

        var uploadPartRequest =
            UploadPartRequest.builder()
                .bucket(bucketName)
                .key(s3ObjectPath)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength((long) buffer.length)
                .build();

        var uploadPartResponse =
            s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(buffer));
        completedParts.add(
            CompletedPart.builder().partNumber(partNumber).eTag(uploadPartResponse.eTag()).build());
        partNumber++;
      }

      var completeRequest =
          CompleteMultipartUploadRequest.builder()
              .bucket(bucketName)
              .key(s3ObjectPath)
              .uploadId(uploadId)
              .multipartUpload(mpu -> mpu.parts(completedParts))
              .build();

      s3Client.completeMultipartUpload(completeRequest);
    } catch (Exception e) {
      log.atWarn()
          .addKeyValue("uploadId", uploadId)
          .setCause(e)
          .log("Multipart upload failed, aborting uploadId={}", uploadId, e);
      try {
        s3Client.abortMultipartUpload(
            AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(s3ObjectPath)
                .uploadId(uploadId)
                .build());
      } catch (Exception abortEx) {
        log.warn("Failed to abort multipart upload for id {}", uploadId, abortEx);
      }
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
    }
  }

  private Attachment attachImage(
      ByteBuffer byteBuffer, MediaType contentType, DocumentationUnitDTO documentationUnit) {

    AttachmentInlineDTO attachmentInlineDTO =
        AttachmentInlineDTO.builder()
            .content(byteBuffer.array())
            .documentationUnit(documentationUnit)
            .format(contentType.getSubtype().toLowerCase())
            .filename(UNKNOWN_YET)
            .uploadTimestamp(Instant.now())
            .build();

    attachmentInlineDTO = attachmentInlineRepository.save(attachmentInlineDTO);
    var fileName = attachmentInlineDTO.getId() + "." + attachmentInlineDTO.getFormat();
    attachmentInlineDTO.setFilename(fileName);
    var persistedAttachmentLineDTO = attachmentInlineRepository.save(attachmentInlineDTO);

    return AttachmentInlineTransformer.transformToDomain(persistedAttachmentLineDTO);
  }

  private Attachment attachDocx(
      UUID documentationUnitId,
      ByteBuffer byteBuffer,
      HttpHeaders httpHeaders,
      User user,
      DocumentationUnitDTO documentationUnit,
      String fileName) {
    checkDocx(byteBuffer);

    AttachmentDTO attachmentDTO =
        AttachmentDTO.builder()
            .s3ObjectPath(UNKNOWN_YET)
            .documentationUnit(documentationUnit)
            .filename(fileName)
            .format("docx")
            .uploadTimestamp(Instant.now())
            .attachmentType(AttachmentType.ORIGINAL.name())
            .build();

    attachmentDTO = repository.save(attachmentDTO);

    String s3ObjectPath = attachmentDTO.getId().toString();
    putObjectIntoBucket(s3ObjectPath, byteBuffer, httpHeaders);

    attachmentDTO.setS3ObjectPath(s3ObjectPath);

    Attachment attachment = AttachmentTransformer.transformToDomain(repository.save(attachmentDTO));

    setLastUpdated(user, documentationUnit);
    documentationUnitHistoryLogService.saveHistoryLog(
        documentationUnitId, user, HistoryLogEventType.FILES, "Word-Dokument hinzugefügt");

    return attachment;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public void deleteByS3Path(String s3Path, UUID documentationUnitId, User user) {
    deleteObjectFromBucket(s3Path);
    documentationUnitRepository
        .findById(documentationUnitId)
        .ifPresent(
            documentationUnit -> {
              setLastUpdated(user, documentationUnit);
              documentationUnitHistoryLogService.saveHistoryLog(
                  documentationUnitId, user, HistoryLogEventType.FILES, "Word-Dokument gelöscht");
            });
    repository.deleteByS3ObjectPath(s3Path);
  }

  public void deleteAllObjectsFromBucketForDocumentationUnit(UUID uuid) {
    repository.findAllByDocumentationUnitId(uuid).stream()
        .map(AttachmentDTO::getS3ObjectPath)
        .filter(Objects::nonNull)
        .forEach(this::deleteObjectFromBucket);
  }

  @Override
  public Optional<Image> findByDocumentationUnitIdAndFileName(
      UUID documentationUnitId, String imageName) {
    return attachmentInlineRepository
        .findByDocumentationUnitIdAndFilename(documentationUnitId, imageName)
        .map(
            attachmentInlineDTO ->
                Image.builder()
                    .content(attachmentInlineDTO.getContent())
                    .contentType(attachmentInlineDTO.getFormat())
                    .name(attachmentInlineDTO.getFilename())
                    .build());
  }

  @Override
  public StreamedFileResponse getFileStream(UUID documentationUnitId, UUID fileUuid) {
    var attachmentDTO =
        repository.findById(fileUuid).orElseThrow(() -> new AttachmentException("File not found"));

    var getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(attachmentDTO.getS3ObjectPath()).build();

    ResponseTransformer<GetObjectResponse, ResponseInputStream<GetObjectResponse>> transformer =
        ResponseTransformer.toInputStream();

    ResponseInputStream<GetObjectResponse> stream =
        s3Client.getObject(getObjectRequest, transformer);

    StreamingResponseBody responseBody =
        outputStream -> {
          try (stream) {
            stream.transferTo(outputStream);
            outputStream.flush();
          } catch (IOException e) {
            log.error("Failed to stream file from S3", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
          }
        };

    return new StreamedFileResponse(stream.response(), responseBody, attachmentDTO.getFilename());
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

  private void putObjectIntoBucket(
      String fileUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders) {

    var contentLength = httpHeaders.getContentLength();

    Map<String, String> metadata = new HashMap<>();
    MediaType mediaType = httpHeaders.getContentType();
    if (mediaType == null) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    log.debug("upload header information: mediaType{}, contentLength={}", mediaType, contentLength);

    var requestBody = RequestBody.fromByteBuffer(byteBuffer);
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

    s3Client.putObject(putObjectRequest, requestBody);
  }

  private void deleteObjectFromBucket(String s3Path) {
    if (StringUtils.isNullOrBlank(s3Path)) {
      throw new AttachmentException("s3Path cant be null");
    }

    var deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(s3Path).build();
    s3Client.deleteObject(deleteObjectRequest);
  }

  private void setLastUpdated(User user, DocumentationUnitDTO documentationUnit) {
    ManagementDataDTO managementData = documentationUnit.getManagementData();
    if (managementData == null) {
      managementData =
          ManagementDataDTO.builder()
              .documentationUnit(documentationUnit)
              .lastUpdatedAtDateTime(Instant.now())
              .lastUpdatedByUserId(user.id())
              .lastUpdatedByUserName(user.name())
              .lastUpdatedByDocumentationOffice(
                  DocumentationOfficeTransformer.transformToDTO(user.documentationOffice()))
              .build();
      documentationUnit.setManagementData(managementData);
    } else {
      managementData.setLastUpdatedAtDateTime(Instant.now());
      managementData.setLastUpdatedByUserId(user.id());
      managementData.setLastUpdatedBySystemName(null);
      managementData.setLastUpdatedByUserName(user.name());
      managementData.setLastUpdatedByDocumentationOffice(
          DocumentationOfficeTransformer.transformToDTO(user.documentationOffice()));
    }
  }
}
