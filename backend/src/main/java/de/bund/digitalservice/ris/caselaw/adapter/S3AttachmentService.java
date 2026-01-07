package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentInlineTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentException;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.Image;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3AttachmentService implements AttachmentService {
  private final AttachmentRepository repository;
  private final AttachmentInlineRepository attachmentInlineRepository;
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
      AttachmentInlineRepository attachmentInlineRepository,
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

    var fileName = attachmentInlineDTO.getId() + "." + attachmentInlineDTO.getFormat();

    attachmentInlineDTO.setFilename(fileName);
    attachmentInlineDTO = attachmentInlineRepository.save(attachmentInlineDTO);
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
    return repository
        .findByDocumentationUnitIdAndFilename(documentationUnitId, imageName)
        .map(
            attachmentDTO ->
                Image.builder()
                    .content(attachmentDTO.getContent())
                    .contentType(attachmentDTO.getFormat())
                    .name(attachmentDTO.getFilename())
                    .build());
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
