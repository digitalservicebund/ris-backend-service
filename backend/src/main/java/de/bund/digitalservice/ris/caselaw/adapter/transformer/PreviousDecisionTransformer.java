package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;

public class PreviousDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static PreviousDecision transformToDomain(PreviousDecisionDTO previousDecisionDTO) {
    return PreviousDecision.builder()
        .uuid(previousDecisionDTO.getId())
        .documentNumber(previousDecisionDTO.getDocumentNumber())
        .court(getCourtFromDTO(previousDecisionDTO.getCourt()))
        .fileNumber(getFileNumber(previousDecisionDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(previousDecisionDTO.getDocumentType()))
        .decisionDate(previousDecisionDTO.getDate())
        // Todo dateKnown?
        .build();
  }

  public static PreviousDecisionDTO transformToDTO(
      PreviousDecision previousDecision, Integer rank) {
    if (previousDecision.hasNoValues()) {
      return null;
    }

    return PreviousDecisionDTO.builder()
        .court(getCourtFromDomain(previousDecision.getCourt()))
        .date(previousDecision.getDecisionDate())
        .documentNumber(previousDecision.getDocumentNumber())
        .documentType(getDocumentTypeFromDomain(previousDecision.getDocumentType()))
        .fileNumber(getFileNumber(previousDecision.getFileNumber()))
        .rank(rank)
        .build();
  }
}
