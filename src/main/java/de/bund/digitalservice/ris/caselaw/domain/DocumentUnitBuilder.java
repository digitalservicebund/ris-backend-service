package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.time.Instant;

public class DocumentUnitBuilder {

  private DocumentUnitDTO documentUnitDTO;

  private DocumentUnitBuilder() {}

  public static DocumentUnitBuilder newInstance() {
    return new DocumentUnitBuilder();
  }

  public DocumentUnitBuilder setDocumentUnitDTO(DocumentUnitDTO documentUnitDTO) {
    this.documentUnitDTO = documentUnitDTO;
    return this;
  }

  public DocumentUnit build() {
    if (documentUnitDTO == null) {
      return DocumentUnit.EMPTY;
    }

    Court court = null;
    if (documentUnitDTO.getCourtType() != null && documentUnitDTO.getCourtLocation() != null) {
      court =
          new Court(
              documentUnitDTO.getCourtType(),
              documentUnitDTO.getCourtLocation(),
              documentUnitDTO.getCourtType() + " " + documentUnitDTO.getCourtLocation());
    }

    return new DocumentUnit(
        documentUnitDTO.getId(),
        documentUnitDTO.getUuid(),
        documentUnitDTO.getDocumentnumber(),
        documentUnitDTO.getCreationtimestamp(),
        documentUnitDTO.getFileuploadtimestamp(),
        documentUnitDTO.getS3path(),
        documentUnitDTO.getFiletype(),
        documentUnitDTO.getFilename(),
        new CoreData(
            documentUnitDTO.getFileNumber(),
            court,
            documentUnitDTO.getCategory(),
            documentUnitDTO.getProcedure(),
            documentUnitDTO.getEcli(),
            documentUnitDTO.getAppraisalBody(),
            documentUnitDTO.getDecisionDate() != null
                ? Instant.parse(documentUnitDTO.getDecisionDate())
                : null,
            documentUnitDTO.getLegalEffect(),
            documentUnitDTO.getInputType(),
            documentUnitDTO.getCenter(),
            documentUnitDTO.getRegion()),
        documentUnitDTO.getPreviousDecisions().stream()
            .map(
                previousDecisionDTO ->
                    PreviousDecision.builder()
                        .id(previousDecisionDTO.getId())
                        .courtType(previousDecisionDTO.getCourtType())
                        .courtPlace(previousDecisionDTO.getCourtLocation())
                        .fileNumber(previousDecisionDTO.getFileNumber())
                        .date(previousDecisionDTO.getDecisionDate())
                        .build())
            .toList(),
        new Texts(
            documentUnitDTO.getDecisionName(),
            documentUnitDTO.getHeadline(),
            documentUnitDTO.getGuidingPrinciple(),
            documentUnitDTO.getHeadnote(),
            documentUnitDTO.getTenor(),
            documentUnitDTO.getReasons(),
            documentUnitDTO.getCaseFacts(),
            documentUnitDTO.getDecisionReasons()));
  }
}
