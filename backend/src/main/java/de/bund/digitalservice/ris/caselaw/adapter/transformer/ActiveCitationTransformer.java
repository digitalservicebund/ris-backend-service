package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationUnitLinkDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveCitationTransformer extends LinkedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(
      DocumentUnitMetadataDTO documentUnitMetadataDTO, DocumentationUnitLinkDTO linkDTO) {

    if (log.isDebugEnabled()) {
      log.debug(
          "transform '{}' to active citation domain object", documentUnitMetadataDTO.getUuid());
    }

    CitationStyle citationStyle = null;
    if (linkDTO != null && linkDTO.getCitationStyleDTO() != null) {
      citationStyle =
          CitationStyle.builder()
              .uuid(linkDTO.getCitationStyleDTO().getUuid())
              .jurisShortcut(linkDTO.getCitationStyleDTO().getJurisShortcut())
              .citationDocumentType(linkDTO.getCitationStyleDTO().getCitationDocumentType())
              .label(linkDTO.getCitationStyleDTO().getLabel())
              .documentType(linkDTO.getCitationStyleDTO().getDocumentType())
              .build();
    }

    return ActiveCitation.builder()
        .uuid(documentUnitMetadataDTO.getUuid())
        .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
        .dataSource(documentUnitMetadataDTO.getDataSource())
        .court(getCourt(documentUnitMetadataDTO))
        .fileNumber(getFileNumber(documentUnitMetadataDTO))
        .documentType(getDocumentTypeByDTO(documentUnitMetadataDTO.getDocumentTypeDTO()))
        .decisionDate(documentUnitMetadataDTO.getDecisionDate())
        .dateKnown(documentUnitMetadataDTO.isDateKnown())
        .citationStyle(citationStyle)
        .build();
  }
}
