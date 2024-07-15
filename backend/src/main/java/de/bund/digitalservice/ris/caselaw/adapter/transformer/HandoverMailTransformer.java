package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailDTO;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import java.util.Arrays;
import java.util.UUID;

public class HandoverMailTransformer {
  private HandoverMailTransformer() {}

  public static HandoverMailDTO transformToDTO(HandoverMail xmlPublication, UUID documentUnitId) {
    return HandoverMailDTO.builder()
        .documentUnitId(documentUnitId)
        .statusMessages(String.join("|", xmlPublication.statusMessages()))
        .statusCode(xmlPublication.success() ? "200" : "400")
        .xml(xmlPublication.xml())
        .receiverAddress(xmlPublication.receiverAddress())
        .sentDate(xmlPublication.getHandoverDate())
        .mailSubject(xmlPublication.mailSubject())
        .fileName(xmlPublication.fileName())
        .issuerAddress(xmlPublication.issuerAddress())
        .build();
  }

  public static HandoverMail transformToDomain(
      HandoverMailDTO handoverMailDTO, UUID documentUnitUuid) {
    return HandoverMail.builder()
        .documentUnitUuid(documentUnitUuid)
        .statusMessages(Arrays.stream(handoverMailDTO.getStatusMessages().split("\\|")).toList())
        .success(handoverMailDTO.getStatusCode().equals("200"))
        .xml(handoverMailDTO.getXml())
        .receiverAddress(handoverMailDTO.getReceiverAddress())
        .handoverDate(handoverMailDTO.getSentDate())
        .mailSubject(handoverMailDTO.getMailSubject())
        .fileName(handoverMailDTO.getFileName())
        .issuerAddress(handoverMailDTO.getIssuerAddress())
        .build();
  }
}
