package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingDecisionDate;
import java.time.Instant;
import java.time.LocalDate;

public class DeviatingDecisionDateTransformer {
  private DeviatingDecisionDateTransformer() {}

  public static DeviatingDecisionDateDTO enrichDTO(
      DeviatingDecisionDateDTO deviatingDecisionDateDTO, Instant deviationDecisionDate) {

    return deviatingDecisionDateDTO.toBuilder().decisionDate(deviationDecisionDate).build();
  }

  public static DeviatingDateDTO transformToDTO(DeviatingDecisionDate deviatingDecisionDate) {
    return DeviatingDateDTO.builder()
        .id(deviatingDecisionDate.id())
        .value(LocalDate.parse(deviatingDecisionDate.deviatingDecisionDate()))
        .build();
  }

  public static DeviatingDecisionDate transformToDomain(DeviatingDateDTO deviatingDecisionDateDTO) {
    return DeviatingDecisionDate.builder()
        .id(deviatingDecisionDateDTO.getId())
        .deviatingDecisionDate(deviatingDecisionDateDTO.getValue().toString())
        .build();
  }
}
