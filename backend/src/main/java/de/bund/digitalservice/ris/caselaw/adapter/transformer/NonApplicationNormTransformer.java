package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NonApplicationNormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.domain.NonApplicationNorm;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for transforming NonApplicationNorm objects between database representation and
 * domain object.
 */
public class NonApplicationNormTransformer {

  private NonApplicationNormTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a NonApplicationNormDTO (database representation) into a {@link NonApplicationNorm}
   * domain object, before all NonApplicationNorm with the same normAbbreviation are being grouped
   * into one NonApplicationNorm object in the {@link DecisionTransformer}
   *
   * @param nonApplicationNormDTO The NonApplicationNormDTO to be transformed.
   * @return The NonApplicationNorm domain object representing the transformed
   *     NonApplicationNormDTO.
   */
  public static NonApplicationNorm transformToDomain(NonApplicationNormDTO nonApplicationNormDTO) {
    List<SingleNorm> list = new ArrayList<>();
    SingleNorm singleNorm = SingleNormTransformer.transformToDomain(nonApplicationNormDTO);

    if (singleNorm != null) {
      list.add(singleNorm);
    }

    return NonApplicationNorm.builder()
        .normAbbreviation(
            NormAbbreviationTransformer.transformToDomain(
                nonApplicationNormDTO.getNormAbbreviation()))
        .singleNorms(list)
        .build();
  }

  /**
   * Transforms a NonApplicationNorm domain object into a list of NonApplicationNormDTO (database
   * representation). When converting into a DTO object, each single norm in a nonApplicationNorm is
   * converted into its own {@link NonApplicationNormDTO}.
   *
   * @param nonApplicationNorm The NonApplicationNorm object to be transformed.
   * @return A list of NonApplicationNormDTOs representing the transformed NonApplicationNorm
   *     object.
   */
  public static List<NonApplicationNormDTO> transformToDTO(NonApplicationNorm nonApplicationNorm) {
    if (nonApplicationNorm == null) {
      return Collections.emptyList();
    }

    if (nonApplicationNorm.normAbbreviation() == null) {
      throw new DocumentationUnitTransformerException(
          "Norm reference has no norm abbreviation, but is required.");
    }

    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder().id(nonApplicationNorm.normAbbreviation().id()).build();

    if (nonApplicationNorm.singleNorms() == null || nonApplicationNorm.singleNorms().isEmpty()) {
      return List.of(NonApplicationNormDTO.builder().normAbbreviation(normAbbreviationDTO).build());
    }

    return nonApplicationNorm.singleNorms().stream()
        .distinct()
        .map(
            singleNorm -> {
              var builder =
                  NonApplicationNormDTO.builder()
                      .id(singleNorm.id())
                      .normAbbreviation(normAbbreviationDTO)
                      .singleNorm(StringUtils.normalizeSpace(singleNorm.singleNorm()))
                      .dateOfVersion(singleNorm.dateOfVersion())
                      .dateOfRelevance(singleNorm.dateOfRelevance());
              return builder.build();
            })
        .toList();
  }
}
