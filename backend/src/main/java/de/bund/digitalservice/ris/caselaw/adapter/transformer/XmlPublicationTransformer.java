package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.XmlHandoverMailDTO;
import de.bund.digitalservice.ris.caselaw.domain.XmlHandoverMail;
import java.util.Arrays;
import java.util.UUID;

public class XmlPublicationTransformer {
  private XmlPublicationTransformer() {}

  public static XmlHandoverMailDTO transformToDTO(
      XmlHandoverMail xmlPublication, UUID documentUnitId) {
    return XmlHandoverMailDTO.builder()
        .documentUnitId(documentUnitId)
        .statusMessages(String.join("|", xmlPublication.statusMessages()))
        .statusCode(xmlPublication.success() ? "200" : "400")
        .xml(xmlPublication.xml())
        .receiverAddress(xmlPublication.receiverAddress())
        .createdDate(xmlPublication.getHandoverDate())
        .mailSubject(xmlPublication.mailSubject())
        .fileName(xmlPublication.fileName())
        .issuerAddress(xmlPublication.issuerAddress())
        .build();
  }

  public static XmlHandoverMail transformToDomain(
      XmlHandoverMailDTO xmlHandoverMailDTO, UUID documentUnitUuid) {
    return XmlHandoverMail.builder()
        .documentUnitUuid(documentUnitUuid)
        .statusMessages(Arrays.stream(xmlHandoverMailDTO.getStatusMessages().split("\\|")).toList())
        .success(xmlHandoverMailDTO.getStatusCode().equals("200"))
        .xml(xmlHandoverMailDTO.getXml())
        .receiverAddress(xmlHandoverMailDTO.getReceiverAddress())
        .handoverDate(xmlHandoverMailDTO.getCreatedDate())
        .mailSubject(xmlHandoverMailDTO.getMailSubject())
        .fileName(xmlHandoverMailDTO.getFileName())
        .issuerAddress(xmlHandoverMailDTO.getIssuerAddress())
        .build();
  }
}
