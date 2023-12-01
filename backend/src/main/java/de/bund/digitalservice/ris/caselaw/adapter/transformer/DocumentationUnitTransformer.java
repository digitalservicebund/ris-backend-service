package de.bund.digitalservice.ris.caselaw.adapter.transformer;

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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry.DocumentUnitListEntryBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Status;
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
import org.jetbrains.annotations.NotNull;

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
      addFileNumbers(currentDto, builder, coreData);
      addDeviationCourts(builder, coreData);
      addDeviatingDecisionDates(builder, coreData);
      addDeviatingFileNumbers(currentDto, builder, coreData);
      addDeviatingEclis(builder, coreData);
      addLegalEffect(currentDto, updatedDomainObject, builder);

    } else {
      builder
          .procedures(Collections.emptyList())
          .ecli(null)
          .judicialBody(null)
          .decisionDate(null)
          .inputType(null)
          .court(null)
          .documentType(null)
          .documentationOffice(null);
    }

    addPreviousDecisions(updatedDomainObject, builder);
    addEnsuingAndPendingDecisions(updatedDomainObject, builder);

    if (updatedDomainObject.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = updatedDomainObject.contentRelatedIndexing();

      addActiveCitations(builder, contentRelatedIndexing);
      addFieldsOfLaw(builder, contentRelatedIndexing);
      addNormReferences(builder, contentRelatedIndexing);
    }

    if (updatedDomainObject.texts() != null) {
      addTexts(updatedDomainObject, builder);
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

  private static void addTexts(
      DocumentUnit updatedDomainObject, DocumentationUnitDTOBuilder builder) {
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
      builder.decisionNames(List.of(DecisionNameDTO.builder().value(texts.decisionName()).build()));
    } else {
      builder.decisionNames(Collections.emptyList());
    }
  }

  private static void addNormReferences(
      DocumentationUnitDTOBuilder builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.norms() == null) {
      return;
    }

    builder.normReferences(
        contentRelatedIndexing.norms().stream()
            .map(NormReferenceTransformer::transformToDTO)
            .toList());
  }

  private static void addFieldsOfLaw(
      DocumentationUnitDTOBuilder builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.fieldsOfLaw() == null) {
      return;
    }

    builder.fieldsOfLaw(
        contentRelatedIndexing.fieldsOfLaw().stream()
            .map(
                fieldOfLaw ->
                    FieldOfLawDTO.builder()
                        .id(fieldOfLaw.id())
                        .identifier(fieldOfLaw.identifier())
                        .build())
            .toList());
  }

  private static void addActiveCitations(
      DocumentationUnitDTOBuilder builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.activeCitations() == null) {
      return;
    }

    AtomicInteger i = new AtomicInteger(1);
    builder.activeCitations(
        contentRelatedIndexing.activeCitations().stream()
            .map(ActiveCitationTransformer::transformToDTO)
            .filter(Objects::nonNull)
            .map(
                activeCitationDTO -> {
                  activeCitationDTO.setRank(i.getAndIncrement());
                  return activeCitationDTO;
                })
            .distinct()
            .toList());
  }

  private static void addEnsuingAndPendingDecisions(
      DocumentUnit updatedDomainObject, DocumentationUnitDTOBuilder builder) {
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
  }

  private static void addPreviousDecisions(
      DocumentUnit updatedDomainObject, DocumentationUnitDTOBuilder builder) {
    List<PreviousDecision> previousDecisions = updatedDomainObject.previousDecisions();
    if (previousDecisions != null) {
      AtomicInteger i = new AtomicInteger(1);
      builder.previousDecisions(
          previousDecisions.stream()
              .map(PreviousDecisionTransformer::transformToDTO)
              .filter(Objects::nonNull)
              .map(
                  previousDecisionDTO -> {
                    previousDecisionDTO.setRank(i.getAndIncrement());
                    return previousDecisionDTO;
                  })
              .toList());
    }
  }

  private static void addLegalEffect(
      DocumentationUnitDTO currentDto,
      DocumentUnit updatedDomainObject,
      DocumentationUnitDTOBuilder builder) {
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
  }

  private static void addDeviatingEclis(DocumentationUnitDTOBuilder builder, CoreData coreData) {
    if (coreData.deviatingEclis() == null) {
      return;
    }

    List<DeviatingEcliDTO> deviatingEcliDTOs = new ArrayList<>();
    List<String> deviatingEclis = coreData.deviatingEclis();

    for (int i = 0; i < deviatingEclis.size(); i++) {
      deviatingEcliDTOs.add(
          DeviatingEcliDTO.builder().value(deviatingEclis.get(i)).rank(i + 1L).build());
    }

    builder.deviatingEclis(deviatingEcliDTOs);
  }

  private static void addDeviatingFileNumbers(
      DocumentationUnitDTO currentDto, DocumentationUnitDTOBuilder builder, CoreData coreData) {
    if (coreData.deviatingFileNumbers() == null) {
      return;
    }

    List<DeviatingFileNumberDTO> deviatingFileNumberDTOs = new ArrayList<>();
    List<String> deviatingFileNumbers = coreData.deviatingFileNumbers();

    for (int i = 0; i < deviatingFileNumbers.size(); i++) {
      deviatingFileNumberDTOs.add(
          DeviatingFileNumberDTO.builder()
              .value(deviatingFileNumbers.get(i))
              .rank(i + 1L)
              .documentationUnit(currentDto)
              .build());
    }

    builder.deviatingFileNumbers(deviatingFileNumberDTOs);
  }

  private static void addDeviatingDecisionDates(
      DocumentationUnitDTOBuilder builder, CoreData coreData) {
    if (coreData.deviatingDecisionDates() == null) {
      return;
    }

    List<DeviatingDateDTO> deviatingDateDTOs = new ArrayList<>();
    List<LocalDate> deviatingDecisionDates = coreData.deviatingDecisionDates();

    for (int i = 0; i < deviatingDecisionDates.size(); i++) {
      deviatingDateDTOs.add(
          DeviatingDateDTO.builder().value(deviatingDecisionDates.get(i)).rank(i + 1L).build());
    }

    builder.deviatingDates(deviatingDateDTOs);
  }

  private static void addDeviationCourts(DocumentationUnitDTOBuilder builder, CoreData coreData) {
    if (coreData.deviatingCourts() == null) {
      return;
    }

    List<DeviatingCourtDTO> deviatingCourtDTOs = new ArrayList<>();
    List<String> deviatingCourts = coreData.deviatingCourts();

    for (int i = 0; i < deviatingCourts.size(); i++) {
      deviatingCourtDTOs.add(
          DeviatingCourtDTO.builder().value(deviatingCourts.get(i)).rank(i + 1L).build());
    }

    builder.deviatingCourts(deviatingCourtDTOs);
  }

  private static void addFileNumbers(
      DocumentationUnitDTO currentDto, DocumentationUnitDTOBuilder builder, CoreData coreData) {
    if (coreData.fileNumbers() == null) {
      return;
    }

    List<FileNumberDTO> fileNumberDTOs = new ArrayList<>();
    List<String> fileNumbers = coreData.fileNumbers();

    for (int i = 0; i < fileNumbers.size(); i++) {
      fileNumberDTOs.add(
          FileNumberDTO.builder()
              .value(fileNumbers.get(i))
              .rank(i + 1L)
              .documentationUnit(currentDto)
              .build());
    }

    builder.fileNumbers(fileNumberDTOs);
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

    LegalEffect legalEffect = getLegalEffectForDomain(documentationUnitDTO);

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

    addFileNumbersToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingFileNumbersToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingCourtsToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingEclisToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingDecisionDatesToDomain(documentationUnitDTO, coreDataBuilder);

    DocumentTypeDTO documentTypeDTO = documentationUnitDTO.getDocumentType();
    if (documentTypeDTO != null) {
      coreDataBuilder.documentType(DocumentTypeTransformer.transformToDomain(documentTypeDTO));
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

    if (documentationUnitDTO.getActiveCitations() != null) {
      contentRelatedIndexingBuilder.activeCitations(
          documentationUnitDTO.getActiveCitations().stream()
              .map(ActiveCitationTransformer::transformToDomain)
              .toList());
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

    addPreviousDecisionsToDomain(documentationUnitDTO, builder);
    addEnsuingDecisionsToDomain(documentationUnitDTO, builder);

    builder
        .uuid(documentationUnitDTO.getId())
        .documentNumber(documentationUnitDTO.getDocumentNumber())
        .coreData(coreData)
        .texts(texts)
        .contentRelatedIndexing(contentRelatedIndexing);

    addStatusToDomain(documentationUnitDTO, builder);

    return builder.build();
  }

  @NotNull
  private static LegalEffect getLegalEffectForDomain(DocumentationUnitDTO documentationUnitDTO) {
    LegalEffect legalEffect = LegalEffect.NOT_SPECIFIED;

    if (documentationUnitDTO.getLegalEffect() != null) {
      if (documentationUnitDTO.getLegalEffect() == LegalEffectDTO.NEIN) {
        legalEffect = LegalEffect.NO;
      } else if (documentationUnitDTO.getLegalEffect() == LegalEffectDTO.JA) {
        legalEffect = LegalEffect.YES;
      }
    }

    return legalEffect;
  }

  private static void addStatusToDomain(
      DocumentationUnitDTO documentationUnitDTO, DocumentUnitBuilder builder) {
    if (documentationUnitDTO.getStatus() == null || documentationUnitDTO.getStatus().isEmpty()) {
      return;
    }

    StatusDTO statusDTO = documentationUnitDTO.getStatus().get(0);
    builder.status(
        Status.builder()
            .publicationStatus(statusDTO.getPublicationStatus())
            .withError(statusDTO.isWithError())
            .build());
  }

  private static void addPreviousDecisionsToDomain(
      DocumentationUnitDTO documentationUnitDTO, DocumentUnitBuilder builder) {
    if (documentationUnitDTO.getPreviousDecisions() == null) {
      return;
    }

    builder.previousDecisions(
        documentationUnitDTO.getPreviousDecisions().stream()
            .map(PreviousDecisionTransformer::transformToDomain)
            .toList());
  }

  private static void addDeviatingDecisionDatesToDomain(
      DocumentationUnitDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingDates() == null) {
      return;
    }

    List<LocalDate> deviatingDecisionDates =
        documentationUnitDTO.getDeviatingDates().stream().map(DeviatingDateDTO::getValue).toList();
    coreDataBuilder.deviatingDecisionDates(deviatingDecisionDates);
  }

  private static void addDeviatingEclisToDomain(
      DocumentationUnitDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingEclis() == null) {
      return;
    }

    List<String> deviatingEclis =
        documentationUnitDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getValue).toList();
    coreDataBuilder.deviatingEclis(deviatingEclis);
  }

  private static void addDeviatingCourtsToDomain(
      DocumentationUnitDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingCourts() == null) {
      return;
    }

    List<String> deviatingCourts =
        documentationUnitDTO.getDeviatingCourts().stream()
            .map(DeviatingCourtDTO::getValue)
            .toList();
    coreDataBuilder.deviatingCourts(deviatingCourts);
  }

  private static void addDeviatingFileNumbersToDomain(
      DocumentationUnitDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingFileNumbers() == null) {
      return;
    }

    List<String> deviatingFileNumbers =
        documentationUnitDTO.getDeviatingFileNumbers().stream()
            .map(DeviatingFileNumberDTO::getValue)
            .toList();
    coreDataBuilder.deviatingFileNumbers(deviatingFileNumbers);
  }

  private static void addFileNumbersToDomain(
      DocumentationUnitDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getFileNumbers() == null) {
      return;
    }

    List<String> fileNumbers =
        documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    coreDataBuilder.fileNumbers(fileNumbers);
  }

  private static void addEnsuingDecisionsToDomain(
      DocumentationUnitDTO documentationUnitDTO, DocumentUnitBuilder builder) {

    List<EnsuingDecisionDTO> ensuingDecisionDTOs = documentationUnitDTO.getEnsuingDecisions();
    List<PendingDecisionDTO> pendingDecisionDTOs = documentationUnitDTO.getPendingDecisions();

    if (pendingDecisionDTOs == null && ensuingDecisionDTOs == null) {
      return;
    }

    int size = getEnsuingDecisionListSize(ensuingDecisionDTOs, pendingDecisionDTOs);

    List<EnsuingDecision> withoutRank = new ArrayList<>();
    EnsuingDecision[] ensuingDecisions = new EnsuingDecision[size];

    addEnsuingDecisionToDomain(ensuingDecisionDTOs, withoutRank, ensuingDecisions);
    addPendingDecisionsToDomain(pendingDecisionDTOs, withoutRank, ensuingDecisions);

    handleEnsuingDecisionsWithoutRank(withoutRank, ensuingDecisions);

    builder.ensuingDecisions(Arrays.stream(ensuingDecisions).toList());
  }

  private static void handleEnsuingDecisionsWithoutRank(
      List<EnsuingDecision> withoutRank, EnsuingDecision[] ensuingDecisions) {
    if (withoutRank.isEmpty()) {
      return;
    }

    int j = 0;
    for (int i = 0; i < ensuingDecisions.length; i++) {
      if (ensuingDecisions[i] == null) {
        ensuingDecisions[i] = withoutRank.get(j++);
      }
    }

    if (j < withoutRank.size()) {
      log.error(
          "ensuing decision - adding ensuing decisions without rank has more elements than expected.");
    }
  }

  private static void addPendingDecisionsToDomain(
      List<PendingDecisionDTO> pendingDecisionDTOs,
      List<EnsuingDecision> withoutRank,
      EnsuingDecision[] ensuingDecisions) {
    if (pendingDecisionDTOs == null) {
      return;
    }

    for (PendingDecisionDTO currentDTO : pendingDecisionDTOs) {
      if (currentDTO.getRank() > 0) {
        ensuingDecisions[currentDTO.getRank() - 1] =
            PendingDecisionTransformer.transformToDomain(currentDTO);
      } else {
        withoutRank.add(PendingDecisionTransformer.transformToDomain(currentDTO));
      }
    }
  }

  private static void addEnsuingDecisionToDomain(
      List<EnsuingDecisionDTO> ensuingDecisionDTOs,
      List<EnsuingDecision> withoutRank,
      EnsuingDecision[] ensuingDecisions) {
    if (ensuingDecisionDTOs == null) {
      return;
    }

    for (EnsuingDecisionDTO currentDTO : ensuingDecisionDTOs) {
      if (currentDTO.getRank() > 0) {
        ensuingDecisions[currentDTO.getRank() - 1] =
            EnsuingDecisionTransformer.transformToDomain(currentDTO);
      } else {
        withoutRank.add(EnsuingDecisionTransformer.transformToDomain(currentDTO));
      }
    }
  }

  private static int getEnsuingDecisionListSize(
      List<EnsuingDecisionDTO> ensuingDecisionDTOs, List<PendingDecisionDTO> pendingDecisionDTOs) {
    int size = 0;

    if (ensuingDecisionDTOs != null) {
      size += ensuingDecisionDTOs.size();
    }

    if (pendingDecisionDTOs != null) {
      size += pendingDecisionDTOs.size();
    }

    return size;
  }

  public static DocumentUnitListEntry transformToMetaDomain(
      DocumentationUnitDTO documentationUnitDTO) {
    DocumentUnitListEntryBuilder builder =
        DocumentUnitListEntry.builder()
            .documentNumber(documentationUnitDTO.getDocumentNumber())
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    documentationUnitDTO.getDocumentationOffice()))
            .documentType(
                DocumentTypeTransformer.transformToDomain(documentationUnitDTO.getDocumentType()))
            .decisionDate(documentationUnitDTO.getDecisionDate());

    if (documentationUnitDTO.getFileNumbers() != null
        && !documentationUnitDTO.getFileNumbers().isEmpty()) {
      builder.fileNumber(documentationUnitDTO.getFileNumbers().get(0).getValue());
    }

    if (documentationUnitDTO.getStatus() != null && !documentationUnitDTO.getStatus().isEmpty()) {
      builder.status(StatusTransformer.transformToDomain(documentationUnitDTO.getStatus().get(0)));
    }

    return builder.build();
  }
}
