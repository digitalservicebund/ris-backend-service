package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;

public class ProceedingDecisionTransformer extends LinkedDocumentationUnitTransformer {
  public static ProceedingDecision transformToDomain(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return ProceedingDecision.builder()
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
