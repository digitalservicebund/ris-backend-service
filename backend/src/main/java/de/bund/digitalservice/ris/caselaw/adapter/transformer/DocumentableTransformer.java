package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.NormReferenceType;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AbstractNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for transforming a documentation unit object from its domain
 * representation into a database object and back
 */
@Slf4j
public class DocumentableTransformer {
  DocumentableTransformer() {}

  static boolean documentableContainsReferenceWithId(DocumentationUnit docUnit, UUID referenceId) {

    if (referenceId == null) {
      return false;
    }

    return Stream.concat(
            Optional.ofNullable(docUnit.caselawReferences()).orElse(List.of()).stream(),
            Optional.ofNullable(docUnit.literatureReferences()).orElse(List.of()).stream())
        .anyMatch(ref -> referenceId.equals(ref.id()));
  }

  static void addCaselawReferences(
      DocumentationUnit updatedDomainObject,
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      DocumentationUnitDTO currentDTO) {
    AtomicInteger rank = new AtomicInteger(0);
    builder.caselawReferences(
        updatedDomainObject.caselawReferences() == null
            ? Collections.emptyList()
            : updatedDomainObject.caselawReferences().stream()
                .map(ReferenceTransformer::transformToDTO)
                .map(
                    referenceDTO -> {
                      referenceDTO.setDocumentationUnitRank(rank.incrementAndGet());

                      Optional<CaselawReferenceDTO> existingReference = Optional.empty();

                      UUID referenceId = referenceDTO.getId();
                      if (referenceId != null) {
                        existingReference =
                            currentDTO.getCaselawReferences().stream()
                                .filter(existing -> referenceId.equals(existing.getId()))
                                .findFirst();
                      }
                      existingReference.ifPresent(
                          caselawReferenceDTO -> {
                            referenceDTO.setEditionRank(caselawReferenceDTO.getEditionRank());
                            referenceDTO.setEdition(caselawReferenceDTO.getEdition());
                          });

                      return (CaselawReferenceDTO) referenceDTO;
                    })
                .toList());
  }

  static void addLiteratureReferences(
      DocumentationUnit updatedDomainObject,
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      DocumentationUnitDTO currentDTO) {
    AtomicInteger rank = new AtomicInteger(0);
    builder.literatureReferences(
        updatedDomainObject.literatureReferences() == null
            ? Collections.emptyList()
            : updatedDomainObject.literatureReferences().stream()
                .map(ReferenceTransformer::transformToDTO)
                .map(
                    referenceDTO -> {
                      referenceDTO.setDocumentationUnitRank(rank.incrementAndGet());

                      Optional<LiteratureReferenceDTO> existingReference = Optional.empty();

                      UUID referenceId = referenceDTO.getId();
                      if (referenceId != null) {
                        existingReference =
                            currentDTO.getLiteratureReferences().stream()
                                .filter(existing -> referenceId.equals(existing.getId()))
                                .findFirst();
                      }
                      existingReference.ifPresent(
                          literatureReferenceDTO -> {
                            referenceDTO.setEditionRank(literatureReferenceDTO.getEditionRank());
                            referenceDTO.setEdition(literatureReferenceDTO.getEdition());
                          });

                      return (LiteratureReferenceDTO) referenceDTO;
                    })
                .toList());
  }

  static void addManagementData(
      DocumentationUnit updatedDomainObject,
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder) {

    if (updatedDomainObject.managementData() != null) {
      var managementData = updatedDomainObject.managementData();

      builder.scheduledPublicationDateTime(managementData.scheduledPublicationDateTime());
      builder.lastHandoverDateTime(managementData.lastHandoverDateTime());
      builder.scheduledByEmail(managementData.scheduledByEmail());
    }
  }

  /**
   * Handles post-build operations, typically setting bidirectional relationships or other
   * properties that require the fully built parent DTO.
   *
   * @param result The newly built DocumentationUnitDTO (or subclass).
   * @param currentDto The existing DTO (from database) which might hold related entities with IDs.
   * @param <T> The specific DocumentationUnitDTO type.
   * @return The updated result DTO.
   */
  protected static <T extends DocumentationUnitDTO> T postProcessRelationships(
      T result, DocumentationUnitDTO currentDto) {

    // --- CaselawReferences linking ---
    if (result.getCaselawReferences() != null) {
      result.getCaselawReferences().forEach(reference -> reference.setDocumentationUnit(result));
    }

    // --- LiteratureReferences linking ---
    if (result.getLiteratureReferences() != null) {
      result.getLiteratureReferences().forEach(reference -> reference.setDocumentationUnit(result));
    }

    // --- ManagementData linking ---
    if (currentDto.getManagementData() != null) {
      currentDto.getManagementData().setDocumentationUnit(result);
      result.setManagementData(currentDto.getManagementData());
    }

    return result;
  }

