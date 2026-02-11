package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for inline attachments (images). */
public interface AttachmentInlineRepository {

  List<AttachmentInline> findAllByDocumentationUnitId(UUID documentationUnitId);

  Optional<AttachmentInline> findByDocumentationUnitIdAndFilename(
      UUID documentationUnitId, String fileName);
}
