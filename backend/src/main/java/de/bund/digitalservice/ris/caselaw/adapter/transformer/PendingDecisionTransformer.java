package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;

public class PendingDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static EnsuingDecision transformToDomain(PendingDecisionDTO pendingDecisionDTO) {
    return EnsuingDecision.builder()
        .uuid(pendingDecisionDTO.getId())
        .documentNumber(pendingDecisionDTO.getDocumentNumber())
        .court(getCourtFromDTO(pendingDecisionDTO.getCourt()))
        .fileNumber(getFileNumber(pendingDecisionDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(pendingDecisionDTO.getDocumentType()))
        .decisionDate(pendingDecisionDTO.getDate())
        .note(pendingDecisionDTO.getNote())
        .isPending(true)
        .build();
  }

  public static PendingDecisionDTO transformToDTO(EnsuingDecision ensuingDecision, Integer rank) {
    if (ensuingDecision.hasNoValues()) {
      return null;
    }

    return PendingDecisionDTO.builder()
        .id(ensuingDecision.getUuid())
        .court(getCourtFromDomain(ensuingDecision.getCourt()))
        .date(ensuingDecision.getDecisionDate())
        .documentNumber(ensuingDecision.getDocumentNumber())
        .documentType(getDocumentTypeFromDomain(ensuingDecision.getDocumentType()))
        .fileNumber(getFileNumber(ensuingDecision.getFileNumber()))
        .note(ensuingDecision.getNote())
        .rank(rank)
        .build();
  }
}
