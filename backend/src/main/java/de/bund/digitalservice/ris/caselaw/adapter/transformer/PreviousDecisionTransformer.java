package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;

public class PreviousDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static PreviousDecision transformToDomain(PreviousDecisionDTO previousDecisionDTO) {
    return PreviousDecision.builder()
        .uuid(previousDecisionDTO.getId())
        .documentNumber(previousDecisionDTO.getDocumentNumber())
        .referencedDocumentationUnitId(
            previousDecisionDTO.getReferencedDocumentationUnit() == null
                ? null
                : previousDecisionDTO.getReferencedDocumentationUnit().getId())
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
        .documentNumber(previousDecision.getDocumentNumber())
        .referencedDocumentationUnit(
            previousDecision.getReferencedDocumentationUnitId() == null
                ? null
                : DocumentationUnitDTO.builder()
                    .id(previousDecision.getReferencedDocumentationUnitId())
                    .build())
        .documentType(getDocumentTypeFromDomain(previousDecision.getDocumentType()))
        .fileNumber(getFileNumber(previousDecision.getFileNumber()))
        .dateKnown(previousDecision.isDateKnown())
        .build();
  }
}
