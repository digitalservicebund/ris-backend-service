package de.bund.digitalservice.ris.caselaw.domain;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public interface AttachmentService {
  Attachment attachFileToDocumentationUnit(
      UUID documentationUnitId, ByteBuffer byteBuffer, HttpHeaders httpHeaders, User user);

  Attachment streamFileToDocumentationUnit(
      UUID documentationUnitId, InputStream file, String filename, User user, AttachmentType type);

  void deleteByFileId(UUID fileId, UUID documentationUnitId, User user);

  void deleteAllObjectsFromBucketForDocumentationUnit(UUID documentationUnitId);

  Optional<Image> findByDocumentationUnitIdAndFileName(UUID documentationUnitId, String imageName);

  StreamedFileResponse getFileStream(UUID documentationUnitId, UUID fileUuid);
}
