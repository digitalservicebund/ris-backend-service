package de.bund.digitalservice.ris.caselaw.domain;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public interface AttachmentService {
  Attachment attachFileToDocumentationUnit(
      UUID documentationUnitId, ByteBuffer byteBuffer, HttpHeaders httpHeaders, User user);

  Attachment attachFileToDocumentationUnit(UUID documentationUnitId, InputStream file, User user);

  void deleteByS3Path(String s3Path, UUID documentationUnitId, User user);

  void deleteAllObjectsFromBucketForDocumentationUnit(UUID documentationUnitId);

  Optional<Image> findByDocumentationUnitIdAndFileName(UUID documentationUnitId, String imageName);
}
