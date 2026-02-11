package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentInline;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AttachmentInlineTransformer {

  public static AttachmentInline transformToDomain(AttachmentInlineDTO dto) {
    return AttachmentInline.builder()
        .name(dto.getFilename())
        .format(dto.getFormat())
        .uploadTimestamp(dto.getUploadTimestamp())
        .build();
  }
}
