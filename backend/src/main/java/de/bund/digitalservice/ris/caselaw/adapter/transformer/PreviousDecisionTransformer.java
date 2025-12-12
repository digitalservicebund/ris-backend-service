package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;

public class PreviousDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static PreviousDecision transformToDomain(PreviousDecisionDTO previousDecisionDTO) {
    return PreviousDecision.builder()
        .uuid(previousDecisionDTO.getId())
        .documentNumber(previousDecisionDTO.getDocumentNumber())
        .court(getCourtFromDTO(previousDecisionDTO.getCourt()))
        .fileNumber(previousDecisionDTO.getFileNumber())
        .documentType(getDocumentTypeFromDTO(previousDecisionDTO.getDocumentType()))
        .deviatingFileNumber(previousDecisionDTO.getDeviatingFileNumber())
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
        .documentType(getDocumentTypeFromDomain(previousDecision.getDocumentType()))
        .fileNumber(StringUtils.normalizeSpace(previousDecision.getFileNumber()))
        .deviatingFileNumber(StringUtils.normalizeSpace(previousDecision.getDeviatingFileNumber()))
        .dateKnown(previousDecision.isDateKnown())
        .build();
  }
}
