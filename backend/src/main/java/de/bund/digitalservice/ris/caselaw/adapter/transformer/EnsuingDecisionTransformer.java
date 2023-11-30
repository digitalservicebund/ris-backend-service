package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;

public class EnsuingDecisionTransformer extends RelatedDocumentationUnitTransformer {
  public static EnsuingDecision transformToDomain(EnsuingDecisionDTO ensuingDecisionDTO) {
    return EnsuingDecision.builder()
        .uuid(ensuingDecisionDTO.getId())
        .documentNumber(ensuingDecisionDTO.getReferencedDocumentationUnit().getDocumentNumber())
        //        .referencedDocumentationUnit(
        //            ensuingDecisionDTO.getReferencedDocumentationUnit() == null
        //                ? null
        //                : ensuingDecisionDTO.getReferencedDocumentationUnit().getId())
        .court(getCourtFromDTO(ensuingDecisionDTO.getCourt()))
        .fileNumber(getFileNumber(ensuingDecisionDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(ensuingDecisionDTO.getDocumentType()))
        .decisionDate(ensuingDecisionDTO.getDate())
        .note(ensuingDecisionDTO.getNote())
        .pending(false)
        .build();
  }

  public static EnsuingDecisionDTO transformToDTO(EnsuingDecision ensuingDecision) {
    if (ensuingDecision.hasNoValues()) {
      return null;
    }
    return EnsuingDecisionDTO.builder()
        .id(ensuingDecision.getUuid())
        .court(getCourtFromDomain(ensuingDecision.getCourt()))
        .date(ensuingDecision.getDecisionDate())
        //        .documentNumber(ensuingDecision.getDocumentNumber())
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
