package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;

public class ActiveCitationTransformer extends LinkedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return ActiveCitation.builder()
        .uuid(documentUnitMetadataDTO.getUuid())
        .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
        .dataSource(documentUnitMetadataDTO.getDataSource())
        .court(getCourt(documentUnitMetadataDTO))
        .fileNumber(getFileNumber(documentUnitMetadataDTO))
        .documentType(getDocumentTypeByDTO(documentUnitMetadataDTO.getDocumentTypeDTO()))
        .decisionDate(documentUnitMetadataDTO.getDecisionDate())
        .dateKnown(documentUnitMetadataDTO.isDateKnown())
        .build();
  }
}
