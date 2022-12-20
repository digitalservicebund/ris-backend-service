package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import java.time.Instant;

public class DeviatingDecisionDateTransformer {

  public static DeviatingDecisionDateDTO enrichDTO(
      DeviatingDecisionDateDTO deviatingDecisionDateDTO, Instant deviationDecisionDate) {

    return deviatingDecisionDateDTO.toBuilder().decisionDate(deviationDecisionDate).build();
  }
}
