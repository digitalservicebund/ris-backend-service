package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.RegionDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;

public class RegionTransformer {
  private RegionTransformer() {}

  public static Region transformDTO(RegionDTO regionDTO) {
    return Region.builder().code(regionDTO.getCode()).label(regionDTO.getLabel()).build();
  }
}
