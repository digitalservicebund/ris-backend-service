package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO.DecisionDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.LocalDate;
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
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * This class is responsible for transforming a documentation unit object from its domain
 * representation into a database object and back
 */
@Slf4j
public class DecisionTransformer {
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
  public static DecisionDTO transformToDTO(
      DecisionDTO currentDto, DocumentationUnit updatedDomainObject) {

    log.debug("transform database decision '{}'", currentDto.getId());

    final var builder = currentDto.toBuilder();

    builder
        .id(updatedDomainObject.uuid())
        .documentNumber(updatedDomainObject.documentNumber())
        .version(updatedDomainObject.version());

    addPreviousDecisions(updatedDomainObject, builder);

    builder.note(
        StringUtils.returnTrueIfNullOrBlank(updatedDomainObject.note())
            ? null
            : updatedDomainObject.note());

    addEnsuingAndPendingDecisions(updatedDomainObject, builder, currentDto);

    if (updatedDomainObject.coreData() != null) {
      var coreData = updatedDomainObject.coreData();

      builder
          .judicialBody(StringUtils.normalizeSpace(coreData.appraisalBody()))
          .date(coreData.decisionDate())
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

      addFileNumbers(builder, coreData);
      addDeviationCourts(builder, coreData);
      addDeviatingDecisionDates(builder, coreData);
      addDeviatingFileNumbers(builder, coreData);
      addSource(currentDto, builder, coreData);

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
      builder
          .decisionNames(Collections.emptyList())
          .guidingPrinciple(null)
          .headnote(null)
          .otherHeadnote(null)
          .headline(null);
    }

    //      if (updatedDomainObject.shortTexts() != null) {
    //        if (updatedDomainObject.shortTexts().otherHeadnote() != null) {
    //          pendingProceedingDTOBuilder.resolutionNote(
    //              updatedDomainObject.shortTexts().otherHeadnote());
    //        }
    //      } else {
    //        pendingProceedingDTOBuilder.resolutionNote(null).headline(null);
    //      }
    //    }

    if (updatedDomainObject.managementData() != null) {
      var managementData = updatedDomainObject.managementData();

      builder.scheduledPublicationDateTime(managementData.scheduledPublicationDateTime());
      builder.lastPublicationDateTime(managementData.lastPublicationDateTime());
      builder.scheduledByEmail(managementData.scheduledByEmail());
    }

    addCaselawReferences(updatedDomainObject, builder, currentDto);
    addLiteratureReferences(updatedDomainObject, builder, currentDto);

    return builder.build();
  }

