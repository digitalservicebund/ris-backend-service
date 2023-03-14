package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision.ProceedingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision.ProceedingDecisionDTO.PreviousDecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;

public class PreviousDecisionTransformer {
  private PreviousDecisionTransformer() {}

  private static String getCourtLocation(ProceedingDecision proceedingDecision) {
    String courtLocation = null;
    if (proceedingDecision.court() != null) {
      courtLocation = proceedingDecision.court().location();
    }
    return courtLocation;
  }

  private static String getCourtType(ProceedingDecision proceedingDecision) {
    String courtType = null;
    if (proceedingDecision.court() != null) {
      courtType = proceedingDecision.court().type();
    }
    return courtType;
  }

  public static ProceedingDecisionDTO generateDTO(
          ProceedingDecision proceedingDecision, Long documentUnitId) {
    return fillData(ProceedingDecisionDTO.builder(), proceedingDecision, documentUnitId);
  }

  public static ProceedingDecisionDTO enrichDTO(
          ProceedingDecisionDTO proceedingDecisionDTO, ProceedingDecision proceedingDecision) {
    return fillData(
        proceedingDecisionDTO.toBuilder(),
            proceedingDecision,
        proceedingDecisionDTO.getDocumentUnitId());
  }

  private static ProceedingDecisionDTO fillData(
      PreviousDecisionDTOBuilder toBuilder,
      ProceedingDecision proceedingDecision,
      Long documentUnitId) {

    return ProceedingDecisionDTO.builder()
        .id(proceedingDecision.id())
        .courtLocation(getCourtLocation(proceedingDecision))
        .courtType(getCourtType(proceedingDecision))
        .fileNumber(proceedingDecision.fileNumber())
        .documentUnitId(documentUnitId)
        .decisionDateTimestamp(proceedingDecision.date())
        .build();
  }
}
