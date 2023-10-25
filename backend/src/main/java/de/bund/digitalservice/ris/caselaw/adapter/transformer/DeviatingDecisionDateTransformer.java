package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import java.time.LocalDate;

public class DeviatingDecisionDateTransformer {
  private DeviatingDecisionDateTransformer() {}

  public static DeviatingDecisionDateDTO enrichDTO(
      DeviatingDecisionDateDTO deviatingDecisionDateDTO, LocalDate deviationDecisionDate) {

    return deviatingDecisionDateDTO.toBuilder().decisionDate(deviationDecisionDate).build();
  }
}
