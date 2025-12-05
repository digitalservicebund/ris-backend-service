package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedPendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.RelatedPendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;

public class RelatedPendingProceedingTransformer extends RelatedDocumentationUnitTransformer {
  public static RelatedPendingProceeding transformToDomain(
      RelatedPendingProceedingDTO relatedPendingProceedingDTO) {
    return RelatedPendingProceeding.builder()
        .uuid(relatedPendingProceedingDTO.getId())
        .documentNumber(relatedPendingProceedingDTO.getDocumentNumber())
        .court(getCourtFromDTO(relatedPendingProceedingDTO.getCourt()))
        .fileNumber(relatedPendingProceedingDTO.getFileNumber())
        .documentType(getDocumentTypeFromDTO(relatedPendingProceedingDTO.getDocumentType()))
        .decisionDate(relatedPendingProceedingDTO.getDate())
        .build();
  }

  public static RelatedPendingProceedingDTO transformToDTO(
      RelatedPendingProceeding relatedPendingProceeding) {
    if (relatedPendingProceeding.hasNoValues()) {
      return null;
    }

    return RelatedPendingProceedingDTO.builder()
        .id(relatedPendingProceeding.isNewEntry() ? null : relatedPendingProceeding.getUuid())
        .court(getCourtFromDomain(relatedPendingProceeding.getCourt()))
        .date(relatedPendingProceeding.getDecisionDate())
        .documentNumber(relatedPendingProceeding.getDocumentNumber())
        .documentType(getDocumentTypeFromDomain(relatedPendingProceeding.getDocumentType()))
        .fileNumber(StringUtils.normalizeSpace(relatedPendingProceeding.getFileNumber()))
        .build();
  }
}
