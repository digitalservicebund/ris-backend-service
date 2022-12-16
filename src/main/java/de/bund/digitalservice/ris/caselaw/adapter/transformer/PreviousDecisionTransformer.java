package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PreviousDecisionDTO.PreviousDecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;

public class PreviousDecisionTransformer {
  private PreviousDecisionTransformer() {}

  public static PreviousDecisionDTO generateDTO(
      PreviousDecision previousDecision, Long documentUnitId) {
    return fillData(PreviousDecisionDTO.builder(), previousDecision, documentUnitId);
  }

  public static PreviousDecisionDTO enrichDTO(
      PreviousDecisionDTO previousDecisionDTO, PreviousDecision previousDecision) {
    return fillData(
        previousDecisionDTO.toBuilder(), previousDecision, previousDecisionDTO.getDocumentUnitId());
  }

  private static PreviousDecisionDTO fillData(
      PreviousDecisionDTOBuilder toBuilder,
      PreviousDecision previousDecision,
      Long documentUnitId) {

    return PreviousDecisionDTO.builder()
        .id(previousDecision.id())
        .courtLocation(previousDecision.courtPlace())
        .courtType(previousDecision.courtType())
        .fileNumber(previousDecision.fileNumber())
        .documentUnitId(documentUnitId)
        .decisionDate(previousDecision.date())
        .build();
  }
}
