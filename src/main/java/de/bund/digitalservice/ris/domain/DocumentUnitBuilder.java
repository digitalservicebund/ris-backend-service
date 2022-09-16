package de.bund.digitalservice.ris.domain;

public class DocumentUnitBuilder {

  private DocumentUnitDTO documentUnitDTO;

  private DocumentUnitBuilder() {}

  public static DocumentUnitBuilder newInstance() {
    return new DocumentUnitBuilder();
  }

  public DocumentUnitBuilder setDocUnitDTO(DocumentUnitDTO documentUnitDTO) {
    this.documentUnitDTO = documentUnitDTO;
    return this;
  }

  public DocumentUnit build() {
    if (documentUnitDTO == null) {
      return DocumentUnit.EMPTY;
    }

    return new DocumentUnit(
        new CoreData(
            documentUnitDTO.getFileNumber(),
            documentUnitDTO.getCourtType(),
            documentUnitDTO.getCategory(),
            documentUnitDTO.getProcedure(),
            documentUnitDTO.getEcli(),
            documentUnitDTO.getAppraisalBody(),
            documentUnitDTO.getDecisionDate(),
            documentUnitDTO.getCourtLocation(),
            documentUnitDTO.getLegalEffect(),
            documentUnitDTO.getInputType(),
            documentUnitDTO.getCenter(),
            documentUnitDTO.getRegion()),
        new Texts(
            documentUnitDTO.getDecisionName(),
            documentUnitDTO.getHeadline(),
            documentUnitDTO.getGuidingPrinciple(),
            documentUnitDTO.getHeadnote(),
            documentUnitDTO.getTenor(),
            documentUnitDTO.getReasons(),
            documentUnitDTO.getCaseFacts(),
            documentUnitDTO.getDecisionReasons(),
            documentUnitDTO.getPreviousDecisions()));
  }
}
