package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;

public class PendingDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static EnsuingDecision transformToDomain(PendingDecisionDTO pendingDecisionDTO) {
    return EnsuingDecision.builder()
        .uuid(pendingDecisionDTO.getId())
        .documentNumber(pendingDecisionDTO.getDocumentNumber())
        .court(getCourtFromDTO(pendingDecisionDTO.getCourt()))
        .fileNumber(pendingDecisionDTO.getFileNumber())
        .documentType(getDocumentTypeFromDTO(pendingDecisionDTO.getDocumentType()))
        .decisionDate(pendingDecisionDTO.getDate())
        .note(pendingDecisionDTO.getNote())
        .pending(true)
        .build();
  }

  public static PendingDecisionDTO transformToDTO(EnsuingDecision ensuingDecision) {
    if (ensuingDecision.hasNoValues()) {
      return null;
    }

    return PendingDecisionDTO.builder()
        .id(ensuingDecision.getUuid())
        .court(getCourtFromDomain(ensuingDecision.getCourt()))
        .date(ensuingDecision.getDecisionDate())
        .documentNumber(ensuingDecision.getDocumentNumber())
        .documentType(getDocumentTypeFromDomain(ensuingDecision.getDocumentType()))
        .fileNumber(StringUtils.normalizeSpace(ensuingDecision.getFileNumber()))
        .note(ensuingDecision.getNote())
        .build();
  }
}
