package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;

/**
 * Utility class for transforming LegalForceType objects between DTOs (Data Transfer Objects) and
 * domain objects.
 */
public class LegalForceTypeTransformer {

  private LegalForceTypeTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a LegalForceTypeDTO (Data Transfer Object) into a LegalForceType domain object.
   *
   * @param dto The LegalForceTypeDTO to be transformed.
   * @return The LegalForceType domain object representing the transformed LegalForceTypeDTO.
   */
  public static LegalForceType transformToDomain(LegalForceTypeDTO dto) {
    if (dto == null) {
      return null;
    }

    return LegalForceType.builder()
        .id(dto.getId())
        .abbreviation(dto.getAbbreviation())
        .label(dto.getLabel())
        .build();
  }
}
