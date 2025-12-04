package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO.DecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DefinitionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ForeignLanguageVersionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NonApplicationNormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ObjectValueDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OralHearingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmission;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreement;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.Definition;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.NonApplicationNorm;
import de.bund.digitalservice.ris.caselaw.domain.ObjectValue;
import de.bund.digitalservice.ris.caselaw.domain.OriginOfTranslation;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
          .hasDeliveryDate(coreData.hasDeliveryDate())
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
      addDeviatingCourts(builder, coreData);
      addCourtBranchLocation(builder, coreData);
      addDeviatingDecisionDates(builder, coreData);
      addDeviatingFileNumbers(builder, coreData, currentDto);
      addSources(currentDto, builder, updatedDomainObject);
      addOralHearingDates(builder, coreData);

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
      addDefinitions(builder, contentRelatedIndexing);
      addDismissalGrounds(builder, contentRelatedIndexing);
      addDismissalTypes(builder, contentRelatedIndexing);
      addCollectiveAgreements(builder, contentRelatedIndexing);
      builder.hasLegislativeMandate(contentRelatedIndexing.hasLegislativeMandate());
      builder.evsf(contentRelatedIndexing.evsf());
      addForeignLanguageVersions(builder, contentRelatedIndexing);
      if (contentRelatedIndexing.appealAdmission() == null) {
        builder.appealAdmitted(null);
        builder.appealAdmittedBy(null);
      } else {
        builder.appealAdmitted(contentRelatedIndexing.appealAdmission().admitted());
        builder.appealAdmittedBy(contentRelatedIndexing.appealAdmission().by());
      }
      builder.appeal(AppealTransformer.transformToDTO(currentDto, contentRelatedIndexing.appeal()));
      addOriginOfTranslations(builder, contentRelatedIndexing);
      addObjectValues(builder, contentRelatedIndexing);
      addNonApplicationNorms(builder, contentRelatedIndexing);
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
      addShortTexts(updatedDomainObject, builder);
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

    if (result.getAppeal() != null) {
      result.getAppeal().setDecision(result);
    }

    return DocumentableTransformer.postProcessRelationships(result, currentDto);
  }

  private static void addSources(
      DecisionDTO currentDto, DecisionDTOBuilder<?, ?> builder, Decision decision) {
    if (decision.coreData().sources() == null) {
      builder.source(new ArrayList<>());
      return;
    }

    List<SourceDTO> existingSources =
        currentDto.getSource() != null
            ? new ArrayList<>(currentDto.getSource())
            : new ArrayList<>();

    List<SourceDTO> newSources = new ArrayList<>();

    for (Source source : decision.coreData().sources()) {
      // lookup if an existing source has the same value (or sourceRawValue if it is a legacy source
      // that couldn't be mapped to a value) and if yes reuse it. This ensures we keep references.
      var optionalExistingSource =
          source.value() != null
              ? existingSources.stream()
                  .filter(existingSource -> source.value() == existingSource.getValue())
                  .findFirst()
              : existingSources.stream()
                  .filter(
                      existingSource ->
                          Objects.equals(
                              source.sourceRawValue(), existingSource.getSourceRawValue()))
                  .findFirst();

      if (optionalExistingSource.isPresent()) {
        var existingSource = optionalExistingSource.get();

        var reference = existingSource.getReference();
        if (reference != null
            && !documentableContainsReferenceWithId(decision, reference.getId())) {
          existingSource.setReference(null);
        }

        existingSource.setRank(newSources.size() + 1);
        newSources.add(optionalExistingSource.get());
      } else {
        newSources.add(
            SourceDTO.builder()
                .value(source.value())
                .sourceRawValue(source.sourceRawValue())
                .rank(newSources.size() + 1)
                .build());
      }
    }

    builder.source(newSources);
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
        .outline(longTexts.outline())
        .corrections(CorrectionTransformer.transformToDTOs(longTexts.corrections()));
  }

  private static void addShortTexts(
      Decision updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {
    ShortTexts shortTexts = updatedDomainObject.shortTexts();

    builder
        .guidingPrinciple(shortTexts.guidingPrinciple())
        .headnote(shortTexts.headnote())
        .otherHeadnote(shortTexts.otherHeadnote())
        .headline(shortTexts.headline());

    var decisionNames = shortTexts.decisionNames();
    if (decisionNames != null && !decisionNames.isEmpty()) {
      List<DecisionNameDTO> decisionNameDTOs = new ArrayList<>();
      for (int i = 0; i < decisionNames.size(); i++) {
        decisionNameDTOs.add(
            DecisionNameDTO.builder().value(decisionNames.get(i)).rank(i + 1).build());
      }
      builder.decisionNames(decisionNameDTOs);
    } else {
      builder.decisionNames(Collections.emptyList());
    }
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

  private static void addDefinitions(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.definitions() == null) {
      return;
    }

    List<Definition> definitions = contentRelatedIndexing.definitions();

    List<DefinitionDTO> definitionDTOs =
        definitions.stream()
            .map(
                def ->
                    DefinitionDTO.builder()
                        .id(def.newEntry() ? null : def.id())
                        .value(def.definedTerm())
                        .borderNumber(def.definingBorderNumber())
                        .rank(definitions.indexOf(def) + 1L)
                        .build())
            .toList();

    builder.definitions(definitionDTOs);
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
    List<CollectiveAgreement> collectiveAgreements =
        contentRelatedIndexing.collectiveAgreements().stream().distinct().toList();

    for (int i = 0; i < collectiveAgreements.size(); i++) {
      var collectiveAgreement = collectiveAgreements.get(i);
      var dto = CollectiveAgreementTransformer.transformToDTO(collectiveAgreement);
      dto.setRank(i + 1L);
      collectiveAgreementDTOS.add(dto);
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

  static void addOralHearingDates(DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.oralHearingDates() == null) {
      return;
    }

    List<OralHearingDateDTO> oralHearingDateDTOs = new ArrayList<>();
    List<LocalDate> oralHearingDates = coreData.oralHearingDates();

    for (int i = 0; i < oralHearingDates.size(); i++) {
      oralHearingDateDTOs.add(
          OralHearingDateDTO.builder().value(oralHearingDates.get(i)).rank(i + 1L).build());
    }

    builder.oralHearingDates(oralHearingDateDTOs);
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

  private static void addForeignLanguageVersions(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.foreignLanguageVersions() == null) {
      return;
    }

    List<ForeignLanguageVersionDTO> foreignLanguageVersionDTOs = new ArrayList<>();
    List<ForeignLanguageVersion> foreignLanguageVersions =
        contentRelatedIndexing.foreignLanguageVersions();

    for (int i = 0; i < foreignLanguageVersions.size(); i++) {
      foreignLanguageVersionDTOs.add(
          ForeignLanguageTransformer.transformToDTO(foreignLanguageVersions.get(i), i));
    }

    builder.foreignLanguageVersions(foreignLanguageVersionDTOs);
  }

  private static void addOriginOfTranslations(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.originOfTranslations() == null) {
      return;
    }

    List<OriginOfTranslationDTO> originOfTranslationDTOS = new ArrayList<>();
    List<OriginOfTranslation> originOfTranslations = contentRelatedIndexing.originOfTranslations();

    for (int i = 0; i < originOfTranslations.size(); i++) {
      originOfTranslationDTOS.add(
          OriginOfTranslationTransformer.transformToDTO(originOfTranslations.get(i), i));
    }

    builder.originOfTranslations(originOfTranslationDTOS);
  }

  private static void addObjectValues(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.objectValues() == null) {
      return;
    }

    List<ObjectValueDTO> objectValueDTOS = new ArrayList<>();
    List<ObjectValue> objectValues = contentRelatedIndexing.objectValues();

    for (int i = 0; i < objectValues.size(); i++) {
      objectValueDTOS.add(ObjectValueTransformer.transformToDTO(objectValues.get(i), i));
    }

    builder.objectValues(objectValueDTOS);
  }

  private static void addNonApplicationNorms(
      DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.nonApplicationNorms() == null) {
      return;
    }

    AtomicInteger i = new AtomicInteger(1);
    List<NonApplicationNormDTO> nonApplicationNormDTOS = new ArrayList<>();
    contentRelatedIndexing
        .nonApplicationNorms()
        .forEach(
            norm -> {
              List<NonApplicationNormDTO> flattened =
                  NonApplicationNormTransformer.transformToDTO(norm);
              flattened.forEach(
                  nonApplicationNormDTO -> nonApplicationNormDTO.setRank(i.getAndIncrement()));
              nonApplicationNormDTOS.addAll(flattened);
            });

    builder.nonApplicationNorms(nonApplicationNormDTOS);
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
        .portalPublicationStatus(decisionDTO.getPortalPublicationStatus())
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
        .currentDocumentationUnitProcessStep(
            DocumentationUnitProcessStepTransformer.toDomain(decisionDTO.getCurrentProcessStep()))
        .previousProcessStep(
            ProcessStepTransformer.getPreviousProcessStep(decisionDTO.getProcessSteps()))
        .processSteps(
            decisionDTO.getProcessSteps().stream()
                .map(DocumentationUnitProcessStepTransformer::toDomain)
                .toList())
        .build();
  }

  private static ShortTexts buildShortTexts(DecisionDTO decisionDTO) {
    return ShortTexts.builder()
        .headline(decisionDTO.getHeadline())
        .decisionNames(
            decisionDTO.getDecisionNames().stream().map(DecisionNameDTO::getValue).toList())
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
        .corrections(
            decisionDTO.getCorrections() != null
                ? decisionDTO.getCorrections().stream()
                    .map(CorrectionTransformer::transformToDomain)
                    .toList()
                : null)
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
        .sources(getSources(decisionDTO))
        .hasDeliveryDate(decisionDTO.hasDeliveryDate())
        .oralHearingDates(
            Optional.ofNullable(decisionDTO.getOralHearingDates())
                .map(d -> d.stream().map(OralHearingDateDTO::getValue).toList())
                .orElse(null));

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
      contentRelatedIndexingBuilder.collectiveAgreements(
          decisionDTO.getCollectiveAgreements().stream()
              .map(CollectiveAgreementTransformer::transformToDomain)
              .toList());
    }

    if (decisionDTO.getDefinitions() != null) {
      List<Definition> definitions =
          decisionDTO.getDefinitions().stream()
              .map(
                  def ->
                      Definition.builder()
                          .id(def.getId())
                          .definedTerm(def.getValue())
                          .definingBorderNumber(def.getBorderNumber())
                          .build())
              .toList();
      contentRelatedIndexingBuilder.definitions(definitions);
    } else {
      contentRelatedIndexingBuilder.definitions(List.of());
    }

    contentRelatedIndexingBuilder.hasLegislativeMandate(decisionDTO.isHasLegislativeMandate());
    contentRelatedIndexingBuilder.evsf(decisionDTO.getEvsf());

    if (decisionDTO.getForeignLanguageVersions() != null) {
      List<ForeignLanguageVersion> foreignLanguageVersions =
          decisionDTO.getForeignLanguageVersions().stream()
              .map(ForeignLanguageTransformer::transformToDomain)
              .toList();
      contentRelatedIndexingBuilder.foreignLanguageVersions(foreignLanguageVersions);
    }

    if (decisionDTO.getAppealAdmitted() != null) {
      contentRelatedIndexingBuilder.appealAdmission(
          AppealAdmission.builder()
              .admitted(decisionDTO.getAppealAdmitted())
              .by(decisionDTO.getAppealAdmittedBy())
              .build());
    }

    contentRelatedIndexingBuilder.appeal(
        AppealTransformer.transformToDomain(decisionDTO.getAppeal()));

    if (decisionDTO.getOriginOfTranslations() != null) {
      List<OriginOfTranslation> originOfTranslations =
          decisionDTO.getOriginOfTranslations().stream()
              .map(OriginOfTranslationTransformer::transformToDomain)
              .toList();
      contentRelatedIndexingBuilder.originOfTranslations(originOfTranslations);
    }

    if (decisionDTO.getObjectValues() != null) {
      List<ObjectValue> objectValues =
          decisionDTO.getObjectValues().stream()
              .map(ObjectValueTransformer::transformToDomain)
              .toList();
      contentRelatedIndexingBuilder.objectValues(objectValues);
    }

    if (decisionDTO.getNonApplicationNorms() != null) {
      contentRelatedIndexingBuilder.nonApplicationNorms(
          transformNonApplicationNormsToDomain(decisionDTO));
    }

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

  static List<Source> getSources(DocumentationUnitDTO decisionDTO) {
    return decisionDTO.getSource().stream()
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
        .toList();
  }

  private static List<NonApplicationNorm> transformNonApplicationNormsToDomain(
      DecisionDTO decisionDTO) {
    List<NonApplicationNorm> nonApplicationNorms = new ArrayList<>();

    decisionDTO
        .getNonApplicationNorms()
        .forEach(
            nonApplicationNormDTO -> {
              NonApplicationNorm nonApplicationNorm =
                  NonApplicationNormTransformer.transformToDomain(nonApplicationNormDTO);

              if (nonApplicationNormDTO.getNormAbbreviation() != null) {
                NonApplicationNorm existingNorm =
                    nonApplicationNorms.stream()
                        .filter(
                            existing ->
                                existing.normAbbreviation() != null
                                    && existing
                                        .normAbbreviation()
                                        .id()
                                        .equals(
                                            nonApplicationNormDTO.getNormAbbreviation().getId()))
                        .findFirst()
                        .orElse(null);

                if (existingNorm != null) {
                  existingNorm
                      .singleNorms()
                      .add(SingleNormTransformer.transformToDomain(nonApplicationNormDTO));
                } else {
                  nonApplicationNorms.add(nonApplicationNorm);
                }
              }
            });

    return nonApplicationNorms;
  }
}
