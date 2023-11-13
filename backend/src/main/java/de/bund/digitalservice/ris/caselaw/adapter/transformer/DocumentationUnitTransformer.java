package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO.DocumentationUnitDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitTransformer {
  private DocumentationUnitTransformer() {}

  public static DocumentationUnitDTO transformToDTO(
      DocumentationUnitDTO currentDto, DocumentUnit updatedDomainObject) {

    if (log.isDebugEnabled()) {
      log.debug("transform database documentation unit '{}'", currentDto.getId());
    }

    DocumentationUnitDTO.DocumentationUnitDTOBuilder builder =
        currentDto.toBuilder()
            .id(updatedDomainObject.uuid())
            .documentNumber(updatedDomainObject.documentNumber());

    // TODO Should we create an originalFileDocument if it doesn't exist since the
    // upload happens
    // somewhere else?
    // if (updatedDomainObject.filetype() != null
    // && updatedDomainObject.filename() != null
    // && updatedDomainObject.s3path() != null
    // && updatedDomainObject.fileuploadtimestamp() != null) {
    // builder.originalFileDocument(
    // OriginalFileDocumentDTO.builder()
    // .documentationUnitId(updatedDomainObject.uuid())
    // .extension(updatedDomainObject.filetype())
    // .filename(updatedDomainObject.filename())
    // .s3ObjectPath(updatedDomainObject.s3path())
    // .uploadTimestamp(updatedDomainObject.fileuploadtimestamp())
    // .build());
    // }

    if (updatedDomainObject.coreData() != null) {
      var coreData = updatedDomainObject.coreData();

      builder
          .ecli(coreData.ecli())
          .judicialBody(coreData.appraisalBody())
          .decisionDate(coreData.decisionDate())
          .inputType(coreData.inputType())
          .documentType(
              coreData.documentType() != null
                  ? DocumentTypeTransformer.transformToDTO(coreData.documentType())
                  : null)
          .court(CourtTransformer.transformToDTO(coreData.court()));

      addProcedures(currentDto, builder, coreData);

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
                  .rank((long) (i + 1))
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
                  .rank((long) (i + 1))
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
                  .rank((long) (i + 1))
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

      boolean courtWasAdded =
          currentDto.getCourt() == null
              && updatedDomainObject.coreData() != null
              && updatedDomainObject.coreData().court() != null;
      boolean courtWasDeleted =
          currentDto.getCourt() != null
              && (updatedDomainObject.coreData() == null
                  || updatedDomainObject.coreData().court() == null);
      boolean courtHasChanged =
          currentDto.getCourt() != null
              && updatedDomainObject.coreData() != null
              && updatedDomainObject.coreData().court() != null
              && updatedDomainObject.coreData().court().id() != currentDto.getCourt().getId();

      LegalEffectDTO legalEffectDTO = LegalEffectDTO.FALSCHE_ANGABE;
      var legalEffect =
          LegalEffect.deriveFrom(
              updatedDomainObject, courtWasAdded || courtWasDeleted || courtHasChanged);

      if (legalEffect != null) {
        switch (legalEffect) {
          case NO -> legalEffectDTO = LegalEffectDTO.NEIN;
          case YES -> legalEffectDTO = LegalEffectDTO.JA;
          case NOT_SPECIFIED -> legalEffectDTO = LegalEffectDTO.KEINE_ANGABE;
        }
      }

      builder.legalEffect(legalEffectDTO);

    } else {
      builder
          .procedures(new ArrayList<>())
          .ecli(null)
          .judicialBody(null)
          .decisionDate(null)
          .inputType(null)
          .court(null)
          .documentType(null)
          .documentationOffice(null);
    }

    List<PreviousDecision> previousDecisions = updatedDomainObject.previousDecisions();
    if (previousDecisions != null) {
      AtomicInteger i = new AtomicInteger(1);
      builder.previousDecisions(
          previousDecisions.stream()
              .map(PreviousDecisionTransformer::transformToDTO)
              .filter(Objects::nonNull)
              .peek(previousDecisionDTO -> previousDecisionDTO.setRank(i.getAndIncrement()))
              .toList());
    }

    List<EnsuingDecision> ensuingDecisions = updatedDomainObject.ensuingDecisions();
    if (ensuingDecisions != null) {

      List<EnsuingDecisionDTO> ensuingDecisionDTOs = new ArrayList<>();
      List<PendingDecisionDTO> pendingDecisionDTOs = new ArrayList<>();

      AtomicInteger i = new AtomicInteger(1);
      for (EnsuingDecision ensuingDecision : ensuingDecisions) {
        if (ensuingDecision.isPending()) {
          PendingDecisionDTO pendingDecisionDTO =
              PendingDecisionTransformer.transformToDTO(ensuingDecision);
          if (pendingDecisionDTO != null) {
            pendingDecisionDTO.setRank(i.getAndIncrement());
            pendingDecisionDTOs.add(pendingDecisionDTO);
          }
        } else {
          EnsuingDecisionDTO ensuingDecisionDTO =
              EnsuingDecisionTransformer.transformToDTO(ensuingDecision);
          if (ensuingDecisionDTO != null) {
            ensuingDecisionDTO.setRank(i.getAndIncrement());
            ensuingDecisionDTOs.add(ensuingDecisionDTO);
          }
        }
      }

      builder.ensuingDecisions(ensuingDecisionDTOs);
      builder.pendingDecisions(pendingDecisionDTOs);
    }

    if (updatedDomainObject.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = updatedDomainObject.contentRelatedIndexing();

      List<ActiveCitation> activeCitations = contentRelatedIndexing.activeCitations();
      if (activeCitations != null) {
        AtomicInteger i = new AtomicInteger(1);
        builder.activeCitations(
            activeCitations.stream()
                .map(ActiveCitationTransformer::transformToDTO)
                .filter(Objects::nonNull)
                .peek(activeCitationDTO -> activeCitationDTO.setRank(i.getAndIncrement()))
                .toList());
      }

      List<FieldOfLaw> fieldOfLaws = contentRelatedIndexing.fieldsOfLaw();
      if (fieldOfLaws != null) {
        builder.fieldsOfLaw(
            fieldOfLaws.stream()
                .map(
                    fieldOfLaw ->
                        FieldOfLawDTO.builder()
                            .id(fieldOfLaw.id())
                            .identifier(fieldOfLaw.identifier())
                            .build())
                .toList());
      }

      List<NormReference> norms = contentRelatedIndexing.norms();
      if (norms != null) {
        builder.normReferences(
            norms.stream().map(NormReferenceTransformer::transformToDTO).toList());
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
        builder.decisionNames(
            List.of(DecisionNameDTO.builder().value(texts.decisionName()).build()));
      } else {
        builder.decisionNames(Collections.emptyList());
      }
    } else {
      builder
          .decisionNames(Collections.emptyList())
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

  private static void addProcedures(
      DocumentationUnitDTO currentDto, DocumentationUnitDTOBuilder builder, CoreData coreData) {

    List<ProcedureDTO> procedureDTOs = currentDto.getProcedures();
    if ((procedureDTOs == null || procedureDTOs.isEmpty()) && coreData.procedure() != null) {
      if (coreData.procedure().id() == null) {
        builder.procedures(
            List.of(
                ProcedureDTO.builder()
                    .label(coreData.procedure().label())
                    .createdAt(Instant.now())
                    .documentationOffice(currentDto.getDocumentationOffice())
                    .build()));
      } else {
        builder.procedures(List.of(ProcedureDTO.builder().id(coreData.procedure().id()).build()));
      }
    } else if (procedureDTOs != null && !procedureDTOs.isEmpty()) {
      if (coreData.procedure().id() == null) {
        procedureDTOs.add(
            0,
            ProcedureDTO.builder()
                .label(coreData.procedure().label())
                .createdAt(Instant.now())
                .documentationOffice(currentDto.getDocumentationOffice())
                .build());
      } else {
        if (!coreData.procedure().id().equals(procedureDTOs.get(0).getId())) {
          procedureDTOs.add(0, ProcedureDTO.builder().id(coreData.procedure().id()).build());
        }
      }
      builder.procedures(procedureDTOs);
    }
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

    LegalEffect legalEffect = LegalEffect.NOT_SPECIFIED;
    if (documentationUnitDTO.getLegalEffect() != null) {
      switch (documentationUnitDTO.getLegalEffect()) {
        case NEIN -> legalEffect = LegalEffect.NO;
        case JA -> legalEffect = LegalEffect.YES;
      }
    }

    DocumentUnit.DocumentUnitBuilder builder = DocumentUnit.builder();
    CoreDataBuilder coreDataBuilder =
        CoreData.builder()
            .court(CourtTransformer.transformToDomain((documentationUnitDTO.getCourt())))
            .procedure(
                ProcedureTransformer.transformFirstToDomain(documentationUnitDTO.getProcedures()))
            .previousProcedures(
                ProcedureTransformer.transformPreviousProceduresToLabel(
                    documentationUnitDTO.getProcedures()))
            .documentationOffice(
                DocumentationOffice.builder()
                    .abbreviation(documentationUnitDTO.getDocumentationOffice().getAbbreviation())
                    .build())
            // TODO multiple regions
            .region(
                documentationUnitDTO.getRegions() == null
                        || documentationUnitDTO.getRegions().isEmpty()
                        || documentationUnitDTO.getRegions().get(0) == null
                    ? null
                    : documentationUnitDTO.getRegions().get(0).getCode())
            .ecli(documentationUnitDTO.getEcli())
            .decisionDate(documentationUnitDTO.getDecisionDate())
            .appraisalBody(documentationUnitDTO.getJudicialBody())
            .legalEffect(legalEffect.getLabel())
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
      coreDataBuilder.documentType(DocumentTypeTransformer.transformToDomain(documentTypeDTO));
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

    if (documentationUnitDTO.getNormReferences() != null) {
      List<NormReference> norms =
          documentationUnitDTO.getNormReferences().stream()
              .map(NormReferenceTransformer::transformToDomain)
              .toList();

      contentRelatedIndexingBuilder.norms(norms);
    }

    List<ActiveCitationDTO> activeCitationDTOS = documentationUnitDTO.getActiveCitations();
    if (activeCitationDTOS != null) {
      ActiveCitation[] activeCitations = new ActiveCitation[activeCitationDTOS.size()];
      for (int i = 0; i < activeCitationDTOS.size(); i++) {
        ActiveCitationDTO currentDTO = activeCitationDTOS.get(i);
        activeCitations[currentDTO.getRank() - 1] =
            ActiveCitationTransformer.transformToDomain(currentDTO);
      }
      contentRelatedIndexingBuilder.activeCitations(Arrays.stream(activeCitations).toList());
    }

    if (documentationUnitDTO.getFieldsOfLaw() != null) {
      List<FieldOfLaw> fieldOfLaws =
          documentationUnitDTO.getFieldsOfLaw().stream()
              .map(FieldOfLawTransformer::transformToDomain)
              .toList();

      contentRelatedIndexingBuilder.fieldsOfLaw(fieldOfLaws);
    }

    ContentRelatedIndexing contentRelatedIndexing = contentRelatedIndexingBuilder.build();

    Texts texts =
        Texts.builder()
            // TODO multiple decisionNames
            .decisionName(
                (documentationUnitDTO.getDecisionNames() == null
                        || documentationUnitDTO.getDecisionNames().isEmpty())
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

    if (documentationUnitDTO.getOriginalFileDocument() != null) {
      OriginalFileDocumentDTO originalFileDocumentDTO =
          documentationUnitDTO.getOriginalFileDocument();

      builder
          .fileuploadtimestamp(originalFileDocumentDTO.getUploadTimestamp())
          .s3path(originalFileDocumentDTO.getS3ObjectPath())
          .filetype(originalFileDocumentDTO.getExtension())
          .filename(originalFileDocumentDTO.getFilename());
    }

    List<PreviousDecisionDTO> previousDecisionDTOS = documentationUnitDTO.getPreviousDecisions();
    if (previousDecisionDTOS != null) {
      PreviousDecision[] previousDecisions = new PreviousDecision[previousDecisionDTOS.size()];
      for (int i = 0; i < previousDecisionDTOS.size(); i++) {
        PreviousDecisionDTO currentDTO = previousDecisionDTOS.get(i);
        previousDecisions[currentDTO.getRank() - 1] =
            PreviousDecisionTransformer.transformToDomain(currentDTO);
      }
      builder.previousDecisions(Arrays.stream(previousDecisions).toList());
    }

    List<EnsuingDecisionDTO> ensuingDecisionDTOs = documentationUnitDTO.getEnsuingDecisions();
    List<PendingDecisionDTO> pendingDecisionDTOs = documentationUnitDTO.getPendingDecisions();

    if (pendingDecisionDTOs != null || ensuingDecisionDTOs != null) {
      EnsuingDecision[] ensuingDecisions = new EnsuingDecision[ensuingDecisionDTOs.size()];
      for (EnsuingDecisionDTO currentDTO : ensuingDecisionDTOs) {
        ensuingDecisions[currentDTO.getRank() - 1] =
            EnsuingDecisionTransformer.transformToDomain(currentDTO);
      }

      if (pendingDecisionDTOs != null) {
        for (PendingDecisionDTO currentDTO : pendingDecisionDTOs) {
          ensuingDecisions[currentDTO.getRank() - 1] =
              PendingDecisionTransformer.transformToDomain(currentDTO);
        }
      }

      builder.ensuingDecisions(Arrays.stream(ensuingDecisions).toList());
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
