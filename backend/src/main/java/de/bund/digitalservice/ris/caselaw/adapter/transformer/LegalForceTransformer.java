package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;

/**
 * Utility class for transforming LegalForce objects between DTOs (Data Transfer Objects) and domain
 * objects.
 */
public class LegalForceTransformer {

  private LegalForceTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a LegalForce domain object into it's database representation, a LegalForceDTO (Data
   * Transfer Object) .
   *
   * @param legalForce The representation of the legal force information of a norm reference.
   * @return A database representation of a legal force domain object.
   */
  public static LegalForceDTO transformToDTO(LegalForce legalForce) {
    if (legalForce == null) {
      return null;
    }

    LegalForceDTO.LegalForceDTOBuilder builder = LegalForceDTO.builder().id(legalForce.id());

    if (legalForce.region() != null) {
      builder.region(RegionDTO.builder().id(legalForce.region().id()).build());
    }

    if (legalForce.type() != null) {
      builder.legalForceType(LegalForceTypeDTO.builder().id(legalForce.type().id()).build());
    }

    return builder.build();
  }

  /**
   * Transforms a LegalForceDTO (Data Transfer Object) into a LegalForce domain object.
   *
   * @param dto A database representation of a legal force domain object, to be transformed.
   * @return The representation of the legal force information of a norm reference.
   */
  public static LegalForce transformToDomain(LegalForceDTO dto) {
    if (dto == null) {
      return null;
    }

    return new LegalForce(
        dto.getId(),
        LegalForceTypeTransformer.transformToDomain(dto.getLegalForceType()),
        RegionTransformer.transformDTO(dto.getRegion()));
  }
}