  static void addNormReferences(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      ContentRelatedIndexing contentRelatedIndexing) {
    if (contentRelatedIndexing.norms() == null) {
      return;
    }

    builder.normReferences(
        (List<NormReferenceDTO>)
            (List<? extends AbstractNormReferenceDTO>)
                NormReferenceTransformer.transformToDTO(
                    contentRelatedIndexing.norms(), NormReferenceType.NORM));
  }

  static void addPreviousDecisions(
      DocumentationUnit updatedDomainObject,
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder) {
    List<PreviousDecision> previousDecisions = updatedDomainObject.previousDecisions(); // NOSONAR
    if (previousDecisions != null) {
      AtomicInteger i = new AtomicInteger(1);
      builder.previousDecisions(
          previousDecisions.stream()
              .map(PreviousDecisionTransformer::transformToDTO)
              .filter(Objects::nonNull)
              .map(
                  activeCitationDTO -> {
                    activeCitationDTO.setRank(i.getAndIncrement());
                    return activeCitationDTO;
                  })
              .toList());
    }
  }

  static void addDeviatingDocumentNumbers(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      CoreData coreData,
      DocumentationUnitDTO currentDto) {
    if (coreData.deviatingDocumentNumbers() == null) {
      return;
    }

    List<DeviatingDocumentNumberDTO> deviatingDocumentNumberDTOs = new ArrayList<>(); // NOSONAR
    List<String> deviatingDocumentNumbers = coreData.deviatingDocumentNumbers(); // NOSONAR

    for (int i = 0; i < deviatingDocumentNumbers.size(); i++) {
      deviatingDocumentNumberDTOs.add(
          DeviatingDocumentNumberDTO.builder()
              .value(StringUtils.normalizeSpace(deviatingDocumentNumbers.get(i)))
              .documentationUnit(currentDto)
              .rank(i + 1L)
              .build());
    }

    builder.deviatingDocumentNumbers(deviatingDocumentNumberDTOs);
  }

  static void addDeviatingFileNumbers(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      CoreData coreData,
      DocumentationUnitDTO currentDto) {
    if (coreData.deviatingFileNumbers() == null) {
      return;
    }

    List<DeviatingFileNumberDTO> deviatingFileNumberDTOs = new ArrayList<>(); // NOSONAR
    List<String> deviatingFileNumbers = coreData.deviatingFileNumbers(); // NOSONAR

    for (int i = 0; i < deviatingFileNumbers.size(); i++) {
      deviatingFileNumberDTOs.add(
          DeviatingFileNumberDTO.builder()
              .value(StringUtils.normalizeSpace(deviatingFileNumbers.get(i)))
              .documentationUnit(currentDto)
              .rank(i + 1L)
              .build());
    }

    builder.deviatingFileNumbers(deviatingFileNumberDTOs);
  }

