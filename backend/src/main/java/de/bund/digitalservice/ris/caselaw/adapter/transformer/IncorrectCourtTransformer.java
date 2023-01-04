package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;

public class IncorrectCourtTransformer {

  public static IncorrectCourtDTO enrichDTO(
      IncorrectCourtDTO incorrectCourtDTO, String incorrectCourt) {

    return incorrectCourtDTO.toBuilder().court(incorrectCourt).build();
  }
}
