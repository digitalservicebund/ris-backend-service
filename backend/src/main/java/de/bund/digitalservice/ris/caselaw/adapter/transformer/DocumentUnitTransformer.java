package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;import java.time.Instant;import java.util.List;

public class DocumentUnitTransformer {
  private DocumentUnitTransformer() {}

  public static DocumentUnitDTO enrichDTO(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {

    DataSource dataSource = DataSource.NEURIS;
    if (documentUnit.dataSource() != null) {
      dataSource = documentUnit.dataSource();
    }

    DocumentUnitDTO.DocumentUnitDTOBuilder builder =
        documentUnitDTO.toBuilder()
            .uuid(documentUnit.uuid())
            .documentnumber(documentUnit.documentNumber())
            .creationtimestamp(documentUnit.creationtimestamp())
            .fileuploadtimestamp(documentUnit.fileuploadtimestamp())
            .dataSource(dataSource)
            .s3path(documentUnit.s3path())
            .filetype(documentUnit.filetype())
            .filename(documentUnit.filename());

    if (documentUnit.coreData() != null) {
      CoreData coreData = documentUnit.coreData();

      builder
          .procedure(coreData.procedure())
          .ecli(coreData.ecli())
          .appraisalBody(coreData.appraisalBody())
          .decisionDate(coreData.decisionDate())
          .inputType(coreData.inputType())
          .center(coreData.center());

      if (coreData.court() != null) {
        builder
            .courtType(documentUnit.coreData().court().type())
            .courtLocation(coreData.court().location());
      } else {
        builder.courtType(null);
        builder.courtLocation(null);
      }
    } else {
      builder
          .procedure(null)
          .ecli(null)
          .appraisalBody(null)
          .decisionDate(null)
          .inputType(null)
          .center(null)
          .courtType(null)
          .courtLocation(null);
    }

    if (documentUnitDTO.getId() == null
        && documentUnit.proceedingDecisions() != null
        && !documentUnit.proceedingDecisions().isEmpty()) {

      throw new DocumentUnitTransformerException(
          "Transformation of a document unit with previous decisions only allowed by update. "
              + "Document unit must have a database id!");
    }

    if (documentUnit.texts() != null) {
      Texts texts = documentUnit.texts();

      builder
          .decisionName(texts.decisionName())
          .headline(texts.headline())
          .guidingPrinciple(texts.guidingPrinciple())
          .headnote(texts.headnote())
          .tenor(texts.tenor())
          .reasons(texts.reasons())
          .caseFacts(texts.caseFacts())
          .decisionReasons(texts.decisionReasons());
    } else {
      builder
          .decisionName(null)
          .headline(null)
          .guidingPrinciple(null)
          .headnote(null)
          .tenor(null)
          .reasons(null)
          .caseFacts(null)
          .decisionReasons(null);
    }

    return builder.build();
  }

  public static Court getCourtObject(String courtType, String courtLocation) {
    Court court = null;
    if (courtType != null) {
      String label = (courtType + " " + (courtLocation == null ? "" : courtLocation)).trim();
      court = new Court(courtType, courtLocation, label, null);
    }
    return court;
  }

  public static DocumentUnit transformMetadataToDomain(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentUnitMetadataDTO.getDocumentTypeDTO();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel());
    }

    List<String> fileNumbers = null;
    if (documentUnitMetadataDTO.getFileNumbers() != null) {
      fileNumbers =
          documentUnitMetadataDTO.getFileNumbers().stream()
              .map(FileNumberDTO::getFileNumber)
              .toList();
    }

    DataSource dataSource = DataSource.NEURIS;
    if (documentUnitMetadataDTO.getDataSource() != null) {
      dataSource = documentUnitMetadataDTO.getDataSource();
    }

    return new DocumentUnit(
        documentUnitMetadataDTO.getUuid(),
        documentUnitMetadataDTO.getDocumentnumber(),
        documentUnitMetadataDTO.getCreationtimestamp(),
        documentUnitMetadataDTO.getFileuploadtimestamp(),
        dataSource,
        documentUnitMetadataDTO.getS3path(),
        documentUnitMetadataDTO.getFiletype(),
        documentUnitMetadataDTO.getFilename(),
        new CoreData(
            fileNumbers,
            null,
            getCourtObject(
                documentUnitMetadataDTO.getCourtType(), documentUnitMetadataDTO.getCourtLocation()),
            null,
            documentType,
            documentUnitMetadataDTO.getProcedure(),
            documentUnitMetadataDTO.getEcli(),
            null,
            documentUnitMetadataDTO.getAppraisalBody(),
            documentUnitMetadataDTO.getDecisionDate(),
            null,
            documentUnitMetadataDTO.getLegalEffect(),
            documentUnitMetadataDTO.getInputType(),
            documentUnitMetadataDTO.getCenter(),
            documentUnitMetadataDTO.getRegion()),
        null,
        null,
        null);
  }

  public static DocumentUnit transformDTO(DocumentUnitDTO documentUnitDTO) {
    if (documentUnitDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentUnitDTO.getDocumentTypeDTO();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel());
    }

    List<ProceedingDecision> proceedingDecisions = null;
    if (documentUnitDTO.getProceedingDecisions() != null) {
      proceedingDecisions =
          documentUnitDTO.getProceedingDecisions().stream()
              .map(ProceedingDecisionTransformer::transformToDomain)
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
    if (documentUnitDTO.getDataSource() != null) {
      dataSource = documentUnitDTO.getDataSource();
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
        proceedingDecisions,
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
