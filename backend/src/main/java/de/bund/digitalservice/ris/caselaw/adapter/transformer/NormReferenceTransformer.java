package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import java.util.Collections;
import java.util.List;

public class NormReferenceTransformer {
  private NormReferenceTransformer() {}

  public static NormReference transformToDomain(NormReferenceDTO normDTO) {
    return NormReference.builder()
        .id(normDTO.getId())
        .normAbbreviation(NormAbbreviationTransformer.transformDTO(normDTO.getNormAbbreviation()))
        .normAbbreviationRawValue(normDTO.getNormAbbreviationRawValue())
        .singleNorms(List.of(SingleNormTransformer.transformToDomain(normDTO)))
        .build();
  }

  public static List<NormReferenceDTO> transformToDTO(NormReference normReference) {
    if (normReference == null) {
      return Collections.emptyList();
    }

    NormAbbreviationDTO normAbbreviationDTO =
        normReference.normAbbreviation() != null
            ? NormAbbreviationDTO.builder().id(normReference.normAbbreviation().id()).build()
            : null;

    if (normReference.singleNorms() == null) {
      return List.of(
          NormReferenceDTO.builder()
              .id(normReference.id())
              .normAbbreviation(normAbbreviationDTO)
              .build());
    }

    return normReference.singleNorms().stream()
        .map(
            singleNorm ->
                NormReferenceDTO.builder()
                    .id(normReference.id())
                    .normAbbreviation(normAbbreviationDTO)
                    .singleNorm(singleNorm.singleNorm())
                    .dateOfVersion(singleNorm.dateOfVersion())
                    .dateOfRelevance(singleNorm.dateOfRelevance())
                    .build())
        .toList();
  }
}
