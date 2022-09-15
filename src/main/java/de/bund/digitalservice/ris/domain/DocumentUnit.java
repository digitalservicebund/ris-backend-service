package de.bund.digitalservice.ris.domain;

import lombok.Data;

@Data
public class DocumentUnit {

  private CoreData coreData;
  private Categories categories;

  public static final DocumentUnit EMPTY = new DocumentUnit();

  private DocumentUnit() {}

  public DocumentUnit(DocUnitDTO docUnitDTO) {
    this.coreData =
        new CoreData(
            docUnitDTO.getDocketNumber(),
            docUnitDTO.getCourtType(),
            docUnitDTO.getCategory(),
            docUnitDTO.getProcedure(),
            docUnitDTO.getEcli(),
            docUnitDTO.getAppraisalBody(),
            docUnitDTO.getDecisionDate(),
            docUnitDTO.getCourtLocation(),
            docUnitDTO.getLegalEffect(),
            docUnitDTO.getReceiptType(),
            docUnitDTO.getCenter(),
            docUnitDTO.getRegion());
    this.categories =
        new Categories(
            docUnitDTO.getDecisionName(),
            docUnitDTO.getHeadline(),
            docUnitDTO.getGuidingPrinciple(),
            docUnitDTO.getHeadnote(),
            docUnitDTO.getTenor(),
            docUnitDTO.getReasons(),
            docUnitDTO.getCaseFacts(),
            docUnitDTO.getDecisionReasons(),
            docUnitDTO.getPreviousDecisions());
  }
}
