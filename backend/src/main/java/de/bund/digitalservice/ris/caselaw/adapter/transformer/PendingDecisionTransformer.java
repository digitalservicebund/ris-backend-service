package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;

public class PendingDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static EnsuingDecision transformToDomain(PendingDecisionDTO pendingDecisionDTO) {
    return EnsuingDecision.builder()
        .uuid(pendingDecisionDTO.getId())
        .documentNumber(pendingDecisionDTO.getReferencedDocumentationUnit().getDocumentNumber())
        .court(getCourtFromDTO(pendingDecisionDTO.getCourt()))
        .fileNumber(getFileNumber(pendingDecisionDTO.getFileNumber()))
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
        .referencedDocumentationUnit(
            ensuingDecision.getDocumentNumber() == null
                ? null
                : DocumentationUnitDTO.builder()
                    .documentNumber(ensuingDecision.getDocumentNumber())
                    .build())
        .documentType(getDocumentTypeFromDomain(ensuingDecision.getDocumentType()))
        .fileNumber(getFileNumber(ensuingDecision.getFileNumber()))
        .note(ensuingDecision.getNote())
        .build();
  }
}
