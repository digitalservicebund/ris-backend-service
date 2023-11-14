package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IncorrectCourtTransformer {

  public IncorrectCourtDTO enrichDTO(IncorrectCourtDTO incorrectCourtDTO, String incorrectCourt) {

    return incorrectCourtDTO.toBuilder().court(incorrectCourt).build();
  }
}
