package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailDTO;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class HandoverMailTransformer {
  private HandoverMailTransformer() {}

  public static HandoverMailDTO transformToDTO(HandoverMail xmlPublication, UUID entityId) {
    var mail =
        HandoverMailDTO.builder()
            .entityId(entityId)
            .statusMessages(String.join("|", xmlPublication.statusMessages()))
            .statusCode(xmlPublication.success() ? "200" : "400")
            .receiverAddress(xmlPublication.receiverAddress())
            .sentDate(xmlPublication.getHandoverDate())
            .mailSubject(xmlPublication.mailSubject())
            .issuerAddress(xmlPublication.issuerAddress())
            .attachments(new ArrayList<>())
            .build();

    for (MailAttachment mailAttachment : xmlPublication.attachments()) {
      mail.addAttachment(HandoverMailAttachmentTransformer.transformToDTO(mailAttachment));
    }
    return mail;
  }

  public static HandoverMail transformToDomain(
      HandoverMailDTO handoverMailDTO, UUID entityId, HandoverEntityType entityType) {
    return HandoverMail.builder()
        .entityId(entityId)
        .entityType(entityType)
        .statusMessages(Arrays.stream(handoverMailDTO.getStatusMessages().split("\\|")).toList())
        .success(handoverMailDTO.getStatusCode().equals("200"))
        .receiverAddress(handoverMailDTO.getReceiverAddress())
        .handoverDate(handoverMailDTO.getSentDate())
        .mailSubject(handoverMailDTO.getMailSubject())
        .issuerAddress(handoverMailDTO.getIssuerAddress())
        .attachments(
            handoverMailDTO.getAttachments().stream()
                .map(HandoverMailAttachmentTransformer::transformToDomain)
                .toList())
        .build();
  }
}
