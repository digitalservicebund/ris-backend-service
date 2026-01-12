package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.domain.HandoverService.prettifyXml;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailAttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailDTO;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachmentImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class HandoverMailTransformer {
  private HandoverMailTransformer() {}

  public static HandoverMailDTO transformToDTO(HandoverMail handoverMail) {
    var attachedImages =
        handoverMail.imageAttachments().stream().map(MailAttachmentImage::fileName).toList();
    var mail =
        HandoverMailDTO.builder()
            .entityId(handoverMail.entityId())
            .statusMessages(String.join("|", handoverMail.statusMessages()))
            .statusCode(handoverMail.success() ? "200" : "400")
            .receiverAddress(handoverMail.receiverAddress())
            .sentDate(handoverMail.getHandoverDate())
            .mailSubject(handoverMail.mailSubject())
            .issuerAddress(handoverMail.issuerAddress())
            .attachedImages(String.join("|", attachedImages))
            .attachments(new ArrayList<>())
            .build();

    for (MailAttachment attachment : handoverMail.attachments()) {
      mail.addAttachment(
          HandoverMailAttachmentDTO.builder()
              .xml(attachment.fileContent())
              .fileName(attachment.fileName())
              .build());
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
                .map(
                    attachmentDTO -> {
                      String fileContent;

                      try {
                        if (entityType == HandoverEntityType.DOCUMENTATION_UNIT) {
                          fileContent = prettifyXml(attachmentDTO.getXml());
                        } else {
                          fileContent = attachmentDTO.getXml();
                        }
                      } catch (Exception e) {
                        fileContent = attachmentDTO.getXml();
                      }
                      return MailAttachment.builder()
                          .fileContent(fileContent)
                          .fileName(attachmentDTO.getFileName())
                          .build();
                    })
                .toList())
        .imageAttachments(
            handoverMailDTO.getAttachedImages() == null
                    || handoverMailDTO.getAttachedImages().isBlank()
                ? Collections.emptyList()
                : Arrays.stream(handoverMailDTO.getAttachedImages().split("\\|"))
                    .map(
                        attachedImageFileName ->
                            MailAttachmentImage.builder().fileName(attachedImageFileName).build())
                    .toList())
        .build();
  }
}
