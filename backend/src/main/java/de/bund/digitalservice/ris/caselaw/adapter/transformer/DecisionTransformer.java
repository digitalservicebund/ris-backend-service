package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO.DecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for transforming a documentation unit object from its domain
 * representation into a database object and back
 */
@Slf4j
public class DecisionTransformer extends DocumentableTransformer {
  private DecisionTransformer() {}

  /**
   * Transforms a documentation unit object from its domain representation into a database object
   *
   * @param currentDto the current database documentation unit
   * @param updatedDomainObject the updated domain object, e.g. by a REST call
   * @return a transformed database object containing the changes from the @param
   *     updatedDomainObject
   */
  @SuppressWarnings({"java:S6541", "java:S3776"})
  public static DecisionDTO transformToDTO(DecisionDTO currentDto, Decision updatedDomainObject) {
    final var builder = currentDto.toBuilder();

    builder
        .id(updatedDomainObject.uuid())
        .documentNumber(updatedDomainObject.documentNumber())
        .version(updatedDomainObject.version())
        .inboxStatus(updatedDomainObject.inboxStatus());

    addPreviousDecisions(updatedDomainObject, builder);

    builder.note(
        StringUtils.isNullOrBlank(updatedDomainObject.note()) ? null : updatedDomainObject.note());

    addEnsuingAndPendingDecisions(updatedDomainObject, builder, currentDto);

    if (updatedDomainObject.coreData() != null) {
      var coreData = updatedDomainObject.coreData();

      builder
          .judicialBody(StringUtils.normalizeSpace(coreData.appraisalBody()))
          .date(coreData.decisionDate())
          .celexNumber(coreData.celexNumber())
          .documentType(
              coreData.documentType() != null
                  ? DocumentTypeTransformer.transformToDTO(coreData.documentType())
                  : null)
          .court(CourtTransformer.transformToDTO(coreData.court()));

      builder.ecli(StringUtils.normalizeSpace(coreData.ecli()));

      addInputTypes(builder, coreData);
      addDeviatingEclis(builder, coreData);
      addLegalEffect(currentDto, updatedDomainObject, builder);
      addLeadingDecisionNormReferences(updatedDomainObject, builder);
      addYearsOfDisputeToDTO(builder, coreData);

      addDeviatingDocumentNumbers(builder, coreData, currentDto);
      addFileNumbers(builder, coreData, currentDto);
      addDeviationCourts(builder, coreData);
      addDeviatingDecisionDates(builder, coreData);
      addDeviatingFileNumbers(builder, coreData, currentDto);
      addSource(currentDto, builder, updatedDomainObject);

    } else {
      builder
          .judicialBody(null)
          .date(null)
          .court(null)
          .documentType(null)
          .documentationOffice(null)
          .procedureHistory(Collections.emptyList())
          .procedure(null)
          .ecli(null)
          .yearsOfDispute(null);
    }

    if (updatedDomainObject.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = updatedDomainObject.contentRelatedIndexing();

      addNormReferences(builder, contentRelatedIndexing);

      addActiveCitations(builder, contentRelatedIndexing);
      addJobProfiles(builder, contentRelatedIndexing);
      addDismissalGrounds(builder, contentRelatedIndexing);
      addDismissalTypes(builder, contentRelatedIndexing);
      addCollectiveAgreements(builder, contentRelatedIndexing);
      builder.hasLegislativeMandate(contentRelatedIndexing.hasLegislativeMandate());
      builder.evsf(contentRelatedIndexing.evsf());
    }

    if (updatedDomainObject.longTexts() != null) {
      addLongTexts(updatedDomainObject, builder);
    } else {
      builder
          .tenor(null)
          .grounds(null)
          .caseFacts(null)
          .decisionGrounds(null)
          .dissentingOpinion(null)
          .otherLongText(null)
          .outline(null);
    }

    if (updatedDomainObject.shortTexts() != null) {
      addShortTexts(updatedDomainObject, builder, currentDto);
    } else {
      currentDto.getDecisionNames().clear();
      builder
          .decisionNames(currentDto.getDecisionNames())
          .guidingPrinciple(null)
          .headnote(null)
          .otherHeadnote(null)
          .headline(null);
    }

    // Calls to pre-build helper methods that populate the builder
    addCaselawReferences(updatedDomainObject, builder, currentDto);
    addLiteratureReferences(updatedDomainObject, builder, currentDto);
    addManagementData(updatedDomainObject, builder);

    DecisionDTO result = builder.build();

    return DocumentableTransformer.postProcessRelationships(result, currentDto);
  }