  static void addDeviatingDecisionDates(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder, CoreData coreData) {
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

  static void addDeviatingCourts(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder, CoreData coreData) {
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

  static void addCourtBranchLocation(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder, CoreData coreData) {
    if (coreData.courtBranchLocation() == null) {
      builder.courtBranchLocation(null);
    } else {
      builder.courtBranchLocation(
          CourtBranchLocationTransformer.transformToDTO(coreData.courtBranchLocation()));
    }
  }

  static void addFileNumbers(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      CoreData coreData,
      DocumentationUnitDTO currentDto) {
    if (coreData.fileNumbers() == null) {
      return;
    }

    List<FileNumberDTO> fileNumberDTOs = new ArrayList<>(); // NOSONAR
    List<String> fileNumbers = coreData.fileNumbers(); // NOSONAR

    for (int i = 0; i < fileNumbers.size(); i++) {
      fileNumberDTOs.add(
          FileNumberDTO.builder()
              .value(StringUtils.normalizeSpace(fileNumbers.get(i)))
              .documentationUnit(currentDto)
              .rank(i + 1L)
              .build());
    }

    builder.fileNumbers(fileNumberDTOs);
  }

  static CoreData buildMutualCoreData(DocumentationUnitDTO documentationUnitDTO) {
    CoreDataBuilder coreDataBuilder =
        CoreData.builder()
            .court(CourtTransformer.transformToDomain(documentationUnitDTO.getCourt()))
            .courtBranchLocation(
                CourtBranchLocationTransformer.transformToDomain(
                    documentationUnitDTO.getCourtBranchLocation()))
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    documentationUnitDTO.getDocumentationOffice()))
            .decisionDate(documentationUnitDTO.getDate())
            .appraisalBody(documentationUnitDTO.getJudicialBody());

    addDeviatingDocumentNumberToDomain(documentationUnitDTO, coreDataBuilder);
    addFileNumbersToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingFileNumbersToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingCourtsToDomain(documentationUnitDTO, coreDataBuilder);
    addDeviatingDecisionDatesToDomain(documentationUnitDTO, coreDataBuilder);

    DocumentTypeDTO documentTypeDTO = documentationUnitDTO.getDocumentType();
    if (documentTypeDTO != null) {
      coreDataBuilder.documentType(DocumentTypeTransformer.transformToDomain(documentTypeDTO));
    }
    return coreDataBuilder.build();
  }

  static ContentRelatedIndexing buildContentRelatedIndexing(DocumentationUnitDTO decisionDTO) {
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
      List<NormReference> norms =
          NormReferenceTransformer.transformToDomain(decisionDTO.getNormReferences());
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

    return contentRelatedIndexingBuilder.build();
  }

  static Status getStatus(DocumentationUnitDTO decisionDTO) {
    return StatusTransformer.transformToDomain(decisionDTO.getStatus());
  }

  static List<PreviousDecision> getPreviousDecisions(DocumentationUnitDTO decisionDTO) {
    if (decisionDTO.getPreviousDecisions() == null) {
      return List.of();
    }

    return decisionDTO.getPreviousDecisions().stream()
        .map(PreviousDecisionTransformer::transformToDomain)
        .toList();
  }

  static void addDeviatingDecisionDatesToDomain(
      DocumentationUnitDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getDeviatingDates() == null) {
      return;
    }

    List<LocalDate> deviatingDecisionDates =
        decisionDTO.getDeviatingDates().stream().map(DeviatingDateDTO::getValue).toList();
    coreDataBuilder.deviatingDecisionDates(deviatingDecisionDates);
  }

  static void addDeviatingDocumentNumberToDomain(
      DocumentationUnitDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getDeviatingDocumentNumbers() == null) {
      return;
    }

    List<String> deviatingDocumentNumbers =
        decisionDTO.getDeviatingDocumentNumbers().stream()
            .map(DeviatingDocumentNumberDTO::getValue)
            .toList();
    coreDataBuilder.deviatingDocumentNumbers(deviatingDocumentNumbers);
  }

  static void addDeviatingCourtsToDomain(
      DocumentationUnitDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getDeviatingCourts() == null) {
      return;
    }

    List<String> deviatingCourts =
        decisionDTO.getDeviatingCourts().stream().map(DeviatingCourtDTO::getValue).toList();
    coreDataBuilder.deviatingCourts(deviatingCourts);
  }

  static void addDeviatingFileNumbersToDomain(
      DocumentationUnitDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getDeviatingFileNumbers() == null) {
      return;
    }

    List<String> deviatingFileNumbers =
        decisionDTO.getDeviatingFileNumbers().stream()
            .map(DeviatingFileNumberDTO::getValue)
            .toList();
    coreDataBuilder.deviatingFileNumbers(deviatingFileNumbers);
  }

  static void addFileNumbersToDomain(
      DocumentationUnitDTO decisionDTO, CoreDataBuilder coreDataBuilder) {
    if (decisionDTO.getFileNumbers() == null) {
      return;
    }

    List<String> fileNumbers =
        decisionDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    coreDataBuilder.fileNumbers(fileNumbers);
  }

  static void handleEnsuingDecisionsWithoutRank(
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

  static void addPendingDecisionsToDomain(
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

  static void addEnsuingDecisionToDomain(
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

  static int getEnsuingDecisionListSize(
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
}
