package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AttachmentTransformer {

  public static Attachment transformToDomain(AttachmentDTO dto) {
    return Attachment.builder()
        .id(dto.getId())
        .name(dto.getFilename())
        .format(dto.getFormat())
        .s3path(dto.getS3ObjectPath())
        .uploadTimestamp(dto.getUploadTimestamp())
        .type(AttachmentType.valueOf(dto.getAttachmentType()))
        .build();
  }
}
