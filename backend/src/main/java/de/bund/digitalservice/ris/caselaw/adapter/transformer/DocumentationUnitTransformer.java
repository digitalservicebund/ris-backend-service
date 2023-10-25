package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitTransformer {
  private DocumentationUnitTransformer() {}

  public static DocumentationUnitDTO domainToDTO(
      DocumentationUnitDTO currentDto, DocumentUnit updatedDomainObject) {

    if (log.isDebugEnabled()) {
      log.debug("enrich database documentation unit '{}'", currentDto.getId());
    }

    OriginalFileDocumentDTO originalFileDocument =
        OriginalFileDocumentDTO.builder()
            .extension(updatedDomainObject.filetype())
            .filename(updatedDomainObject.filename())
            .s3ObjectPath(updatedDomainObject.s3path())
            .uploadTimestamp(updatedDomainObject.fileuploadtimestamp())
            .build();

    DocumentationUnitDTO.DocumentationUnitDTOBuilder builder =
        currentDto.toBuilder()
            .id(updatedDomainObject.uuid())
            .documentNumber(updatedDomainObject.documentNumber())
            .originalFileDocument(originalFileDocument)
            .fileNumbers(
                updatedDomainObject.coreData().fileNumbers().stream()
                    .map(
                        fileNumber ->
                            FileNumberDTO.builder()
                                // TODO do we have to use the fileNumber repo instead?
                                .value(fileNumber)
                                .documentationUnit(currentDto)
                                .build())
                    .collect(Collectors.toSet()))
            .normReferences(
                updatedDomainObject.contentRelatedIndexing().norms().stream()
                    .map(
                        norm ->
                            NormReferenceDTO.builder()
                                // TODO do we have to use the normAbbreviation repo instead?
                                .normAbbreviation(
                                    NormAbbreviationDTO.builder()
                                        .id(norm.normAbbreviation().id())
                                        .build())
                                .singleNorm(norm.singleNorm())
                                .dateOfVersion(norm.dateOfVersion())
                                .dateOfRelevance(norm.dateOfRelevance())
                                .build())
                    .collect(Collectors.toSet()));
    var legalEffect =
        LegalEffect.deriveLegalEffectFrom(
            updatedDomainObject, hasCourtChanged(currentDto, updatedDomainObject));

    LegalEffectDTO legalEffectDTO;
    switch (legalEffect) {
      case NO -> legalEffectDTO = LegalEffectDTO.NEIN;
      case YES -> legalEffectDTO = LegalEffectDTO.JA;
      case NOT_SPECIFIED -> legalEffectDTO = LegalEffectDTO.KEINE_ANGABE;
      default -> legalEffectDTO = LegalEffectDTO.FALSCHE_ANGABE;
    }
    builder.legalEffect(legalEffectDTO);

    if (updatedDomainObject.coreData() != null) {
      CoreData coreData = updatedDomainObject.coreData();

      builder
          .ecli(coreData.ecli())
          .judicialBody(coreData.appraisalBody())
          .decisionDate(LocalDate.ofInstant(coreData.decisionDate(), ZoneId.of("Europe/Berlin")))
          .inputType(coreData.inputType());
      // TODO documentationOffice
      // TODO court
    } else {
      builder.procedure(null).ecli(null).judicialBody(null).decisionDate(null).inputType(null)
      // TODO documentationOffice
      // TODO court
      ;
    }

    if (currentDto.getId() == null
        && updatedDomainObject.proceedingDecisions() != null
        && !updatedDomainObject.proceedingDecisions().isEmpty()) {

      throw new DocumentUnitTransformerException(
          "Transformation of a document unit with previous decisions only allowed by update. "
              + "Document unit must have a database id!");
    }

    if (updatedDomainObject.texts() != null) {
      Texts texts = updatedDomainObject.texts();

      builder
          .decisionNames(Set.of(DecisionNameDTO.builder().value(texts.decisionName()).build()))
          .headline(texts.headline())
          .guidingPrinciple(texts.guidingPrinciple())
          .headnote(texts.headnote())
          .tenor(texts.tenor())
          .grounds(texts.reasons())
          .caseFacts(texts.caseFacts())
          .decisionGrounds(texts.decisionReasons());
    } else {
      builder
          .decisionNames(null)
          .headline(null)
          .guidingPrinciple(null)
          .headnote(null)
          .tenor(null)
          .grounds(null)
          .caseFacts(null)
          .decisionGrounds(null);
    }

    return builder.build();
  }

  private static boolean hasCourtChanged(
      DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().court() == null
    // TODO court
    //        || !Objects.equals(documentUnitDTO.getCourtType(),
    // documentUnit.coreData().court().type())
    //        || !Objects.equals(
    //            documentUnitDTO.getCourtLocation(), documentUnit.coreData().court().location()
    ;
  }

  public static Court getCourtObject(String courtType, String courtLocation) {
    if (log.isDebugEnabled()) {
      log.debug("get court object from '{}' and '{}", courtType, courtLocation);
    }

    Court court = null;
    if (courtType != null) {
      String label = Court.generateLabel(courtType, courtLocation);
      court = new Court(courtType, courtLocation, label, null);
    }

    return court;
  }

  public static DocumentUnit transformMetadataToDomain(
      DocumentationUnitMetadataDTO documentUnitMetadataDTO) {

    if (log.isDebugEnabled()) {
      log.debug(
          "transfer database metadata documentation unit '{}' to domain object",
          documentUnitMetadataDTO.getId());
    }

    if (documentUnitMetadataDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentUnitMetadataDTO.getDocumentType();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getAbbreviation(), documentTypeDTO.getLabel());
    }

    List<String> fileNumbers = null;
    if (documentUnitMetadataDTO.getFileNumbers() != null) {
      fileNumbers =
          documentUnitMetadataDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    }

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(fileNumbers)
            //            .court(
            //                getCourtObject(
            //                    documentUnitMetadataDTO.getCourtType(),
            //                    documentUnitMetadataDTO.getCourtLocation()))
            .documentType(documentType)
            .ecli(documentUnitMetadataDTO.getEcli())
            .appraisalBody(documentUnitMetadataDTO.getJudicialBody())
            .decisionDate(
                documentUnitMetadataDTO.getDecisionDate() == null
                    ? null
                    : documentUnitMetadataDTO
                        .getDecisionDate()
                        .atStartOfDay(ZoneId.of("Europe/Berlin"))
                        .toInstant())
            // .legalEffect(documentUnitMetadataDTO.getLegalEffect())
            .inputType(documentUnitMetadataDTO.getInputType())
            //            .documentationOffice(
            //
            // getDocumentationOffice(documentUnitMetadataDTO.getDocumentationOffice()))
            // TODO multiple regions? .region(documentUnitMetadataDTO.getRegion())
            .build();

    return DocumentUnit.builder()
        .uuid(documentUnitMetadataDTO.getId())
        .coreData(coreData)
        .documentNumber(documentUnitMetadataDTO.getDocumentNumber())
        // .fileuploadtimestamp(documentUnitMetadataDTO.getFileuploadtimestamp())
        // .s3path(documentUnitMetadataDTO.getS3path())
        // .filetype(documentUnitMetadataDTO.getFiletype())
        // .filename(documentUnitMetadataDTO.getFilename())
        // .status(documentUnitMetadataDTO.getStatus())
        .build();
  }

  public static DocumentUnit transformDTO(DocumentationUnitDTO documentationUnitDTO) {
    if (log.isDebugEnabled()) {
      log.debug(
          "transfer database documentation unit '{}' to domain object",
          documentationUnitDTO.getId());
    }

    if (documentationUnitDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentType documentType = null;
    DocumentTypeDTO documentTypeDTO = documentationUnitDTO.getDocumentType();
    if (documentTypeDTO != null) {
      documentType =
          new DocumentType(documentTypeDTO.getAbbreviation(), documentTypeDTO.getLabel());
    }

    //    List<ProceedingDecision> proceedingDecisions = null;
    //    if (documentationUnitDTO.getProceedingDecisions() != null) {
    //      proceedingDecisions =
    //          documentationUnitDTO.getProceedingDecisions().stream()
    //              .map(ProceedingDecisionTransformer::transformToDomain)
    //              .toList();
    //    }
    //
    //    List<ActiveCitation> activeCitations = null;
    //    if (documentationUnitDTO.getActiveCitations() != null) {
    //      activeCitations = documentationUnitDTO.getActiveCitations();
    //    }

    List<String> fileNumbers = null;
    if (documentationUnitDTO.getFileNumbers() != null) {
      fileNumbers =
          documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    }

    //    List<String> deviatingFileNumbers = null;
    //    if (documentationUnitDTO.getDeviatingFileNumbers() != null) {
    //      deviatingFileNumbers =
    //          documentationUnitDTO.getDeviatingFileNumbers().stream()
    //              .map(FileNumberDTO::getFileNumber)
    //              .toList();
    //    }
    //
    //    List<String> deviatingEclis = null;
    //    if (documentationUnitDTO.getDeviatingEclis() != null) {
    //      deviatingEclis =
    //
    // documentationUnitDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getEcli).toList();
    //    }
    //
    //    List<Instant> deviatingDecisionDates = null;
    //    if (documentationUnitDTO.getDeviatingDecisionDates() != null) {
    //      deviatingDecisionDates =
    //          documentationUnitDTO.getDeviatingDecisionDates().stream()
    //              .map(DeviatingDecisionDateDTO::decisionDate)
    //              .toList();
    //    }
    //
    //    List<String> incorrectCourts = null;
    //    if (documentationUnitDTO.getIncorrectCourts() != null) {
    //      incorrectCourts =
    //
    // documentationUnitDTO.getIncorrectCourts().stream().map(IncorrectCourtDTO::court).toList();
    //    }
    //
    //    List<FieldOfLaw> fieldsOfLaw = null;
    //    if (documentationUnitDTO.getFieldsOfLaw() != null) {
    //      fieldsOfLaw =
    //          documentationUnitDTO.getFieldsOfLaw().stream()
    //              .map(FieldOfLawTransformer::transformToDomain)
    //              .toList();
    //    }

    List<DocumentUnitNorm> norms = null;
    if (documentationUnitDTO.getNormReferences() != null) {
      norms =
          documentationUnitDTO.getNormReferences().stream()
              .map(DocumentUnitNormTransformer::transformToDomain)
              .toList();
    }

    //    List<String> keywords = null;
    //    if (documentationUnitDTO.getKeywords() != null) {
    //      keywords =
    // documentationUnitDTO.getKeywords().stream().map(KeywordDTO::keyword).toList();
    //    }

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(fileNumbers)
            //            .deviatingFileNumbers(deviatingFileNumbers)
            //            .court(
            //                getCourtObject(documentationUnitDTO.getCourtType(),
            // documentationUnitDTO.getCourtLocation()))
            //            .incorrectCourts(incorrectCourts)
            .documentType(documentType)
            // .procedure(getProcedure(documentationUnitDTO.getProcedure()))
            //            .previousProcedures(documentationUnitDTO.getPreviousProcedures())
            .ecli(documentationUnitDTO.getEcli())
            //            .deviatingEclis(deviatingEclis)
            .appraisalBody(documentationUnitDTO.getJudicialBody())
            .decisionDate(
                documentationUnitDTO.getDecisionDate() == null
                    ? null
                    : documentationUnitDTO
                        .getDecisionDate()
                        .atStartOfDay(ZoneId.of("Europe/Berlin"))
                        .toInstant())
            // .deviatingDecisionDates(deviatingDecisionDates)
            // .legalEffect(documentationUnitDTO.getLegalEffect())
            .inputType(documentationUnitDTO.getInputType())
            // .documentationOffice(getDocumentationOffice(documentationUnitDTO.getDocumentationOffice()))
            // TODO multiple regions .region(documentationUnitDTO.getRegions())
            .build();
    Texts texts =
        Texts.builder()
            // TODO multiple decisionNames
            .decisionName(
                documentationUnitDTO.getDecisionNames().isEmpty()
                    ? null
                    : documentationUnitDTO.getDecisionNames().stream().findFirst().get().getValue())
            .headline(documentationUnitDTO.getHeadline())
            .guidingPrinciple(documentationUnitDTO.getGuidingPrinciple())
            .headnote(documentationUnitDTO.getHeadnote())
            .tenor(documentationUnitDTO.getTenor())
            .reasons(documentationUnitDTO.getGrounds())
            .caseFacts(documentationUnitDTO.getCaseFacts())
            .decisionReasons(documentationUnitDTO.getDecisionGrounds())
            .build();

    OriginalFileDocumentDTO originalFileDocumentDTO =
        documentationUnitDTO.getOriginalFileDocument();

    return DocumentUnit.builder()
        .uuid(documentationUnitDTO.getId())
        .documentNumber(documentationUnitDTO.getDocumentNumber())
        .fileuploadtimestamp(originalFileDocumentDTO.getUploadTimestamp())
        .s3path(originalFileDocumentDTO.getS3ObjectPath())
        .filetype(originalFileDocumentDTO.getExtension())
        .filename(originalFileDocumentDTO.getFilename())
        .coreData(coreData)
        // .proceedingDecisions(proceedingDecisions)
        .texts(texts)
        // .status(documentUnitDTO.getStatus())
        //        .contentRelatedIndexing(
        //            ContentRelatedIndexing.builder()
        //                .keywords(keywords)
        //                .fieldsOfLaw(fieldsOfLaw)
        //                .norms(norms)
        //                .activeCitations(activeCitations)
        //                .build())
        .build();
  }
}
