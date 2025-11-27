package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtBranchLocationDTO;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtBranchLocation;

public class CourtBranchLocationTransformer {

  private CourtBranchLocationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  public static CourtBranchLocationDTO transformToDTO(CourtBranchLocation courtBranchLocation) {
    if (courtBranchLocation == null) {
      return null;
    }
    return CourtBranchLocationDTO.builder()
        .id(courtBranchLocation.id())
        .value(courtBranchLocation.value())
        .build();
  }

  public static CourtBranchLocation transformToDomain(
      CourtBranchLocationDTO courtBranchLocationDTO) {
    if (courtBranchLocationDTO == null) {
      return null;
    }
    return CourtBranchLocation.builder()
        .id(courtBranchLocationDTO.getId())
        .value(courtBranchLocationDTO.getValue())
        .build();
  }
}
