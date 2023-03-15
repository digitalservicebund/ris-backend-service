package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DataSourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.Instant;
import java.util.List;

public class DocumentUnitBuilder {

  private DocumentUnitDTO documentUnitDTO;

  private DocumentUnitBuilder() {}

  private Court getCourtObject(String courtType, String courtLocation) {
    Court court = null;
    if (courtType != null) {
      String label = (courtType + " " + (courtLocation == null ? "" : courtLocation)).trim();
      court = new Court(courtType, courtLocation, label, null);
    }
    return court;
  }

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

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentUnitDTO.getDocumentTypeDTO();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel());
    }

    List<PreviousDecision> previousDecisions = null;
    if (documentUnitDTO.getPreviousDecisions() != null) {
      previousDecisions =
          documentUnitDTO.getPreviousDecisions().stream()
              .map(
                  previousDecisionDTO ->
                      PreviousDecision.builder()
                          .id(previousDecisionDTO.getId())
                          .court(
                              getCourtObject(
                                  previousDecisionDTO.getCourtType(),
                                  previousDecisionDTO.getCourtLocation()))
                          .fileNumber(previousDecisionDTO.getFileNumber())
                          .date(previousDecisionDTO.getDecisionDateTimestamp())
                          .build())
              .toList();
    }

    List<String> fileNumbers = null;
    if (documentUnitDTO.getFileNumbers() != null) {
      fileNumbers =
          documentUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getFileNumber).toList();
    }

    List<String> deviatingFileNumbers = null;
    if (documentUnitDTO.getDeviatingFileNumbers() != null) {
      deviatingFileNumbers =
          documentUnitDTO.getDeviatingFileNumbers().stream()
              .map(FileNumberDTO::getFileNumber)
              .toList();
    }

    List<String> deviatingEclis = null;
    if (documentUnitDTO.getDeviatingEclis() != null) {
      deviatingEclis =
          documentUnitDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getEcli).toList();
    }

    List<Instant> deviatingDecisionDates = null;
    if (documentUnitDTO.getDeviatingDecisionDates() != null) {
      deviatingDecisionDates =
          documentUnitDTO.getDeviatingDecisionDates().stream()
              .map(DeviatingDecisionDateDTO::decisionDate)
              .toList();
    }

    List<String> incorrectCourts = null;
    if (documentUnitDTO.getIncorrectCourts() != null) {
      incorrectCourts =
          documentUnitDTO.getIncorrectCourts().stream().map(IncorrectCourtDTO::court).toList();
    }

    List<FieldOfLaw> fieldsOfLaw = null;
    if (documentUnitDTO.getFieldsOfLaw() != null) {
      fieldsOfLaw =
          documentUnitDTO.getFieldsOfLaw().stream()
              .map(FieldOfLawTransformer::transformToDomain)
              .toList();
    }

    DataSource dataSource = DataSource.NEURIS;
    if (documentUnitDTO.getDataSource() == DataSourceDTO.MIGRATION) {
      dataSource = DataSource.MIGRATION;
    }

    List<String> keywords = null;
    if (documentUnitDTO.getKeywords() != null) {
      keywords = documentUnitDTO.getKeywords().stream().map(KeywordDTO::keyword).toList();
    }

    return new DocumentUnit(
        documentUnitDTO.getUuid(),
        documentUnitDTO.getDocumentnumber(),
        documentUnitDTO.getCreationtimestamp(),
        documentUnitDTO.getFileuploadtimestamp(),
        dataSource,
        documentUnitDTO.getS3path(),
        documentUnitDTO.getFiletype(),
        documentUnitDTO.getFilename(),
        new CoreData(
            fileNumbers,
            deviatingFileNumbers,
            getCourtObject(documentUnitDTO.getCourtType(), documentUnitDTO.getCourtLocation()),
            incorrectCourts,
            documentType,
            documentUnitDTO.getProcedure(),
            documentUnitDTO.getEcli(),
            deviatingEclis,
            documentUnitDTO.getAppraisalBody(),
            documentUnitDTO.getDecisionDate(),
            deviatingDecisionDates,
            documentUnitDTO.getLegalEffect(),
            documentUnitDTO.getInputType(),
            documentUnitDTO.getCenter(),
            documentUnitDTO.getRegion()),
        previousDecisions,
        new Texts(
            documentUnitDTO.getDecisionName(),
            documentUnitDTO.getHeadline(),
            documentUnitDTO.getGuidingPrinciple(),
            documentUnitDTO.getHeadnote(),
            documentUnitDTO.getTenor(),
            documentUnitDTO.getReasons(),
            documentUnitDTO.getCaseFacts(),
            documentUnitDTO.getDecisionReasons()),
        new ContentRelatedIndexing(keywords, fieldsOfLaw));
  }
}
