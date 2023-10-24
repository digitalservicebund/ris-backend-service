package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingCourt;

public class DeviatingCourtTransformer {

  public static IncorrectCourtDTO enrichDTO(
      IncorrectCourtDTO incorrectCourtDTO, String incorrectCourt) {

    return incorrectCourtDTO.toBuilder().court(incorrectCourt).build();
  }

  public static DeviatingCourtDTO transformToDTO(DeviatingCourt deviatingCourt, Long rank) {
    return DeviatingCourtDTO.builder().value(deviatingCourt.court()).rank(rank).build();
  }

  public static DeviatingCourt transformToDomain(DeviatingCourtDTO deviatingCourtDTO) {
    return DeviatingCourt.builder().court(deviatingCourtDTO.getValue()).build();
  }
}
