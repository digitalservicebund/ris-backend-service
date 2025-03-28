package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class DocumentableTransformer {
  DocumentableTransformer() {}

  static boolean decisionContainsReferenceWithId(Documentable docUnit, UUID referenceID) {
    boolean caselawReferencesContainId =
        docUnit.caselawReferences() != null
            && !docUnit.caselawReferences().isEmpty()
            && referenceID.equals(docUnit.caselawReferences().getFirst().id());

    boolean literatureReferencesContainId =
        docUnit.literatureReferences() != null
            && !docUnit.literatureReferences().isEmpty()
            && referenceID.equals(docUnit.literatureReferences().getFirst().id());

    return caselawReferencesContainId || literatureReferencesContainId;
  }

  static void addCaselawReferences(
      Documentable updatedDomainObject,
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

  static void addLiteratureReferences(
      Documentable updatedDomainObject,
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

  /**
   * Adds norm references to the documentation unit builder based on the provided content-related
   * indexing information. Each {@link SingleNorm} are grouped in a list of single norms, according
   * to the associated norm abbreviation and packed into a {@link NormReference}. When converting
   * into a DTO object, each single norm in the normReference is converted into its own {@link
   * NormReferenceDTO}. In order for JPA to be able to correctly link the legal force of each
   * NormReferenceDTO, it must be explicitly set again. (Because legal force is the owning side of
   * the one to one connection, it is not implicitly linked by jpa, when a norm with legal force is
   * saved.)
   *
   * @param builder The builder for constructing the documentation unit DTO.
   * @param contentRelatedIndexing The content-related indexing information containing the norms.
   */
  static void addNormReferences(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder,
      ContentRelatedIndexing contentRelatedIndexing) {
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

  static void addPreviousDecisions(
      Documentable updatedDomainObject,
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

  static void addDeviatingFileNumbers(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder, CoreData coreData) {
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

  static void addDeviationCourts(
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

  static void addFileNumbers(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder<?, ?> builder, CoreData coreData) {
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

  static CoreData buildCoreData(DocumentationUnitDTO decisionDTO) {
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
            .source(getSource(decisionDTO))
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
    return coreDataBuilder.build();
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
              return Source.builder()
                  .value(sourceValue)
                  .sourceRawValue(sourceDTO.getSourceRawValue())
                  .build();
            })
        .orElse(null);
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

    return contentRelatedIndexingBuilder.build();
  }

  @NotNull
  static List<DuplicateRelation> transformDuplicateRelations(DocumentationUnitDTO decisionDTO) {
    return Stream.concat(
            decisionDTO.getDuplicateRelations1().stream()
                .filter(
                    relation ->
                        isPublishedDuplicateOrSameDocOffice(
                            decisionDTO, relation.getDocumentationUnit2())),
            decisionDTO.getDuplicateRelations2().stream()
                .filter(
                    relation ->
                        isPublishedDuplicateOrSameDocOffice(
                            decisionDTO, relation.getDocumentationUnit1())))
        .map(relation -> DuplicateRelationTransformer.transformToDomain(relation, decisionDTO))
        .sorted(
            Comparator.comparing(
                    (DuplicateRelation relation) ->
                        Optional.ofNullable(relation.decisionDate()).orElse(LocalDate.MIN),
                    Comparator.reverseOrder())
                .thenComparing(DuplicateRelation::documentNumber))
        .toList();
  }

  static Boolean isPublishedDuplicateOrSameDocOffice(
      DocumentationUnitDTO original, DocumentationUnitDTO duplicate) {
    var duplicateStatus =
        Optional.ofNullable(duplicate.getStatus())
            .map(StatusDTO::getPublicationStatus)
            .orElse(null);
    return original.getDocumentationOffice().equals(duplicate.getDocumentationOffice())
        || PublicationStatus.PUBLISHED.equals(duplicateStatus)
        || PublicationStatus.PUBLISHING.equals(duplicateStatus);
  }

  /**
   * Adds norm references to the domain object based on the provided documentation unit DTO. A list
   * of NormReferenceDTOs with the same normAbbreviation are grouped into one NormReference, with a
   * list of {@link SingleNorm}.
   *
   * @param decisionDTO The documentation unit DTO containing norm references to be added.
   * @return A list of NormReference objects representing the added norm references.
   */
  static List<NormReference> addNormReferencesToDomain(DocumentationUnitDTO decisionDTO) {
    List<NormReference> normReferences = new ArrayList<>();

    decisionDTO
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
        decisionDTO.getNormReferences().stream()
            .filter(
                normReferenceDTO ->
                    normReferenceDTO.getNormAbbreviation() == null
                        && normReferenceDTO.getNormAbbreviationRawValue() == null)
            .map(NormReferenceTransformer::transformToDomain)
            .toList());

    return normReferences;
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

  /**
   * Extracts all border numbers from the passed strings and returns them as a list based on the
   * following rules: For all <border-number> elements that contain a single <number> element with
   * non-blank content, that content will be added to the list of border numbers.
   *
   * @param input the strings to be searched for border numbers
   * @return a list of found border numbers or an empty list, if the input is null
   */
  static List<String> extractBorderNumbers(String... input) {
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
                      var number = numberElement.getFirst().text();
                      if (org.apache.commons.lang3.StringUtils.isNotBlank(number)) {
                        borderNumbers.add(numberElement.text());
                      }
                    }
                  });
            });
    return borderNumbers;
  }
}
