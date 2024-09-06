package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailAttachmentDTO;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;

public class HandoverMailAttachmentTransformer {
  private HandoverMailAttachmentTransformer() {}

  public static HandoverMailAttachmentDTO transformToDTO(MailAttachment attachment) {
    return HandoverMailAttachmentDTO.builder()
        .xml(attachment.fileContent())
        .fileName(attachment.fileName())
        .build();
  }

  public static MailAttachment transformToDomain(HandoverMailAttachmentDTO attachmentDTO) {
    return MailAttachment.builder()
        .fileContent(attachmentDTO.getXml())
        .fileName(attachmentDTO.getFileName())
        .build();
  }
}
