package de.bund.digitalservice.ris.caselaw.domain;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public interface AttachmentService {
  Attachment attachFileToDocumentationUnit(
      UUID documentUnitUuid, ByteBuffer byteBuffer, HttpHeaders httpHeaders);

  void deleteByS3path(String s3path);

  void deleteAllObjectsFromBucketForDocumentationUnit(UUID documentationUnitId);
}
