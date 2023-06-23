package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
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

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(fileNumbers)
            .court(
                getCourtObject(
                    documentUnitMetadataDTO.getCourtType(),
                    documentUnitMetadataDTO.getCourtLocation()))
            .documentType(documentType)
            .procedure(documentUnitMetadataDTO.getProcedure())
            .ecli(documentUnitMetadataDTO.getEcli())
            .appraisalBody(documentUnitMetadataDTO.getAppraisalBody())
            .decisionDate(documentUnitMetadataDTO.getDecisionDate())
            .dateKnown(documentUnitMetadataDTO.isDateKnown())
            .legalEffect(documentUnitMetadataDTO.getLegalEffect())
            .inputType(documentUnitMetadataDTO.getInputType())
            .documentationOffice(
                getDocumentationOffice(documentUnitMetadataDTO.getDocumentationOffice()))
            .region(documentUnitMetadataDTO.getRegion())
            .build();

    return DocumentUnit.builder()
        .uuid(documentUnitMetadataDTO.getUuid())
        .coreData(coreData)
        .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
        .creationtimestamp(documentUnitMetadataDTO.getCreationtimestamp())
        .fileuploadtimestamp(documentUnitMetadataDTO.getFileuploadtimestamp())
        .dataSource(dataSource)
        .s3path(documentUnitMetadataDTO.getS3path())
        .filetype(documentUnitMetadataDTO.getFiletype())
        .filename(documentUnitMetadataDTO.getFilename())
        .status(documentUnitMetadataDTO.getStatus())
        .build();
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

    List<ActiveCitation> activeCitations = null;
    if (documentUnitDTO.getActiveCitations() != null) {
      activeCitations =
          documentUnitDTO.getActiveCitations().stream()
              .map(ActiveCitationTransformer::transformToDomain)
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

    List<DocumentUnitNorm> norms = null;
    if (documentUnitDTO.getNorms() != null) {
      norms =
          documentUnitDTO.getNorms().stream()
              .map(DocumentUnitNormTransformer::transformToDomain)
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

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(fileNumbers)
            .deviatingFileNumbers(deviatingFileNumbers)
            .court(
                getCourtObject(documentUnitDTO.getCourtType(), documentUnitDTO.getCourtLocation()))
            .incorrectCourts(incorrectCourts)
            .documentType(documentType)
            .procedure(documentUnitDTO.getProcedure())
            .ecli(documentUnitDTO.getEcli())
            .deviatingEclis(deviatingEclis)
            .appraisalBody(documentUnitDTO.getAppraisalBody())
            .decisionDate(documentUnitDTO.getDecisionDate())
            .dateKnown(documentUnitDTO.isDateKnown())
            .deviatingDecisionDates(deviatingDecisionDates)
            .legalEffect(documentUnitDTO.getLegalEffect())
            .inputType(documentUnitDTO.getInputType())
            .documentationOffice(getDocumentationOffice(documentUnitDTO.getDocumentationOffice()))
            .region(documentUnitDTO.getRegion())
            .build();
    Texts texts =
        Texts.builder()
            .decisionName(documentUnitDTO.getDecisionName())
            .headline(documentUnitDTO.getHeadline())
            .guidingPrinciple(documentUnitDTO.getGuidingPrinciple())
            .headnote(documentUnitDTO.getHeadnote())
            .tenor(documentUnitDTO.getTenor())
            .reasons(documentUnitDTO.getReasons())
            .caseFacts(documentUnitDTO.getCaseFacts())
            .decisionReasons(documentUnitDTO.getDecisionReasons())
            .build();

    return DocumentUnit.builder()
        .uuid(documentUnitDTO.getUuid())
        .documentNumber(documentUnitDTO.getDocumentnumber())
        .creationtimestamp(documentUnitDTO.getCreationtimestamp())
        .fileuploadtimestamp(documentUnitDTO.getFileuploadtimestamp())
        .dataSource(dataSource)
        .s3path(documentUnitDTO.getS3path())
        .filetype(documentUnitDTO.getFiletype())
        .filename(documentUnitDTO.getFilename())
        .coreData(coreData)
        .proceedingDecisions(proceedingDecisions)
        .texts(texts)
        .status(documentUnitDTO.getStatus())
        .contentRelatedIndexing(
            ContentRelatedIndexing.builder()
                .keywords(keywords)
                .fieldsOfLaw(fieldsOfLaw)
                .norms(norms)
                .activeCitations(activeCitations)
                .build())
        .build();
  }
}