  // No need to check for null, when accessing max of a non-empty list.
  @SuppressWarnings("java:S3655")
  /*
   Currently, our UI and domain object only support one single source,
   however our DTO and the jDV support multiple sources.
   This is why we only update the source with the highest rank.

   A) If the domain object has an empty source, we do not update the sources list in the DTO.
   B) If the domain object has a source and the DTO has no sources, we add the source to the DTO.
   C) If the domain object has a source and the DTO has sources...
   C1) ... and if the value of the highest ranking source is the same -> we don't do anything.
   C2) ... and if the value of the highest ranking source is different -> we replace the source.
  */
  private static void addSource(
      DecisionDTO currentDto, DecisionDTOBuilder<?, ?> builder, Decision decision) {
    if (decision.coreData().source() == null) {
      return;
    }

    List<SourceDTO> existingSources =
        currentDto.getSource() != null
            ? new ArrayList<>(currentDto.getSource())
            : new ArrayList<>();

    // Create new SourceDTO
    SourceDTO newSource = SourceDTO.builder().value(decision.coreData().source().value()).build();

    if (existingSources.isEmpty()) {
      newSource.setRank(1);
      existingSources.add(newSource);
    } else {
      SourceDTO firstSource = existingSources.getFirst();
      var reference = firstSource.getReference();
      // Check if the first existing source has a reference and remove if it has been deleted in
      // domain object. A reference is only linked if the doc unit is created from a reference,
      // that's why it is enough to check the first reference.
      if (reference != null && !documentableContainsReferenceWithId(decision, reference.getId())) {
        firstSource.setReference(null); // Otherwise the source can not be deleted
      }

      var latestSource = existingSources.getLast();
      if (latestSource.getValue() != newSource.getValue()) {
        // If the user selected a source with a different value, we replace it.
        newSource.setRank(existingSources.size());
        existingSources.remove(latestSource);
        existingSources.add(newSource);
      }
      // else: If the user selected the same source or didn't change it, we don't do anything.
    }

    builder.source(existingSources); // Update builder with new list
  }

  private static void addLongTexts(Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {
    LongTexts longTexts = updatedDomainObject.longTexts();

    builder
        .tenor(longTexts.tenor())
        .grounds(longTexts.reasons())
        .caseFacts(longTexts.caseFacts())
        .decisionGrounds(longTexts.decisionReasons())
        .dissentingOpinion(longTexts.dissentingOpinion())
        .participatingJudges(
            ParticipatingJudgeTransformer.transformToDTO(longTexts.participatingJudges()))
        .otherLongText(longTexts.otherLongText())
        .outline(longTexts.outline());
  }

  private static void addShortTexts(
      Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder, DecisionDTO currentDto) {
    ShortTexts shortTexts = updatedDomainObject.shortTexts();

    builder
        .guidingPrinciple(shortTexts.guidingPrinciple())
        .headnote(shortTexts.headnote())
        .otherHeadnote(shortTexts.otherHeadnote())
        .headline(shortTexts.headline());

    var currentDecisionNames = currentDto.getDecisionNames();
    currentDecisionNames.clear();
    if (shortTexts.decisionName() != null) {
      currentDecisionNames.add(DecisionNameDTO.builder().value(shortTexts.decisionName()).build());
    }
    builder.decisionNames(currentDto.getDecisionNames());
  }

  private static void addActiveCitations(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.activeCitations() == null) {
      return;
    }

    AtomicInteger i = new AtomicInteger(1);
    builder.activeCitations(
        contentRelatedIndexing.activeCitations().stream()
            .map(ActiveCitationTransformer::transformToDTO)
            .filter(Objects::nonNull)
            .map(
                previousDecisionDTO -> {
                  previousDecisionDTO.setRank(i.getAndIncrement());
                  return previousDecisionDTO;
                })
            .toList());
  }

  private static void addJobProfiles(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.jobProfiles() == null) {
      return;
    }

    List<JobProfileDTO> jobProfileDTOs = new ArrayList<>();
    List<String> jobProfiles = contentRelatedIndexing.jobProfiles().stream().distinct().toList();

    for (int i = 0; i < jobProfiles.size(); i++) {
      jobProfileDTOs.add(JobProfileDTO.builder().value(jobProfiles.get(i)).rank(i + 1L).build());
    }

