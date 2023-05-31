package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitReadDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitWriteDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.Instant;
import java.util.List;

public class DocumentUnitTransformer {
  private DocumentUnitTransformer() {}

  public static DocumentUnitWriteDTO enrichDTO(
      DocumentUnitWriteDTO documentUnitWriteDTO, DocumentUnit documentUnit) {

    DataSource dataSource = DataSource.NEURIS;
    if (documentUnit.dataSource() != null) {
      dataSource = documentUnit.dataSource();
    }

    DocumentUnitWriteDTO.DocumentUnitWriteDTOBuilder builder =
        documentUnitWriteDTO.toBuilder()
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
          .dateKnown(coreData.dateKnown())
          .inputType(coreData.inputType());

      if (coreData.documentationOffice() != null) {
        builder.documentationOffice(
            DocumentationOfficeTransformer.transform(coreData.documentationOffice()));
      }

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
          .dateKnown(true)
          .inputType(null)
          .documentationOffice(null)
          .courtType(null)
          .courtLocation(null);
    }

    if (documentUnitWriteDTO.getId() == null
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
      String label = Court.generateLabel(courtType, courtLocation);
      court = new Court(courtType, courtLocation, label, null);
    }
    return court;
  }

  private static DocumentationOffice getDocumentationOffice(
      DocumentationOfficeDTO documentationOfficeDTO) {
    if (documentationOfficeDTO == null) {
      return null;
    }
    return DocumentationOfficeTransformer.transformDTO(documentationOfficeDTO);
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
            documentUnitMetadataDTO.isDateKnown(),
            null,
            documentUnitMetadataDTO.getLegalEffect(),
            documentUnitMetadataDTO.getInputType(),
            getDocumentationOffice(documentUnitMetadataDTO.getDocumentationOffice()),
            documentUnitMetadataDTO.getRegion()),
        null,
        null,
        null,
        null);
  }

  public static DocumentUnit transformDTO(DocumentUnitWriteDTO documentUnitWriteDTO) {
    if (documentUnitWriteDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentUnitWriteDTO.getDocumentTypeDTO();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getJurisShortcut(), documentTypeDTO.getLabel());
    }

    List<ProceedingDecision> proceedingDecisions = null;
    if (documentUnitWriteDTO.getProceedingDecisions() != null) {
      proceedingDecisions =
          documentUnitWriteDTO.getProceedingDecisions().stream()
              .map(ProceedingDecisionTransformer::transformToDomain)
              .toList();
    }

    List<String> fileNumbers = null;
    if (documentUnitWriteDTO.getFileNumbers() != null) {
      fileNumbers =
          documentUnitWriteDTO.getFileNumbers().stream().map(FileNumberDTO::getFileNumber).toList();
    }

    List<String> deviatingFileNumbers = null;
    if (documentUnitWriteDTO.getDeviatingFileNumbers() != null) {
      deviatingFileNumbers =
          documentUnitWriteDTO.getDeviatingFileNumbers().stream()
              .map(FileNumberDTO::getFileNumber)
              .toList();
    }

    List<String> deviatingEclis = null;
    if (documentUnitWriteDTO.getDeviatingEclis() != null) {
      deviatingEclis =
          documentUnitWriteDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getEcli).toList();
    }

    List<Instant> deviatingDecisionDates = null;
    if (documentUnitWriteDTO.getDeviatingDecisionDates() != null) {
      deviatingDecisionDates =
          documentUnitWriteDTO.getDeviatingDecisionDates().stream()
              .map(DeviatingDecisionDateDTO::decisionDate)
              .toList();
    }

    List<String> incorrectCourts = null;
    if (documentUnitWriteDTO.getIncorrectCourts() != null) {
      incorrectCourts =
          documentUnitWriteDTO.getIncorrectCourts().stream().map(IncorrectCourtDTO::court).toList();
    }

    List<FieldOfLaw> fieldsOfLaw = null;
    if (documentUnitWriteDTO.getFieldsOfLaw() != null) {
      fieldsOfLaw =
          documentUnitWriteDTO.getFieldsOfLaw().stream()
              .map(FieldOfLawTransformer::transformToDomain)
              .toList();
    }

    List<DocumentUnitNorm> norms = null;
    if (documentUnitWriteDTO.getNorms() != null) {
      norms =
          documentUnitWriteDTO.getNorms().stream()
              .map(DocumentUnitNormTransformer::transformToDomain)
              .toList();
    }

    DataSource dataSource = DataSource.NEURIS;
    if (documentUnitWriteDTO.getDataSource() != null) {
      dataSource = documentUnitWriteDTO.getDataSource();
    }

    List<String> keywords = null;
    if (documentUnitWriteDTO.getKeywords() != null) {
      keywords = documentUnitWriteDTO.getKeywords().stream().map(KeywordDTO::keyword).toList();
    }

    return new DocumentUnit(
        documentUnitWriteDTO.getUuid(),
        documentUnitWriteDTO.getDocumentnumber(),
        documentUnitWriteDTO.getCreationtimestamp(),
        documentUnitWriteDTO.getFileuploadtimestamp(),
        dataSource,
        documentUnitWriteDTO.getS3path(),
        documentUnitWriteDTO.getFiletype(),
        documentUnitWriteDTO.getFilename(),
        new CoreData(
            fileNumbers,
            deviatingFileNumbers,
            getCourtObject(
                documentUnitWriteDTO.getCourtType(), documentUnitWriteDTO.getCourtLocation()),
            incorrectCourts,
            documentType,
            documentUnitWriteDTO.getProcedure(),
            documentUnitWriteDTO.getEcli(),
            deviatingEclis,
            documentUnitWriteDTO.getAppraisalBody(),
            documentUnitWriteDTO.getDecisionDate(),
            documentUnitWriteDTO.isDateKnown(),
            deviatingDecisionDates,
            documentUnitWriteDTO.getLegalEffect(),
            documentUnitWriteDTO.getInputType(),
            getDocumentationOffice(documentUnitWriteDTO.getDocumentationOffice()),
            documentUnitWriteDTO.getRegion()),
        proceedingDecisions,
        new Texts(
            documentUnitWriteDTO.getDecisionName(),
            documentUnitWriteDTO.getHeadline(),
            documentUnitWriteDTO.getGuidingPrinciple(),
            documentUnitWriteDTO.getHeadnote(),
            documentUnitWriteDTO.getTenor(),
            documentUnitWriteDTO.getReasons(),
            documentUnitWriteDTO.getCaseFacts(),
            documentUnitWriteDTO.getDecisionReasons()),
        new ContentRelatedIndexing(keywords, fieldsOfLaw, norms),
        documentUnitWriteDTO.getStatus());
  }

  public static DocumentUnitWriteDTO transformReadDTO(DocumentUnitReadDTO documentUnitReadDTO) {
    return DocumentUnitWriteDTO.builder()
        .id(documentUnitReadDTO.getId())
        .uuid(documentUnitReadDTO.getUuid())
        .documentnumber(documentUnitReadDTO.getDocumentnumber())
        .creationtimestamp(documentUnitReadDTO.getCreationtimestamp())
        .dataSource(documentUnitReadDTO.getDataSource())
        .fileuploadtimestamp(documentUnitReadDTO.getFileuploadtimestamp())
        .s3path(documentUnitReadDTO.getS3path())
        .filetype(documentUnitReadDTO.getFiletype())
        .filename(documentUnitReadDTO.getFilename())
        .courtType(documentUnitReadDTO.getCourtType())
        .documentTypeId(documentUnitReadDTO.getDocumentTypeId())
        .documentTypeDTO(documentUnitReadDTO.getDocumentTypeDTO())
        .procedure(documentUnitReadDTO.getProcedure())
        .ecli(documentUnitReadDTO.getEcli())
        .appraisalBody(documentUnitReadDTO.getAppraisalBody())
        .decisionDate(documentUnitReadDTO.getDecisionDate())
        .dateKnown(documentUnitReadDTO.isDateKnown())
        .courtLocation(documentUnitReadDTO.getCourtLocation())
        .legalEffect(documentUnitReadDTO.getLegalEffect())
        .inputType(documentUnitReadDTO.getInputType())
        .region(documentUnitReadDTO.getRegion())
        .documentationOfficeId(documentUnitReadDTO.getDocumentationOfficeId())
        .documentationOffice(documentUnitReadDTO.getDocumentationOffice())
        .fileNumbers(documentUnitReadDTO.getFileNumbers())
        .status(documentUnitReadDTO.getStatus())
        .decisionName(documentUnitReadDTO.getDecisionName())
        .headline(documentUnitReadDTO.getHeadline())
        .guidingPrinciple(documentUnitReadDTO.getGuidingPrinciple())
        .headnote(documentUnitReadDTO.getHeadnote())
        .tenor(documentUnitReadDTO.getTenor())
        .reasons(documentUnitReadDTO.getReasons())
        .caseFacts(documentUnitReadDTO.getCaseFacts())
        .decisionReasons(documentUnitReadDTO.getDecisionReasons())
        .proceedingDecisions(documentUnitReadDTO.getProceedingDecisions())
        .deviatingFileNumbers(documentUnitReadDTO.getDeviatingFileNumbers())
        .incorrectCourts(documentUnitReadDTO.getIncorrectCourts())
        .deviatingEclis(documentUnitReadDTO.getDeviatingEclis())
        .deviatingDecisionDates(documentUnitReadDTO.getDeviatingDecisionDates())
        .keywords(documentUnitReadDTO.getKeywords())
        .fieldsOfLaw(documentUnitReadDTO.getFieldsOfLaw())
        .norms(documentUnitReadDTO.getNorms())
        .build();
  }
}
