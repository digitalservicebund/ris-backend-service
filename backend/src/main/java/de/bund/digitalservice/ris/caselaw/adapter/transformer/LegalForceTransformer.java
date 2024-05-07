package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;

public class LegalForceTransformer {
  private LegalForceTransformer() {}

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