    builder.jobProfiles(jobProfileDTOs);
  }

  private static void addDismissalGrounds(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.dismissalGrounds() == null) {
      return;
    }

    List<DismissalGroundsDTO> dismissalGroundsDTOS = new ArrayList<>();
    List<String> dismissalGrounds =
        contentRelatedIndexing.dismissalGrounds().stream().distinct().toList();

    for (int i = 0; i < dismissalGrounds.size(); i++) {
      dismissalGroundsDTOS.add(
          DismissalGroundsDTO.builder().value(dismissalGrounds.get(i)).rank(i + 1L).build());
    }

    builder.dismissalGrounds(dismissalGroundsDTOS);
  }

  private static void addDismissalTypes(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.dismissalTypes() == null) {
      return;
    }

    List<DismissalTypesDTO> dismissalTypesDTOS = new ArrayList<>();
    List<String> dismissalTypes =
        contentRelatedIndexing.dismissalTypes().stream().distinct().toList();

    for (int i = 0; i < dismissalTypes.size(); i++) {
      dismissalTypesDTOS.add(
          DismissalTypesDTO.builder().value(dismissalTypes.get(i)).rank(i + 1L).build());
    }

    builder.dismissalTypes(dismissalTypesDTOS);
  }

  private static void addCollectiveAgreements(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.collectiveAgreements() == null) {
      return;
    }

    List<CollectiveAgreementDTO> collectiveAgreementDTOS = new ArrayList<>();
    List<String> collectiveAgreements =
        contentRelatedIndexing.collectiveAgreements().stream().distinct().toList();

    for (int i = 0; i < collectiveAgreements.size(); i++) {
      collectiveAgreementDTOS.add(
          CollectiveAgreementDTO.builder().value(collectiveAgreements.get(i)).rank(i + 1L).build());
    }

    builder.collectiveAgreements(collectiveAgreementDTOS);
  }

  private static void addEnsuingAndPendingDecisions(
      Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder, DecisionDTO currentDTO) {
    List<EnsuingDecision> ensuingDecisions = updatedDomainObject.ensuingDecisions();

    List<EnsuingDecisionDTO> ensuingDecisionDTOs = new ArrayList<>();
    List<PendingDecisionDTO> pendingDecisionDTOs = new ArrayList<>();

    if (ensuingDecisions != null) {
      List<UUID> ensuingDecisionIds =
          currentDTO.getEnsuingDecisions().stream().map(EnsuingDecisionDTO::getId).toList();
      List<UUID> pendingDecisionIds =
          currentDTO.getPendingDecisions().stream().map(PendingDecisionDTO::getId).toList();

      AtomicInteger i = new AtomicInteger(1);
      for (EnsuingDecision ensuingDecision : ensuingDecisions) {
        if (ensuingDecision.isPending()) {
          PendingDecisionDTO pendingDecisionDTO =
              PendingDecisionTransformer.transformToDTO(ensuingDecision);
          if (pendingDecisionDTO != null) {
            if (ensuingDecisionIds.contains(pendingDecisionDTO.getId())) {
              pendingDecisionDTO.setId(null);
            }
            pendingDecisionDTO.setRank(i.getAndIncrement());
            pendingDecisionDTOs.add(pendingDecisionDTO);
          }
        } else {
          EnsuingDecisionDTO ensuingDecisionDTO =
              EnsuingDecisionTransformer.transformToDTO(ensuingDecision);
          if (ensuingDecisionDTO != null) {
            if (pendingDecisionIds.contains(ensuingDecisionDTO.getId())) {
              ensuingDecisionDTO.setId(null);
            }
            ensuingDecisionDTO.setRank(i.getAndIncrement());
            ensuingDecisionDTOs.add(ensuingDecisionDTO);
          }
        }
      }
    }

    builder.ensuingDecisions(ensuingDecisionDTOs);
    builder.pendingDecisions(pendingDecisionDTOs);
  }

  private static void addLegalEffect(
      DecisionDTO currentDto, Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {

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
            && !updatedDomainObject.coreData().court().id().equals(currentDto.getCourt().getId());

    var legalEffect =
        LegalEffect.deriveFrom(
            updatedDomainObject, courtWasAdded || courtWasDeleted || courtHasChanged);

    builder.legalEffect(LegalEffectTransformer.transformToDTO(legalEffect));
  }

  private static void addLeadingDecisionNormReferences(
      Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {

    List<String> leadingDecisionNormReferences =
        updatedDomainObject.coreData().leadingDecisionNormReferences();
    if (leadingDecisionNormReferences != null) {
      AtomicInteger i = new AtomicInteger(1);
      builder.leadingDecisionNormReferences(
          leadingDecisionNormReferences.stream()
              .map(
                  normReference ->
                      LeadingDecisionNormReferenceDTO.builder()
                          .normReference(StringUtils.normalizeSpace(normReference))
                          .rank(i.getAndIncrement())
                          .build())
              .toList());
    } else {
      builder.leadingDecisionNormReferences(Collections.emptyList());
    }
  }

  private static void addDeviatingEclis(DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.deviatingEclis() == null) {
      return;
    }

    List<DeviatingEcliDTO> deviatingEcliDTOs = new ArrayList<>();
    List<String> deviatingEclis = coreData.deviatingEclis();

    for (int i = 0; i < deviatingEclis.size(); i++) {
      deviatingEcliDTOs.add(
          DeviatingEcliDTO.builder()
              .value(StringUtils.normalizeSpace(deviatingEclis.get(i)))
              .rank(i + 1L)
              .build());
    }

    builder.deviatingEclis(deviatingEcliDTOs);
  }

  private static void addInputTypes(DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.inputTypes() == null) {
      return;
    }

    List<InputTypeDTO> inputTypeDTOs = new ArrayList<>();
    List<String> inputTypes = coreData.inputTypes();

    for (int i = 0; i < inputTypes.size(); i++) {
      inputTypeDTOs.add(
          InputTypeDTO.builder()
              .value(StringUtils.normalizeSpace(inputTypes.get(i)))
              .rank(i + 1L)
              .build());
    }

    builder.inputTypes(inputTypeDTOs);
  }

  public static Decision transformToDomain(DecisionDTO decisionDTO) {
    return transformToDomain(decisionDTO, null);
  }

  /**
   * Transforms a documentation unit object from its database representation into a domain object
   * that is suitable to be consumed by clients of the REST service.
   *
   * @param decisionDTO the database documentation unit
   * @param user the {@link User}, may be null
   * @return a transformed domain object, or an empty domain object if the input is null
   */
  public static Decision transformToDomain(DecisionDTO decisionDTO, @Nullable User user) {
    if (decisionDTO == null) {
      throw new DocumentationUnitTransformerException("Document unit is null and won't transform");
    }

    log.debug("transfer database documentation unit '{}' to domain object", decisionDTO.getId());

    return Decision.builder()
        .note(decisionDTO.getNote())
        .version(decisionDTO.getVersion())
        .uuid(decisionDTO.getId())
        .documentNumber(decisionDTO.getDocumentNumber())
        .coreData(buildCoreData(decisionDTO))
        .shortTexts(buildShortTexts(decisionDTO))
        .longTexts(buildLongTexts(decisionDTO))
        .contentRelatedIndexing(buildContentRelatedIndexing(decisionDTO))
        .managementData(ManagementDataTransformer.transformToDomain(decisionDTO, user))
        .caselawReferences(
            decisionDTO.getCaselawReferences() == null
                ? new ArrayList<>()
                : decisionDTO.getCaselawReferences().stream()
                    .map(ReferenceTransformer::transformToDomain)
                    .toList())
        .literatureReferences(
            decisionDTO.getLiteratureReferences() == null
                ? new ArrayList<>()
                : decisionDTO.getLiteratureReferences().stream()
                    .map(ReferenceTransformer::transformToDomain)
                    .toList())
        .documentalists(
            decisionDTO.getDocumentalists() == null
                ? new ArrayList<>()
                : decisionDTO.getDocumentalists().stream().map(DocumentalistDTO::getValue).toList())
        .previousDecisions(getPreviousDecisions(decisionDTO))
        .attachments(buildAttachments(decisionDTO))
        .ensuingDecisions(buildEnsuingDecisions(decisionDTO))
        .status(getStatus(decisionDTO))
        .inboxStatus(decisionDTO.getInboxStatus())
        .currentProcessStep(
            DocumentationUnitProcessStepTransformer.toDomain(decisionDTO.getCurrentProcessStep()))
        .processSteps(
            decisionDTO.getProcessSteps().stream()
                .map(DocumentationUnitProcessStepTransformer::toDomain)
                .toList())
        .build();
  }

  private static ShortTexts buildShortTexts(DecisionDTO decisionDTO) {
    return ShortTexts.builder()
        .headline(decisionDTO.getHeadline())
        // TODO multiple decisionNames
        .decisionName(
            (decisionDTO.getDecisionNames() == null || decisionDTO.getDecisionNames().isEmpty())
                ? null
                : decisionDTO.getDecisionNames().stream()
                    .findFirst()
                    .map(DecisionNameDTO::getValue)
                    .orElse(null))
        .guidingPrinciple(decisionDTO.getGuidingPrinciple())
        .headnote(decisionDTO.getHeadnote())
        .otherHeadnote(decisionDTO.getOtherHeadnote())
        .build();
  }

  private static LongTexts buildLongTexts(DecisionDTO decisionDTO) {
    return LongTexts.builder()
        .tenor(decisionDTO.getTenor())
        .reasons(decisionDTO.getGrounds())
        .caseFacts(decisionDTO.getCaseFacts())
        .decisionReasons(decisionDTO.getDecisionGrounds())
        .dissentingOpinion(decisionDTO.getDissentingOpinion())
        .participatingJudges(
            ParticipatingJudgeTransformer.transformToDomain(decisionDTO.getParticipatingJudges()))
        .otherLongText(decisionDTO.getOtherLongText())
        .outline(decisionDTO.getOutline())
        .build();
  }

  private static CoreData buildCoreData(DecisionDTO decisionDTO) {
    CoreDataBuilder coreDataBuilder =
        DocumentableTransformer.buildMutualCoreData(decisionDTO).toBuilder();

    // decision specific fields
    LegalEffect legalEffect =
        LegalEffectTransformer.transformToDomain(decisionDTO.getLegalEffect());

    coreDataBuilder
        .ecli(decisionDTO.getEcli())
        .celexNumber(decisionDTO.getCelexNumber())
        .legalEffect(legalEffect == null ? null : legalEffect.getLabel())
        .procedure(ProcedureTransformer.transformToDomain(decisionDTO.getProcedure(), false))
        .previousProcedures(
            ProcedureTransformer.transformPreviousProceduresToLabel(
                decisionDTO.getProcedureHistory()))
        .creatingDocOffice(
            DocumentationOfficeTransformer.transformToDomain(
                decisionDTO.getCreatingDocumentationOffice()))
        .source(getSource(decisionDTO));

    addInputTypesToDomain(decisionDTO, coreDataBuilder);
    addLeadingDecisionNormReferencesToDomain(decisionDTO, coreDataBuilder);
    addYearsOfDisputeToDomain(decisionDTO, coreDataBuilder);
    addDeviatingEclisToDomain(decisionDTO, coreDataBuilder);
    return coreDataBuilder.build();
  }

  private static ContentRelatedIndexing buildContentRelatedIndexing(DecisionDTO decisionDTO) {
    ContentRelatedIndexing.ContentRelatedIndexingBuilder contentRelatedIndexingBuilder =
        DocumentableTransformer.buildContentRelatedIndexing(decisionDTO).toBuilder();

    if (decisionDTO.getActiveCitations() != null) {
      contentRelatedIndexingBuilder.activeCitations(
          decisionDTO.getActiveCitations().stream()
              .map(ActiveCitationTransformer::transformToDomain)
              .toList());
    }

    if (decisionDTO.getJobProfiles() != null) {
      List<String> jobProfiles =
          decisionDTO.getJobProfiles().stream().map(JobProfileDTO::getValue).toList();
      contentRelatedIndexingBuilder.jobProfiles(jobProfiles);
    }

    if (decisionDTO.getDismissalGrounds() != null) {
      List<String> dismissalGrounds =
          decisionDTO.getDismissalGrounds().stream().map(DismissalGroundsDTO::getValue).toList();
      contentRelatedIndexingBuilder.dismissalGrounds(dismissalGrounds);
    }

    if (decisionDTO.getDismissalTypes() != null) {
      List<String> dismissalTypes =
          decisionDTO.getDismissalTypes().stream().map(DismissalTypesDTO::getValue).toList();
      contentRelatedIndexingBuilder.dismissalTypes(dismissalTypes);
    }

    if (decisionDTO.getCollectiveAgreements() != null) {
      List<String> collectiveAgreements =
          decisionDTO.getCollectiveAgreements().stream()
              .map(CollectiveAgreementDTO::getValue)
              .toList();
      contentRelatedIndexingBuilder.collectiveAgreements(collectiveAgreements);
    }

    contentRelatedIndexingBuilder.hasLegislativeMandate(decisionDTO.isHasLegislativeMandate());
    contentRelatedIndexingBuilder.evsf(decisionDTO.getEvsf());

    return contentRelatedIndexingBuilder.build();
  }

  private static List<Attachment> buildAttachments(DecisionDTO decisionDTO) {
    return decisionDTO.getAttachments().stream()
        .map(AttachmentTransformer::transformToDomain)
        .filter(
            attachment -> "docx".equals(attachment.format()) || "fmx".equals(attachment.format()))
        .toList();
  }

  private static void addDeviatingEclisToDomain(
      DecisionDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getDeviatingEclis() == null) {
      return;
    }

    List<String> deviatingEclis =
        decisionDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getValue).toList();
    coreDataBuilder.deviatingEclis(deviatingEclis);
  }

  private static void addInputTypesToDomain(
      DecisionDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getInputTypes() == null) {
      return;
    }

    List<String> inputTypes =
        decisionDTO.getInputTypes().stream().map(InputTypeDTO::getValue).toList();
    coreDataBuilder.inputTypes(inputTypes);
  }

  private static void addLeadingDecisionNormReferencesToDomain(
      DecisionDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getLeadingDecisionNormReferences() == null) {
      return;
    }
    coreDataBuilder.leadingDecisionNormReferences(
        decisionDTO.getLeadingDecisionNormReferences().stream()
            .map(LeadingDecisionNormReferenceDTO::getNormReference)
            .toList());
  }

  private static List<EnsuingDecision> buildEnsuingDecisions(DecisionDTO decisionDTO) {

    if (decisionDTO.getPendingDecisions() == null && decisionDTO.getEnsuingDecisions() == null) {
      return List.of();
    }

    List<EnsuingDecision> withoutRank = new ArrayList<>();

    EnsuingDecision[] ensuingDecisions =
        new EnsuingDecision
            [getEnsuingDecisionListSize(
                decisionDTO.getEnsuingDecisions(), decisionDTO.getPendingDecisions())];

    addEnsuingDecisionToDomain(decisionDTO.getEnsuingDecisions(), withoutRank, ensuingDecisions);
    addPendingDecisionsToDomain(decisionDTO.getPendingDecisions(), withoutRank, ensuingDecisions);

    handleEnsuingDecisionsWithoutRank(withoutRank, ensuingDecisions);

    return Arrays.stream(ensuingDecisions).toList();
  }

  private static void addYearsOfDisputeToDTO(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, CoreData coreData) {

    if (coreData.yearsOfDispute() == null || coreData.yearsOfDispute().isEmpty()) {
      builder.yearsOfDispute(new LinkedHashSet<>());
      return;
    }

    Set<YearOfDisputeDTO> yearOfDisputeDTOS = new LinkedHashSet<>();

    for (int i = 0; i < coreData.yearsOfDispute().size(); i++) {
      yearOfDisputeDTOS.add(
          YearOfDisputeTransformer.transformToDTO(coreData.yearsOfDispute().get(i), i + 1));
    }
    builder.yearsOfDispute(yearOfDisputeDTOS);
  }

  static void addYearsOfDisputeToDomain(
      DecisionDTO currentDto, CoreData.CoreDataBuilder coreDataBuilder) {

    coreDataBuilder.yearsOfDispute(
        currentDto.getYearsOfDispute().stream()
            .map(YearOfDisputeTransformer::transformToDomain)
            .toList());
  }

  static Source getSource(DocumentationUnitDTO decisionDTO) {
    return decisionDTO.getSource().stream()
        .max(Comparator.comparing(SourceDTO::getRank)) // Find the highest-ranked item
        .map(
            sourceDTO -> {
              SourceValue sourceValue = null;
              if (sourceDTO.getValue() != null) {
                sourceValue = sourceDTO.getValue();
              }
              var reference =
                  Optional.ofNullable(sourceDTO.getReference())
                      .map(ReferenceTransformer::transformToDomain)
                      .orElse(null);
              return Source.builder()
                  .value(sourceValue)
                  .sourceRawValue(sourceDTO.getSourceRawValue())
                  .reference(reference)
                  .build();
            })
        .orElse(null);
  }
}
