package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.XmlPublicationDTO;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import java.util.Arrays;
import java.util.UUID;

public class XmlPublicationTransformer {
  private XmlPublicationTransformer() {}

  public static XmlPublicationDTO transformToDTO(
      XmlPublication xmlPublication, UUID documentUnitId) {
    return XmlPublicationDTO.builder()
        .documentUnitId(documentUnitId)
        .statusMessages(String.join("|", xmlPublication.statusMessages()))
        .statusCode(xmlPublication.getStatusCode())
        .xml(xmlPublication.xml())
        .receiverAddress(xmlPublication.receiverAddress())
        .publishDate(xmlPublication.getPublishDate())
        .mailSubject(xmlPublication.mailSubject())
        .fileName(xmlPublication.fileName())
        .build();
  }

  public static XmlPublication transformToDomain(
      XmlPublicationDTO xmlPublicationDTO, UUID documentUnitUuid) {
    return XmlPublication.builder()
        .documentUnitUuid(documentUnitUuid)
        .statusMessages(Arrays.stream(xmlPublicationDTO.getStatusMessages().split("\\|")).toList())
        .statusCode(xmlPublicationDTO.getStatusCode())
        .xml(xmlPublicationDTO.getXml())
        .receiverAddress(xmlPublicationDTO.getReceiverAddress())
        .publishDate(xmlPublicationDTO.getPublishDate())
        .mailSubject(xmlPublicationDTO.getMailSubject())
        .fileName(xmlPublicationDTO.getFileName())
        .build();
  }
}