  private static void addSource(
      DecisionDTO currentDto, DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.source() != null) {
      List<SourceDTO> existingSources =
          currentDto.getSource() != null
              ? new ArrayList<>(currentDto.getSource())
              : new ArrayList<>();
      Integer rank = existingSources.size() + 1;

      // Create new SourceDTO
      SourceDTO newSource =
          SourceDTO.builder().value(coreData.source().value().toString()).rank(rank).build();

      // Add new source to the builder
      List<SourceDTO> updatedSources =
          new ArrayList<>(currentDto.getSource()); // Copy existing sources
      updatedSources.add(newSource);
      builder.source(updatedSources); // Update builder with new list
    }
  }

  private static void addCaselawReferences(
      DocumentationUnit updatedDomainObject,
      DecisionDTO.DecisionDTOBuilder<?, ?> builder,
      DecisionDTO currentDTO) {
    AtomicInteger rank = new AtomicInteger(0);
    builder.caselawReferences(
        updatedDomainObject.caselawReferences() == null
            ? Collections.emptyList()
            : updatedDomainObject.caselawReferences().stream()
                .map(ReferenceTransformer::transformToDTO)
                .map(
                    referenceDTO -> {
                      referenceDTO.setDocumentationUnit(builder.build()); // TODO needed?
                      referenceDTO.setDocumentationUnitRank(rank.incrementAndGet());

                      var existingReference =
                          currentDTO.getCaselawReferences().stream()
                              .filter(existing -> referenceDTO.getId().equals(existing.getId()))
                              .findFirst();
                      existingReference.ifPresent(
                          caselawReferenceDTO -> {
                            referenceDTO.setEditionRank(caselawReferenceDTO.getEditionRank());
                            referenceDTO.setEdition(caselawReferenceDTO.getEdition());
                          });

                      return (CaselawReferenceDTO) referenceDTO;
                    })
                .toList());
  }

  private static void addLiteratureReferences(
      DocumentationUnit updatedDomainObject,
      DecisionDTO.DecisionDTOBuilder<?, ?> builder,
      DecisionDTO currentDTO) {
    AtomicInteger rank = new AtomicInteger(0);
    builder.literatureReferences(
        updatedDomainObject.literatureReferences() == null
            ? Collections.emptyList()
            : updatedDomainObject.literatureReferences().stream()
                .map(ReferenceTransformer::transformToDTO)
                .map(
                    referenceDTO -> {
                      referenceDTO.setDocumentationUnit(builder.build()); // TODO needed?
                      referenceDTO.setDocumentationUnitRank(rank.incrementAndGet());

                      var existingReference =
                          currentDTO.getLiteratureReferences().stream()
                              .filter(existing -> referenceDTO.getId().equals(existing.getId()))
                              .findFirst();
                      existingReference.ifPresent(
                          literatureReferenceDTO -> {
                            referenceDTO.setEditionRank(literatureReferenceDTO.getEditionRank());
                            referenceDTO.setEdition(literatureReferenceDTO.getEdition());
                          });

                      return (LiteratureReferenceDTO) referenceDTO;
                    })
                .toList());
  }

  private static void addLongTexts(
      DocumentationUnit updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {
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
      DocumentationUnit updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {
    ShortTexts shortTexts = updatedDomainObject.shortTexts();

    builder
        .guidingPrinciple(shortTexts.guidingPrinciple())
        .headnote(shortTexts.headnote())
        .otherHeadnote(shortTexts.otherHeadnote())
        .headline(shortTexts.headline());

    if (shortTexts.decisionName() != null) {
      // Todo multiple decision names?
      builder.decisionNames(
          List.of(DecisionNameDTO.builder().value(shortTexts.decisionName()).build()));
    } else {
      builder.decisionNames(Collections.emptyList());
    }
  }

  /**
   * Adds norm references to the documentation unit builder based on the provided content-related
   * indexing information. Each {@link de.bund.digitalservice.ris.caselaw.domain.SingleNorm} are
   * grouped in a list of single norms, according to the associated norm abbreviation and packed
   * into a {@link NormReference}. When converting into a DTO object, each single norm in the
   * normReference is converted into its own {@link NormReferenceDTO}. In order for JPA to be able
   * to correctly link the legal force of each NormReferenceDTO, it must be explicitly set again.
   * (Because legal force is the owning side of the one to one connection, it is not implicitly
   * linked by jpa, when a norm with legal force is saved.)
   *
   * @param builder The builder for constructing the documentation unit DTO.
   * @param contentRelatedIndexing The content-related indexing information containing the norms.
   */
  private static void addNormReferences(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.norms() == null) {
      return;
    }

    AtomicInteger i = new AtomicInteger(1);
    List<NormReferenceDTO> flattenNormReferenceDTOs = new ArrayList<>();
    contentRelatedIndexing
        .norms()
        .forEach(
            norm -> {
              List<NormReferenceDTO> normReferenceDTOs =
                  NormReferenceTransformer.transformToDTO(norm);
              normReferenceDTOs.forEach(
                  normReferenceDTO -> normReferenceDTO.setRank(i.getAndIncrement()));
              flattenNormReferenceDTOs.addAll(normReferenceDTOs);
            });

    flattenNormReferenceDTOs.forEach(
        normReferenceDTO -> {
          if (normReferenceDTO.getLegalForce() != null)
            normReferenceDTO.getLegalForce().setNormReference(normReferenceDTO);
        });

    builder.normReferences(flattenNormReferenceDTOs);
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
                activeCitationDTO -> {
                  activeCitationDTO.setRank(i.getAndIncrement());
                  return activeCitationDTO;
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
      DocumentationUnit updatedDomainObject,
      DecisionDTOBuilder<?, ?> builder,
      DecisionDTO currentDTO) {
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

  private static void addPreviousDecisions(
      DocumentationUnit updatedDomainObject, DecisionDTO.DecisionDTOBuilder<?, ?> builder) {
    List<PreviousDecision> previousDecisions = updatedDomainObject.previousDecisions(); // NOSONAR
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
      DecisionDTO currentDto,
      DocumentationUnit updatedDomainObject,
      DecisionDTOBuilder<?, ?> builder) {

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
      DocumentationUnit updatedDomainObject, DecisionDTOBuilder<?, ?> builder) {

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

  private static void addDeviatingFileNumbers(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.deviatingFileNumbers() == null) {
      return;
    }

    List<DeviatingFileNumberDTO> deviatingFileNumberDTOs = new ArrayList<>(); // NOSONAR
    List<String> deviatingFileNumbers = coreData.deviatingFileNumbers(); // NOSONAR

    for (int i = 0; i < deviatingFileNumbers.size(); i++) {
      deviatingFileNumberDTOs.add(
          DeviatingFileNumberDTO.builder()
              .value(StringUtils.normalizeSpace(deviatingFileNumbers.get(i)))
              .rank(i + 1L)
              .build());
    }

    builder.deviatingFileNumbers(deviatingFileNumberDTOs);
  }

  private static void addDeviatingDecisionDates(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.deviatingDecisionDates() == null) {
      return;
    }

    List<DeviatingDateDTO> deviatingDateDTOs = new ArrayList<>(); // NOSONAR
    List<LocalDate> deviatingDecisionDates = coreData.deviatingDecisionDates(); // NOSONAR

    for (int i = 0; i < deviatingDecisionDates.size(); i++) {
      deviatingDateDTOs.add(
          DeviatingDateDTO.builder().value(deviatingDecisionDates.get(i)).rank(i + 1L).build());
    }

    builder.deviatingDates(deviatingDateDTOs);
  }

  private static void addDeviationCourts(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.deviatingCourts() == null) {
      return;
    }

    List<DeviatingCourtDTO> deviatingCourtDTOs = new ArrayList<>(); // NOSONAR
    List<String> deviatingCourts = coreData.deviatingCourts(); // NOSONAR

    for (int i = 0; i < deviatingCourts.size(); i++) {
      deviatingCourtDTOs.add(
          DeviatingCourtDTO.builder()
              .value(StringUtils.normalizeSpace(deviatingCourts.get(i)))
              .rank(i + 1L)
              .build());
    }

    builder.deviatingCourts(deviatingCourtDTOs);
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

  private static void addFileNumbers(
      DecisionDTO.DecisionDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.fileNumbers() == null) {
      return;
    }

    List<FileNumberDTO> fileNumberDTOs = new ArrayList<>(); // NOSONAR
    List<String> fileNumbers = coreData.fileNumbers(); // NOSONAR

    for (int i = 0; i < fileNumbers.size(); i++) {
      fileNumberDTOs.add(
          FileNumberDTO.builder()
              .value(StringUtils.normalizeSpace(fileNumbers.get(i)))
              .rank(i + 1L)
              .build());
    }

    builder.fileNumbers(fileNumberDTOs);
  }

  /**
   * Transforms a documentation unit object from its database representation into a domain object
   * that is suitable to be consumed by clients of the REST service.
   *
   * @param decisionDTO the database documentation unit
   * @return a transformed domain object, or an empty domain object if the input is null
   */
  public static DocumentationUnit transformToDomain(DecisionDTO decisionDTO) {
    if (decisionDTO == null) {
      throw new DocumentationUnitTransformerException("Document unit is null and won't transform");
    }

    log.debug("transfer database documentation unit '{}' to domain object", decisionDTO.getId());

    DocumentationUnit.DocumentationUnitBuilder builder =
        DocumentationUnit.builder().note(decisionDTO.getNote()).version(decisionDTO.getVersion());

    CoreDataBuilder coreDataBuilder =
        CoreData.builder()
            .court(CourtTransformer.transformToDomain(decisionDTO.getCourt()))
            .procedure(ProcedureTransformer.transformToDomain(decisionDTO.getProcedure(), false))
            .previousProcedures(
                ProcedureTransformer.transformPreviousProceduresToLabel(
                    decisionDTO.getProcedureHistory()))
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    decisionDTO.getDocumentationOffice()))
            .creatingDocOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    decisionDTO.getCreatingDocumentationOffice()))
            // TODO multiple regions
            .region(
                decisionDTO.getRegions() == null
                        || decisionDTO.getRegions().isEmpty()
                        || decisionDTO.getRegions().get(0) == null
                    ? null
                    : decisionDTO.getRegions().get(0).getCode())
            .source(
                decisionDTO.getSource().stream()
                    .max(Comparator.comparing(SourceDTO::getRank)) // Find the highest-ranked item
                    .map(
                        sourceDTO -> {
                          SourceValue sourceValue = null;
                          if (sourceDTO.getValue() != null) {
                            try {
                              // Attempt to convert the value to SourceValue
                              sourceValue = SourceValue.valueOf(sourceDTO.getValue());
                            } catch (IllegalArgumentException | NullPointerException e) {
                              System.err.println("Invalid SourceValue: " + sourceDTO.getValue());
                              // This should not be necessary after updating migration code &
                              // backfilling
                              sourceDTO.setSourceRawValue(sourceDTO.getValue());
                            }
                          }
                          return Source.builder()
                              .value(sourceValue) // Set the (valid) SourceValue, or null if invalid
                              .sourceRawValue(
                                  sourceDTO.getSourceRawValue()) // Ensure raw value is set
                              .build();
                        })
                    .orElse(null))
            .decisionDate(decisionDTO.getDate())
            .appraisalBody(decisionDTO.getJudicialBody());

    addFileNumbersToDomain(decisionDTO, coreDataBuilder);
    addDeviatingFileNumbersToDomain(decisionDTO, coreDataBuilder);
    addDeviatingCourtsToDomain(decisionDTO, coreDataBuilder);
    addDeviatingDecisionDatesToDomain(decisionDTO, coreDataBuilder);

    DocumentTypeDTO documentTypeDTO = decisionDTO.getDocumentType();
    if (documentTypeDTO != null) {
      coreDataBuilder.documentType(DocumentTypeTransformer.transformToDomain(documentTypeDTO));
    }

    ContentRelatedIndexing.ContentRelatedIndexingBuilder contentRelatedIndexingBuilder =
        ContentRelatedIndexing.builder();

    if (decisionDTO.getDocumentationUnitKeywordDTOs() != null) {
      List<String> keywords =
          decisionDTO.getDocumentationUnitKeywordDTOs().stream()
              .map(
                  documentationUnitKeywordDTO ->
                      documentationUnitKeywordDTO.getKeyword().getValue())
              .toList();
      contentRelatedIndexingBuilder.keywords(keywords);
    }

    if (decisionDTO.getNormReferences() != null) {
      List<NormReference> norms = addNormReferencesToDomain(decisionDTO);
      contentRelatedIndexingBuilder.norms(norms);
    }

    if (decisionDTO.getDocumentationUnitFieldsOfLaw() != null) {
      List<FieldOfLaw> fieldOfLaws =
          decisionDTO.getDocumentationUnitFieldsOfLaw().stream()
              .map(
                  documentationUnitFieldOfLawDTO ->
                      FieldOfLawTransformer.transformToDomain(
                          documentationUnitFieldOfLawDTO.getFieldOfLaw(), false, false))
              .toList();

      contentRelatedIndexingBuilder.fieldsOfLaw(fieldOfLaws);
    }

    ShortTexts.ShortTextsBuilder shortTextsBuilder =
        ShortTexts.builder().headline(decisionDTO.getHeadline());
    LongTexts.LongTextsBuilder longTextsBuilder = LongTexts.builder();

    ManagementData.ManagementDataBuilder managementDataBuilder =
        ManagementData.builder()
            .lastPublicationDateTime(decisionDTO.getLastPublicationDateTime())
            .scheduledPublicationDateTime(decisionDTO.getScheduledPublicationDateTime())
            .scheduledByEmail(decisionDTO.getScheduledByEmail());

    addOriginalFileDocuments(decisionDTO, builder);
    addPreviousDecisionsToDomain(decisionDTO, builder);

    // decision specific fields

    LegalEffect legalEffect =
        LegalEffectTransformer.transformToDomain(decisionDTO.getLegalEffect());

    coreDataBuilder
        .ecli(decisionDTO.getEcli())
        .legalEffect(legalEffect == null ? null : legalEffect.getLabel());

    addInputTypesToDomain(decisionDTO, coreDataBuilder);
    addLeadingDecisionNormReferencesToDomain(decisionDTO, coreDataBuilder);
    addYearsOfDisputeToDomain(decisionDTO, coreDataBuilder);
    addDeviatingEclisToDomain(decisionDTO, coreDataBuilder);

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

    shortTextsBuilder
        // TODO multiple decisionNames
        .decisionName(
            (decisionDTO.getDecisionNames() == null || decisionDTO.getDecisionNames().isEmpty())
                ? null
                : decisionDTO.getDecisionNames().stream().findFirst().get().getValue())
        .guidingPrinciple(decisionDTO.getGuidingPrinciple())
        .headnote(decisionDTO.getHeadnote())
        .otherHeadnote(decisionDTO.getOtherHeadnote())
        .build();

    longTextsBuilder
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

    List<String> borderNumbers =
        extractBorderNumbers(
            decisionDTO.getTenor(),
            decisionDTO.getGrounds(),
            decisionDTO.getCaseFacts(),
            decisionDTO.getDecisionGrounds(),
            decisionDTO.getOtherLongText(),
            decisionDTO.getDissentingOpinion());

    List<DuplicateRelation> duplicateRelations = transformDuplicateRelations(decisionDTO);

    managementDataBuilder.borderNumbers(borderNumbers).duplicateRelations(duplicateRelations);

    addEnsuingDecisionsToDomain(decisionDTO, builder);

    CoreData coreData = coreDataBuilder.build();
    ContentRelatedIndexing contentRelatedIndexing = contentRelatedIndexingBuilder.build();
    ShortTexts shortTexts = shortTextsBuilder.build();
    LongTexts longTexts = longTextsBuilder.build();
    ManagementData managementData = managementDataBuilder.build();

    builder
        .uuid(decisionDTO.getId())
        .documentNumber(decisionDTO.getDocumentNumber())
        .coreData(coreData)
        .shortTexts(shortTexts)
        .longTexts(longTexts)
        .contentRelatedIndexing(contentRelatedIndexing)
        .managementData(managementData)
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
                    .toList());

    addStatusToDomain(decisionDTO, builder);

    return builder.build();
  }

  @NotNull
  private static List<DuplicateRelation> transformDuplicateRelations(
      DecisionDTO documentationUnitDTO) {
    return Stream.concat(
            documentationUnitDTO.getDuplicateRelations1().stream()
                .filter(
                    relation ->
                        isPublishedDuplicateOrSameDocOffice(
                            documentationUnitDTO, relation.getDocumentationUnit2())),
            documentationUnitDTO.getDuplicateRelations2().stream()
                .filter(
                    relation ->
                        isPublishedDuplicateOrSameDocOffice(
                            documentationUnitDTO, relation.getDocumentationUnit1())))
        .map(
            relation ->
                DuplicateRelationTransformer.transformToDomain(relation, documentationUnitDTO))
        .sorted(
            Comparator.comparing(
                    (DuplicateRelation relation) ->
                        Optional.ofNullable(relation.decisionDate()).orElse(LocalDate.MIN),
                    Comparator.reverseOrder())
                .thenComparing(DuplicateRelation::documentNumber))
        .toList();
  }

  private static Boolean isPublishedDuplicateOrSameDocOffice(
      DecisionDTO original, DecisionDTO duplicate) {
    var duplicateStatus =
        Optional.ofNullable(duplicate.getStatus())
            .map(StatusDTO::getPublicationStatus)
            .orElse(null);
    return original.getDocumentationOffice().equals(duplicate.getDocumentationOffice())
        || PublicationStatus.PUBLISHED.equals(duplicateStatus);
  }

  /**
   * Adds norm references to the domain object based on the provided documentation unit DTO. A list
   * of NormReferenceDTOs with the same normAbbreviation are grouped into one NormReference, with a
   * list of {@link SingleNorm}.
   *
   * @param documentationUnitDTO The documentation unit DTO containing norm references to be added.
   * @return A list of NormReference objects representing the added norm references.
   */
  private static List<NormReference> addNormReferencesToDomain(DecisionDTO documentationUnitDTO) {
    List<NormReference> normReferences = new ArrayList<>();

    documentationUnitDTO
        .getNormReferences()
        .forEach(
            normReferenceDTO -> {
              NormReference normReference =
                  NormReferenceTransformer.transformToDomain(normReferenceDTO);

              if (normReferenceDTO.getNormAbbreviation() != null) {
                NormReference existingReference =
                    normReferences.stream()
                        .filter(
                            existingNormReference ->
                                existingNormReference.normAbbreviation() != null
                                    && existingNormReference
                                        .normAbbreviation()
                                        .id()
                                        .equals(normReferenceDTO.getNormAbbreviation().getId()))
                        .findFirst()
                        .orElse(null);

                if (existingReference != null) {
                  existingReference
                      .singleNorms()
                      .add(SingleNormTransformer.transformToDomain(normReferenceDTO));
                } else {
                  normReferences.add(normReference);
                }

              } else if (normReferenceDTO.getNormAbbreviationRawValue() != null) {
                NormReference existingReference =
                    normReferences.stream()
                        .filter(
                            existingNormReference ->
                                existingNormReference.normAbbreviationRawValue() != null
                                    && existingNormReference
                                        .normAbbreviationRawValue()
                                        .equals(normReferenceDTO.getNormAbbreviationRawValue()))
                        .findFirst()
                        .orElse(null);

                if (existingReference != null) {
                  existingReference
                      .singleNorms()
                      .add(SingleNormTransformer.transformToDomain(normReferenceDTO));
                } else {
                  normReferences.add(normReference);
                }
              }
            });

    // Handle cases where both abbreviation and raw value are null
    normReferences.addAll(
        documentationUnitDTO.getNormReferences().stream()
            .filter(
                normReferenceDTO ->
                    normReferenceDTO.getNormAbbreviation() == null
                        && normReferenceDTO.getNormAbbreviationRawValue() == null)
            .map(NormReferenceTransformer::transformToDomain)
            .toList());

    return normReferences;
  }

  private static void addOriginalFileDocuments(
      DecisionDTO documentationUnitDTO, DocumentationUnit.DocumentationUnitBuilder builder) {
    builder.attachments(
        documentationUnitDTO.getAttachments().stream()
            .map(AttachmentTransformer::transformToDomain)
            .toList());
  }

  private static void addStatusToDomain(
      DecisionDTO documentationUnitDTO, DocumentationUnit.DocumentationUnitBuilder builder) {
    builder.status(StatusTransformer.transformToDomain(documentationUnitDTO.getStatus()));
  }

  private static void addPreviousDecisionsToDomain(
      DecisionDTO documentationUnitDTO, DocumentationUnit.DocumentationUnitBuilder builder) {
    if (documentationUnitDTO.getPreviousDecisions() == null) {
      return;
    }

    builder.previousDecisions(
        documentationUnitDTO.getPreviousDecisions().stream()
            .map(PreviousDecisionTransformer::transformToDomain)
            .toList());
  }

  private static void addDeviatingDecisionDatesToDomain(
      DecisionDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingDates() == null) {
      return;
    }

    List<LocalDate> deviatingDecisionDates =
        documentationUnitDTO.getDeviatingDates().stream().map(DeviatingDateDTO::getValue).toList();
    coreDataBuilder.deviatingDecisionDates(deviatingDecisionDates);
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

  private static void addDeviatingCourtsToDomain(
      DecisionDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
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
      DecisionDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getDeviatingFileNumbers() == null) {
      return;
    }

    List<String> deviatingFileNumbers =
        documentationUnitDTO.getDeviatingFileNumbers().stream()
            .map(DeviatingFileNumberDTO::getValue)
            .toList();
    coreDataBuilder.deviatingFileNumbers(deviatingFileNumbers);
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

  private static void addFileNumbersToDomain(
      DecisionDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getFileNumbers() == null) {
      return;
    }

    List<String> fileNumbers =
        documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    coreDataBuilder.fileNumbers(fileNumbers);
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

  private static void addEnsuingDecisionsToDomain(
      DecisionDTO decisionDTO, DocumentationUnit.DocumentationUnitBuilder builder) {

    if (decisionDTO.getPendingDecisions() == null && decisionDTO.getEnsuingDecisions() == null) {
      return;
    }

    List<EnsuingDecision> withoutRank = new ArrayList<>();

    EnsuingDecision[] ensuingDecisions =
        new EnsuingDecision
            [getEnsuingDecisionListSize(
                decisionDTO.getEnsuingDecisions(), decisionDTO.getPendingDecisions())];

    addEnsuingDecisionToDomain(decisionDTO.getEnsuingDecisions(), withoutRank, ensuingDecisions);
    addPendingDecisionsToDomain(decisionDTO.getPendingDecisions(), withoutRank, ensuingDecisions);

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

  /**
   * Extracts all border numbers from the passed strings and returns them as a list based on the
   * following rules: For all <border-number> elements that contain a single <number> element with
   * non-blank content, that content will be added to the list of border numbers.
   *
   * @param input the strings to be searched for border numbers
   * @return a list of found border numbers or an empty list, if the input is null
   */
  private static List<String> extractBorderNumbers(String... input) {
    if (Objects.isNull(input)) {
      return new ArrayList<>();
    }
    List<String> borderNumbers = new ArrayList<>();
    Arrays.stream(input)
        .forEach(
            longText -> {
              if (Objects.isNull(longText)) {
                return;
              }
              Document doc = Jsoup.parse(longText);
              var borderNumberElements = doc.getElementsByTag("border-number");
              borderNumberElements.forEach(
                  element -> {
                    var numberElement = element.getElementsByTag("number");
                    if (numberElement.size() == 1) {
                      var number = numberElement.get(0).text();
                      if (org.apache.commons.lang3.StringUtils.isNotBlank(number)) {
                        borderNumbers.add(numberElement.text());
                      }
                    }
                  });
            });
    return borderNumbers;
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
}
