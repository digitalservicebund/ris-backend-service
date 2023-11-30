package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;

public class PreviousDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static PreviousDecision transformToDomain(PreviousDecisionDTO previousDecisionDTO) {
    return PreviousDecision.builder()
        .uuid(previousDecisionDTO.getId())
        .documentNumber(previousDecisionDTO.getReferencedDocumentationUnit().getDocumentNumber())
        .court(getCourtFromDTO(previousDecisionDTO.getCourt()))
        .fileNumber(getFileNumber(previousDecisionDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(previousDecisionDTO.getDocumentType()))
        .decisionDate(previousDecisionDTO.getDate())
        .dateKnown(previousDecisionDTO.isDateKnown())
        .build();
  }

  public static PreviousDecisionDTO transformToDTO(PreviousDecision previousDecision) {
    if (previousDecision.hasNoValues()) {
      return null;
    }

    return PreviousDecisionDTO.builder()
        .id(previousDecision.getUuid())
        .court(getCourtFromDomain(previousDecision.getCourt()))
        .date(previousDecision.getDecisionDate())
        .referencedDocumentationUnit(
            previousDecision.getDocumentNumber() == null
                ? null
                : DocumentationUnitDTO.builder()
                    .id(previousDecision.getUuid())
                    .documentNumber(previousDecision.getDocumentNumber())
                    .build())
        .documentType(getDocumentTypeFromDomain(previousDecision.getDocumentType()))
        .fileNumber(getFileNumber(previousDecision.getFileNumber()))
        .dateKnown(previousDecision.isDateKnown())
        .build();
  }
}
