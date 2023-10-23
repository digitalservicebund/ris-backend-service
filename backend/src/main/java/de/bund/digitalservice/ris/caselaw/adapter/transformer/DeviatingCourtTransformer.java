package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingCourt;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeviatingCourtTransformer {

  public IncorrectCourtDTO enrichDTO(IncorrectCourtDTO incorrectCourtDTO, String incorrectCourt) {

    return incorrectCourtDTO.toBuilder().court(incorrectCourt).build();
  }

  public static DeviatingCourtDTO transformToDTO(DeviatingCourt deviatingCourt) {
    return DeviatingCourtDTO.builder()
        .id(deviatingCourt.id())
        .value(deviatingCourt.court())
        .build();
  }

  public static DeviatingCourt transformToDomain(DeviatingCourtDTO deviatingCourtDTO) {
    return DeviatingCourt.builder()
        .id(deviatingCourtDTO.getId())
        .court(deviatingCourtDTO.getValue())
        .build();
  }
}
