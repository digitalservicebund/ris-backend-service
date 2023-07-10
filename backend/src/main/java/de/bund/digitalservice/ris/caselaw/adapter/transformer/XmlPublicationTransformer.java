package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.XmlPublicationDTO;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import java.util.Arrays;
import java.util.UUID;

public class XmlPublicationTransformer {
  private XmlPublicationTransformer() {}

  public static XmlPublicationDTO transformToDTO(
      XmlPublication xmlPublication, Long documentUnitId) {
    return XmlPublicationDTO.builder()
        .documentUnitId(documentUnitId)
        .statusMessages(String.join("|", xmlPublication.statusMessages()))
        .statusCode(xmlPublication.getStatusCode())
        .xml(xmlPublication.xml())
        .receiverAddress(xmlPublication.receiverAddress())
        .publishDate(xmlPublication.getPublishDate())
        .publishState(xmlPublication.emailPublishState())
        .mailSubject(xmlPublication.mailSubject())
        .fileName(xmlPublication.fileName())
        .build();
  }

  public static XmlPublication transformToDomain(
      XmlPublicationDTO xmlPublicationDTO, UUID documentUnitUuid) {
    return XmlPublication.builder()
        .documentUnitUuid(documentUnitUuid)
        .statusMessages(Arrays.stream(xmlPublicationDTO.statusMessages().split("\\|")).toList())
        .statusCode(xmlPublicationDTO.statusCode())
        .xml(xmlPublicationDTO.xml())
        .receiverAddress(xmlPublicationDTO.receiverAddress())
        .publishDate(xmlPublicationDTO.publishDate())
        .emailPublishState(xmlPublicationDTO.publishState())
        .publishStateDisplayText(xmlPublicationDTO.publishState().getDisplayText())
        .mailSubject(xmlPublicationDTO.mailSubject())
        .fileName(xmlPublicationDTO.fileName())
        .build();
  }
}
