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
        // Todo dateKown?
        .build();
  }

  public static PreviousDecisionDTO transformToDTO(PreviousDecision previousDecision) {
    return PreviousDecisionDTO.builder()
        .court(getCourtFromDomain(previousDecision.getCourt()))
        .date(previousDecision.getDecisionDate())
        .documentNumber(previousDecision.getDocumentNumber())
        .documentType(getDocumentTypeFromDomain(previousDecision.getDocumentType()))
        .fileNumber(getFileNumber(previousDecision.getFileNumber()))
        .build();
  }
}
