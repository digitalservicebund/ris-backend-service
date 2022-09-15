package de.bund.digitalservice.ris.domain;

public class DocumentUnitBuilder {

  private DocUnitDTO docUnitDTO;

  private DocumentUnitBuilder() {}

  public static DocumentUnitBuilder newInstance() {
    return new DocumentUnitBuilder();
  }

  public DocumentUnitBuilder setDocUnitDTO(DocUnitDTO docUnitDTO) {
    this.docUnitDTO = docUnitDTO;
    return this;
  }

  public DocumentUnit build() {
    if (docUnitDTO == null) {
      return DocumentUnit.EMPTY;
    }

    return new DocumentUnit(
        new CoreData(
            docUnitDTO.getFileNumber(),
            docUnitDTO.getCourtType(),
            docUnitDTO.getCategory(),
            docUnitDTO.getProcedure(),
            docUnitDTO.getEcli(),
            docUnitDTO.getAppraisalBody(),
            docUnitDTO.getDecisionDate(),
            docUnitDTO.getCourtLocation(),
            docUnitDTO.getLegalEffect(),
            docUnitDTO.getInputType(),
            docUnitDTO.getCenter(),
            docUnitDTO.getRegion()),
        new Categories(
            docUnitDTO.getDecisionName(),
            docUnitDTO.getHeadline(),
            docUnitDTO.getGuidingPrinciple(),
            docUnitDTO.getHeadnote(),
            docUnitDTO.getTenor(),
            docUnitDTO.getReasons(),
            docUnitDTO.getCaseFacts(),
            docUnitDTO.getDecisionReasons(),
            docUnitDTO.getPreviousDecisions()));
  }
}
