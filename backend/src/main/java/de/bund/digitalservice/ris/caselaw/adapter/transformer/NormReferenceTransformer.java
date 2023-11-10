package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;

public class NormReferenceTransformer {
  private NormReferenceTransformer() {}

  public static NormReference transformToDomain(NormReferenceDTO normDTO) {
    return NormReference.builder()
        .id(normDTO.getId())
        .normAbbreviation(NormAbbreviationTransformer.transformDTO(normDTO.getNormAbbreviation()))
        .singleNorm(normDTO.getSingleNorm())
        .dateOfVersion(normDTO.getDateOfVersion())
        .dateOfRelevance(normDTO.getDateOfRelevance())
        .build();
  }

  public static NormReferenceDTO transformToDTO(NormReference normReference) {
    if (normReference == null) {
      return null;
    }
    return NormReferenceDTO.builder()
        .id(normReference.id())
        .normAbbreviation(
            normReference.normAbbreviation() != null
                ? NormAbbreviationDTO.builder().id(normReference.normAbbreviation().id()).build()
                : null)
        .singleNorm(normReference.singleNorm())
        .dateOfVersion(normReference.dateOfVersion())
        .dateOfRelevance(normReference.dateOfRelevance())
        .build();
  }
}
