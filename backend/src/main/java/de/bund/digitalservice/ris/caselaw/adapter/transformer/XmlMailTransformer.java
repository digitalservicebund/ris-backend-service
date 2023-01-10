package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.XmlMailDTO;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import java.util.Arrays;
import java.util.UUID;

public class XmlMailTransformer {
  private XmlMailTransformer() {}

  public static XmlMailDTO transformToDTO(XmlMail xmlMail, Long documentUnitId) {
    return XmlMailDTO.builder()
        .documentUnitId(documentUnitId)
        .statusMessages(String.join("|", xmlMail.statusMessages()))
        .statusCode(xmlMail.statusCode())
        .xml(xmlMail.xml())
        .receiverAddress(xmlMail.receiverAddress())
        .publishDate(xmlMail.publishDate())
        .publishState(xmlMail.publishState())
        .mailSubject(xmlMail.mailSubject())
        .fileName(xmlMail.fileName())
        .build();
  }

  public static XmlMail transformToDomain(XmlMailDTO xmlMailDTO, UUID documentUnitUuid) {
    return XmlMail.builder()
        .documentUnitUuid(documentUnitUuid)
        .statusMessages(Arrays.stream(xmlMailDTO.statusMessages().split("\\|")).toList())
        .statusCode(xmlMailDTO.statusCode())
        .xml(xmlMailDTO.xml())
        .receiverAddress(xmlMailDTO.receiverAddress())
        .publishDate(xmlMailDTO.publishDate())
        .publishState(xmlMailDTO.publishState())
        .mailSubject(xmlMailDTO.mailSubject())
        .fileName(xmlMailDTO.fileName())
        .build();
  }
}
