package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

  public DocumentUnitBuilder setId(Long id) {
    this.documentUnitDTO.setId(id);
    return this;
  }

  public DocumentUnitBuilder setUUID(UUID uuid) {
    this.documentUnitDTO.setUuid(uuid);
    return this;
  }

  public DocumentUnitBuilder setDocumentNumber(String documentNumber) {
    this.documentUnitDTO.setDocumentnumber(documentNumber);
    return this;
  }

  public DocumentUnitBuilder setCreationtimestamp(Instant instant) {
    this.documentUnitDTO.setCreationtimestamp(instant);
    return this;
  }

  public DocumentUnitBuilder setFileuploadtimestamp(Instant instant) {
    this.documentUnitDTO.setFileuploadtimestamp(instant);
    return this;
  }

  public DocumentUnitBuilder setPreviousDecisions(List<PreviousDecision> previousDecisions) {
    this.documentUnitDTO.setPreviousDecisions(previousDecisions);
    return this;
  }

  public DocumentUnit build() {
    if (documentUnitDTO == null) {
      return DocumentUnit.EMPTY;
    }

    Court court = null;
    String courtType = documentUnitDTO.getCourtType();
    String courtLocation = documentUnitDTO.getCourtLocation();
    if (courtType != null) {
      String label = (courtType + " " + (courtLocation == null ? "" : courtLocation)).trim();
      court = new Court(courtType, courtLocation, label, null);
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
            documentUnitDTO.getFileNumbers(),
            documentUnitDTO.getDeviatingFileNumbers(),
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
        documentUnitDTO.getPreviousDecisions(),
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
