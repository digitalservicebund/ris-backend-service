package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AttachmentTransformer {

  public static Attachment transformToDomain(AttachmentDTO dto) {
    return Attachment.builder()
        .name(dto.getFilename())
        .extension(dto.getExtension())
        .s3path(dto.getS3ObjectPath())
        .uploadTimestamp(dto.getUploadTimestamp())
        .build();
  }
}
