package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitTransformer {
  private DocumentationUnitTransformer() {}

  public static DocumentationUnitDTO transformToDTO(
      DocumentationUnitDTO currentDto, DocumentUnit updatedDomainObject) {

    if (log.isDebugEnabled()) {
      log.debug("enrich database documentation unit '{}'", currentDto.getId());
    }

    // TODO needs null-checking
    // OriginalFileDocumentDTO originalFileDocument =
    // OriginalFileDocumentDTO.builder()
    // .extension(updatedDomainObject.filetype())
    // .filename(updatedDomainObject.filename())
    // .s3ObjectPath(updatedDomainObject.s3path())
    // .uploadTimestamp(updatedDomainObject.fileuploadtimestamp())
    // .build();

    DocumentationUnitDTO.DocumentationUnitDTOBuilder builder =
        currentDto.toBuilder()
            .id(updatedDomainObject.uuid())
            .documentNumber(updatedDomainObject.documentNumber());
    // .originalFileDocument(originalFileDocument)

    if (updatedDomainObject.coreData() != null) {
      var coreData = updatedDomainObject.coreData();

      builder
          .ecli(coreData.ecli())
          .judicialBody(coreData.appraisalBody())
          .decisionDate(coreData.decisionDate())
          .inputType(coreData.inputType());

      var fileNumbers = coreData.fileNumbers();
      if (fileNumbers != null && !fileNumbers.isEmpty()) {
        builder.fileNumbers(
            fileNumbers.stream()
                .map(
                    fileNumber ->
                        FileNumberDTO.builder()
                            // TODO do we have to use the fileNumber repo instead?
                            .value(fileNumber)
                            .documentationUnit(currentDto)
                            .build())
                .toList());
      }

      if (coreData.deviatingCourts() != null) {
        List<DeviatingCourtDTO> deviatingCourtDTOs = new ArrayList<>();
        List<String> deviatingCourts = coreData.deviatingCourts();
        for (int i = 0; i < deviatingCourts.size(); i++) {
          deviatingCourtDTOs.add(
              DeviatingCourtDTO.builder()
                  .value(deviatingCourts.get(i))
                  .rank(Long.valueOf(i + 1))
                  .build());
        }
        builder.deviatingCourts(deviatingCourtDTOs);
      }

      if (coreData.deviatingDecisionDates() != null) {
        List<DeviatingDateDTO> deviatingDateDTOs = new ArrayList<>();
        List<LocalDate> deviatingDecisionDates = coreData.deviatingDecisionDates();
        for (int i = 0; i < deviatingDecisionDates.size(); i++) {
          deviatingDateDTOs.add(
              DeviatingDateDTO.builder()
                  .value(deviatingDecisionDates.get(i))
                  .rank(Long.valueOf(i + 1))
                  .build());
        }
        builder.deviatingDates(deviatingDateDTOs);
      }

      if (coreData.deviatingFileNumbers() != null) {
        List<DeviatingFileNumberDTO> deviatingFileNumberDTOs = new ArrayList<>();
        List<String> deviatingFileNumbers = coreData.deviatingFileNumbers();
        for (int i = 0; i < deviatingFileNumbers.size(); i++) {
          deviatingFileNumberDTOs.add(
              DeviatingFileNumberDTO.builder()
                  .value(deviatingFileNumbers.get(i))
                  .rank(Long.valueOf(i + 1))
                  .build());
        }
        builder.deviatingFileNumbers(deviatingFileNumberDTOs);
      }

      if (coreData.deviatingEclis() != null) {
        List<DeviatingEcliDTO> deviatingEcliDTOs = new ArrayList<>();
        List<String> deviatingEclis = coreData.deviatingEclis();
        for (int i = 0; i < deviatingEclis.size(); i++) {
          deviatingEcliDTOs.add(
              DeviatingEcliDTO.builder()
                  .value(deviatingEclis.get(i))
                  .rank(Long.valueOf(i + 1))
                  .build());
        }
        builder.deviatingEclis(deviatingEcliDTOs);
      }

      // TODO documentationOffice

      // .normReferences(
      // updatedDomainObject.contentRelatedIndexing().norms().stream()
      // .map(
      // norm ->
      // NormReferenceDTO.builder()
      // // TODO do we have to use the normAbbreviation repo instead?
      // .normAbbreviation(
      // NormAbbreviationDTO.builder()
      // .id(norm.normAbbreviation().id())
      // .build())
      // .singleNorm(norm.singleNorm())
      // .dateOfVersion(norm.dateOfVersion())
      // .dateOfRelevance(norm.dateOfRelevance())
      // .build())
      // .collect(Collectors.toSet()));

      // TODO nullchecks
      // var legalEffect =
      // LegalEffect.deriveLegalEffectFrom(
      // updatedDomainObject, hasCourtChanged(currentDto, updatedDomainObject));

      // LegalEffectDTO legalEffectDTO;
      // switch (legalEffect) {
      // case NO -> legalEffectDTO = LegalEffectDTO.NEIN;
      // case YES -> legalEffectDTO = LegalEffectDTO.JA;
      // case NOT_SPECIFIED -> legalEffectDTO = LegalEffectDTO.KEINE_ANGABE;
      // default -> legalEffectDTO = LegalEffectDTO.FALSCHE_ANGABE;
      // }
      // builder.legalEffect(legalEffectDTO);ich hoffe nicht.

    } else {
      builder
          .procedures(new ArrayList<>())
          .ecli(null)
          .judicialBody(null)
          .decisionDate(null)
          .inputType(null)
          .court(null)
          .documentationOffice(null);
    }

    List<PreviousDecision> previousDecisions = updatedDomainObject.previousDecisions();
    if (previousDecisions != null && !previousDecisions.isEmpty()) {
      builder.previousDecisions(
          previousDecisions.stream().map(PreviousDecisionTransformer::transformToDTO).toList());
    }

    if (updatedDomainObject.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = updatedDomainObject.contentRelatedIndexing();

      List<ActiveCitation> activeCitations = contentRelatedIndexing.activeCitations();
      if (activeCitations != null && !activeCitations.isEmpty()) {
        builder.activeCitations(
            activeCitations.stream()
                .map(ActiveCitationTransformer::transformToDTO)
                .filter(Objects::nonNull)
                .toList());
      }
    }

    if (updatedDomainObject.texts() != null) {
      Texts texts = updatedDomainObject.texts();

      builder
          .headline(texts.headline())
          .guidingPrinciple(texts.guidingPrinciple())
          .headnote(texts.headnote())
          .tenor(texts.tenor())
          .grounds(texts.reasons())
          .caseFacts(texts.caseFacts())
          .decisionGrounds(texts.decisionReasons());

      if (texts.decisionName() != null) {
        // Todo multiple decision names?
        //
        // builder.decisionNames(Set.of(DecisionNameDTO.builder().value(texts.decisionName()).build()));
      }
    } else {
      builder
          // .decisionNames(null)
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
        || !Objects.equals(
            documentUnitDTO.getCourt().getType(), documentUnit.coreData().court().type())
        || !Objects.equals(
            documentUnitDTO.getCourt().getLocation(), documentUnit.coreData().court().location());
  }

  public static DocumentUnit transformToDomain(DocumentationUnitDTO documentationUnitDTO) {
    if (log.isDebugEnabled()) {
      log.debug(
          "transfer database documentation unit '{}' to domain object",
          documentationUnitDTO.getId());
    }

    if (documentationUnitDTO == null) {
      return DocumentUnit.builder().build();
    }

    DocumentUnit.DocumentUnitBuilder builder = DocumentUnit.builder();

    CoreDataBuilder coreDataBuilder =
        CoreData.builder()
            .court(CourtTransformer.transformToDomain((documentationUnitDTO.getCourt())))
            .procedure(ProcedureTransformer.transformToDomain(documentationUnitDTO.getProcedures()))
            // .previousProcedures(documentationUnitDTO.getPreviousProcedures())
            .documentationOffice(
                DocumentationOffice.builder()
                    .abbreviation(documentationUnitDTO.getDocumentationOffice().getAbbreviation())
                    .build())
            // TODO multiple regions .region(documentationUnitDTO.getRegions())
            .ecli(documentationUnitDTO.getEcli())
            .decisionDate(documentationUnitDTO.getDecisionDate())
            .appraisalBody(documentationUnitDTO.getJudicialBody())
            // .legalEffect(documentationUnitDTO.getLegalEffect().toString())
            .inputType(documentationUnitDTO.getInputType());

    List<String> fileNumbers = null;
    if (documentationUnitDTO.getFileNumbers() != null) {
      fileNumbers =
          documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
      coreDataBuilder.fileNumbers(fileNumbers);
    }

    if (documentationUnitDTO.getDeviatingFileNumbers() != null) {
      List<String> deviatingFileNumbers = null;
      deviatingFileNumbers =
          documentationUnitDTO.getDeviatingFileNumbers().stream()
              .map(DeviatingFileNumberDTO::getValue)
              .toList();
      coreDataBuilder.deviatingFileNumbers(deviatingFileNumbers);
    }

    if (documentationUnitDTO.getDeviatingCourts() != null) {
      List<String> deviatingCourts = null;
      deviatingCourts =
          documentationUnitDTO.getDeviatingCourts().stream()
              .map(DeviatingCourtDTO::getValue)
              .toList();
      coreDataBuilder.deviatingCourts(deviatingCourts);
    }

    DocumentTypeDTO documentTypeDTO = documentationUnitDTO.getDocumentType();
    if (documentTypeDTO != null) {
      DocumentType documentType = null;
      documentType =
          new DocumentType(documentTypeDTO.getAbbreviation(), documentTypeDTO.getLabel());
      coreDataBuilder.documentType(documentType);
    }

    if (documentationUnitDTO.getDeviatingEclis() != null) {
      List<String> deviatingEclis = null;
      deviatingEclis =
          documentationUnitDTO.getDeviatingEclis().stream()
              .map(DeviatingEcliDTO::getValue)
              .toList();
      coreDataBuilder.deviatingEclis(deviatingEclis);
    }

    if (documentationUnitDTO.getDeviatingDates() != null) {
      List<LocalDate> deviatingDecisionDates = null;
      deviatingDecisionDates =
          documentationUnitDTO.getDeviatingDates().stream()
              .map(DeviatingDateDTO::getValue)
              .toList();
      coreDataBuilder.deviatingDecisionDates(deviatingDecisionDates);
    }

    CoreData coreData = coreDataBuilder.build();

    ContentRelatedIndexing.ContentRelatedIndexingBuilder contentRelatedIndexingBuilder =
        ContentRelatedIndexing.builder();

    if (documentationUnitDTO.getKeywords() != null) {
      List<String> keywords =
          documentationUnitDTO.getKeywords().stream().map(KeywordDTO::getValue).toList();
      contentRelatedIndexingBuilder.keywords(keywords);
    }

    // List<FieldOfLaw> fieldsOfLaw = null;
    // if (documentationUnitDTO.getFieldsOfLaw() != null) {
    // fieldsOfLaw =
    // documentationUnitDTO.getFieldsOfLaw().stream()
    // .map(FieldOfLawTransformer::transformToDomain)
    // .toList();
    // }

    if (documentationUnitDTO.getNormReferences() != null) {
      List<DocumentUnitNorm> norms = null;
      norms =
          documentationUnitDTO.getNormReferences().stream()
              .map(DocumentUnitNormTransformer::transformToDomain)
              .toList();

      contentRelatedIndexingBuilder.norms(norms);
    }

    if (documentationUnitDTO.getActiveCitations() != null) {
      List<ActiveCitation> activeCitations = null;

      activeCitations =
          documentationUnitDTO.getActiveCitations().stream()
              .map(ActiveCitationTransformer::transformToDomain)
              .toList();

      contentRelatedIndexingBuilder.activeCitations(activeCitations);
    }

    ContentRelatedIndexing contentRelatedIndexing = contentRelatedIndexingBuilder.build();

    Texts texts =
        Texts.builder()
            // TODO multiple decisionNames
            // .decisionName(
            // documentationUnitDTO.getDecisionNames().isEmpty()
            // ? null
            // :
            // documentationUnitDTO.getDecisionNames().stream().findFirst().get().getValue())
            .headline(documentationUnitDTO.getHeadline())
            .guidingPrinciple(documentationUnitDTO.getGuidingPrinciple())
            .headnote(documentationUnitDTO.getHeadnote())
            .tenor(documentationUnitDTO.getTenor())
            .reasons(documentationUnitDTO.getGrounds())
            .caseFacts(documentationUnitDTO.getCaseFacts())
            .decisionReasons(documentationUnitDTO.getDecisionGrounds())
            .build();

    if (documentationUnitDTO.getOriginalFileDocument() != null) {
      OriginalFileDocumentDTO originalFileDocumentDTO =
          documentationUnitDTO.getOriginalFileDocument();

      builder
          .fileuploadtimestamp(originalFileDocumentDTO.getUploadTimestamp())
          .s3path(originalFileDocumentDTO.getS3ObjectPath())
          .filetype(originalFileDocumentDTO.getExtension())
          .filename(originalFileDocumentDTO.getFilename());
    }

    List<PreviousDecision> previousDecisions = null;
    if (documentationUnitDTO.getPreviousDecisions() != null) {
      previousDecisions =
          documentationUnitDTO.getPreviousDecisions().stream()
              .map(PreviousDecisionTransformer::transformToDomain)
              .toList();
      builder.previousDecisions(previousDecisions);
    }

    builder
        .uuid(documentationUnitDTO.getId())
        .documentNumber(documentationUnitDTO.getDocumentNumber())
        .coreData(coreData)
        .texts(texts)
        .contentRelatedIndexing(contentRelatedIndexing);

    if (documentationUnitDTO.getStatus() != null && !documentationUnitDTO.getStatus().isEmpty()) {
      StatusDTO statusDTO = documentationUnitDTO.getStatus().get(0);
      builder.status(
          DocumentUnitStatus.builder()
              .publicationStatus(statusDTO.getPublicationStatus())
              .withError(statusDTO.isWithError())
              .build());
    }

    return builder.build();
  }
}
