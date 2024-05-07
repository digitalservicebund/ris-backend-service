package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;

public class LegalForceTransformer {
  private LegalForceTransformer() {}

  public static LegalForceDTO transformDomain(LegalForce legalForce) {
    if (legalForce == null) {
      return null;
    }

    return LegalForceDTO.builder()
        .id(legalForce.id())
        .region(RegionDTO.builder().id(legalForce.region().id()).build())
        .legalForceType(LegalForceTypeDTO.builder().id(legalForce.type().id()).build())
        .build();
  }
}
