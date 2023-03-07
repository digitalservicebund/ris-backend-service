package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink.LinkedDocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink.LinkedDocumentUnitDTO.PreviousDecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentUnit;

public class PreviousDecisionTransformer {
  private PreviousDecisionTransformer() {}

  private static String getCourtLocation(LinkedDocumentUnit linkedDocumentUnit) {
    String courtLocation = null;
    if (linkedDocumentUnit.court() != null) {
      courtLocation = linkedDocumentUnit.court().location();
    }
    return courtLocation;
  }

  private static String getCourtType(LinkedDocumentUnit linkedDocumentUnit) {
    String courtType = null;
    if (linkedDocumentUnit.court() != null) {
      courtType = linkedDocumentUnit.court().type();
    }
    return courtType;
  }

  public static LinkedDocumentUnitDTO generateDTO(
      LinkedDocumentUnit linkedDocumentUnit, Long documentUnitId) {
    return fillData(LinkedDocumentUnitDTO.builder(), linkedDocumentUnit, documentUnitId);
  }

  public static LinkedDocumentUnitDTO enrichDTO(
      LinkedDocumentUnitDTO linkedDocumentUnitDTO, LinkedDocumentUnit linkedDocumentUnit) {
    return fillData(
        linkedDocumentUnitDTO.toBuilder(),
        linkedDocumentUnit,
        linkedDocumentUnitDTO.getDocumentUnitId());
  }

  private static LinkedDocumentUnitDTO fillData(
      PreviousDecisionDTOBuilder toBuilder,
      LinkedDocumentUnit linkedDocumentUnit,
      Long documentUnitId) {

    return LinkedDocumentUnitDTO.builder()
        .id(linkedDocumentUnit.id())
        .courtLocation(getCourtLocation(linkedDocumentUnit))
        .courtType(getCourtType(linkedDocumentUnit))
        .fileNumber(linkedDocumentUnit.fileNumber())
        .documentUnitId(documentUnitId)
        .decisionDateTimestamp(linkedDocumentUnit.date())
        .build();
  }
}
