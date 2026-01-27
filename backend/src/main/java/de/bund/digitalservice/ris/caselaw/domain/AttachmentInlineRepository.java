package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for inline attachments (images). */
public interface AttachmentInlineRepository {

  List<Attachment> findAllByDocumentationUnitId(UUID documentationUnitId);

  Optional<Attachment> findByDocumentationUnitIdAndFilename(
      UUID documentationUnitId, String fileName);
}
