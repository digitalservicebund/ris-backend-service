package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;

public class RegionTransformer {
  private RegionTransformer() {}

  public static Region transformDTO(RegionDTO regionDTO) {
    if (regionDTO == null) {
      return null;
    }

    return Region.builder()
        .id(regionDTO.getId())
        .code(regionDTO.getCode())
        .longText(regionDTO.getLongText())
        .build();
  }
}
