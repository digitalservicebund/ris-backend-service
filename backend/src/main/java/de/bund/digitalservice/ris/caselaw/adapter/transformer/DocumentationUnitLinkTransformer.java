package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationUnitLinkDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLink;

public class DocumentationUnitLinkTransformer {
  private DocumentationUnitLinkTransformer() {}

  public static DocumentationUnitLink transferToDomain(
      DocumentationUnitLinkDTO documentationUnitLinkDTO) {
    return DocumentationUnitLink.builder()
        .parentDocumentationUnitUuid(documentationUnitLinkDTO.getParentDocumentationUnitUuid())
        .childDocumentationUnitUuid(documentationUnitLinkDTO.getChildDocumentationUnitUuid())
        .type(documentationUnitLinkDTO.getType())
        .build();
  }
}
