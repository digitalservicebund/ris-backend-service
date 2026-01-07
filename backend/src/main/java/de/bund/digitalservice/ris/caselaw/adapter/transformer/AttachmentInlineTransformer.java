package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;

public class AttachmentInlineTransformer {

  public static Attachment transformToDomain(AttachmentInlineDTO dto) {
    return Attachment.builder()
        .name(dto.getFilename())
        .format(dto.getFormat())
        .s3path(null)
        .uploadTimestamp(dto.getUploadTimestamp())
        .build();
  }
}
