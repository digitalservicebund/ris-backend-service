package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NormReferenceTransformer {
  private NormReferenceTransformer() {}

  public static NormReference transformToDomain(NormReferenceDTO normDTO) {
    List<SingleNorm> list = new ArrayList<>();
    SingleNorm singleNorm = SingleNormTransformer.transformToDomain(normDTO);

    if (singleNorm != null) {
      list.add(singleNorm);
    }

    return NormReference.builder()
        .normAbbreviation(
            NormAbbreviationTransformer.transformToDomain(normDTO.getNormAbbreviation()))
        .normAbbreviationRawValue(normDTO.getNormAbbreviationRawValue())
        .singleNorms(list)
        .build();
  }

  public static List<NormReferenceDTO> transformToDTO(
      NormReference normReference, boolean featureActive) {
    if (normReference == null) {
      return Collections.emptyList();
    }

    if (normReference.normAbbreviation() == null) {
      throw new DocumentUnitTransformerException(
          "Norm reference has no norm abbreviation, but is required.");
    }

    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder().id(normReference.normAbbreviation().id()).build();

    if (normReference.singleNorms() == null || normReference.singleNorms().isEmpty()) {
      return List.of(NormReferenceDTO.builder().normAbbreviation(normAbbreviationDTO).build());
    }

    return normReference.singleNorms().stream()
        .map(
            singleNorm -> {
              var builder =
                  NormReferenceDTO.builder()
                      .id(singleNorm.id())
                      .normAbbreviation(normAbbreviationDTO)
                      .singleNorm(singleNorm.singleNorm())
                      .dateOfVersion(singleNorm.dateOfVersion())
                      .dateOfRelevance(singleNorm.dateOfRelevance());

              if (featureActive && singleNorm.legalForce() != null) {
                builder.legalForce(LegalForceTransformer.transformToDTO(singleNorm.legalForce()));
              }

              return builder.build();
            })
        .toList();
  }
}
