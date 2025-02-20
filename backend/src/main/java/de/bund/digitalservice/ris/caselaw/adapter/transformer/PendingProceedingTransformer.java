package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for transforming a documentation unit object from its domain
 * representation into a database object and back
 */
@Slf4j
public class PendingProceedingTransformer {
  private PendingProceedingTransformer() {}

  /**
   * /** Transforms a documentation unit object from its database representation into a domain
   * object that is suitable to be consumed by clients of the REST service.
   *
   * @param pendingProceedingDTO the database documentation unit
   * @return a transformed domain object, or an empty domain object if the input is null
   */
  public static PendingProceeding transformToDomain(PendingProceedingDTO pendingProceedingDTO) {
    if (pendingProceedingDTO == null) {
      throw new DocumentationUnitTransformerException(
          "Pending proceeding is null and won't transform");
    }

    log.debug(
        "transfer database pending proceeding '{}' to domain object", pendingProceedingDTO.getId());

    PendingProceeding.PendingProceedingBuilder builder = PendingProceeding.builder();

    CoreData coreData = buildCoreData(pendingProceedingDTO);
    ContentRelatedIndexing contentRelatedIndexing =
        buildContentRelatedIndexing(pendingProceedingDTO);

    ShortTexts.ShortTextsBuilder shortTextsBuilder =
        ShortTexts.builder().headline(pendingProceedingDTO.getHeadline());

    addPreviousDecisionsToDomain(pendingProceedingDTO, builder);

    shortTextsBuilder
        .guidingPrinciple(pendingProceedingDTO.getGuidingPrinciple())
        .headnote(pendingProceedingDTO.getHeadnote())
        .build();

    ShortTexts shortTexts = shortTextsBuilder.build();

    builder
        .uuid(pendingProceedingDTO.getId())
        .documentNumber(pendingProceedingDTO.getDocumentNumber())
        .coreData(coreData)
        .shortTexts(shortTexts)
        .contentRelatedIndexing(contentRelatedIndexing)
        .caselawReferences(
            pendingProceedingDTO.getCaselawReferences() == null
                ? new ArrayList<>()
                : pendingProceedingDTO.getCaselawReferences().stream()
                    .map(ReferenceTransformer::transformToDomain)
                    .toList())
        .literatureReferences(
            pendingProceedingDTO.getLiteratureReferences() == null
                ? new ArrayList<>()
                : pendingProceedingDTO.getLiteratureReferences().stream()
                    .map(ReferenceTransformer::transformToDomain)
                    .toList());

    addStatusToDomain(pendingProceedingDTO, builder);

    return builder.build();
  }

  private static CoreData buildCoreData(PendingProceedingDTO pendingProceedingDTO) {
    CoreDataBuilder coreDataBuilder =
        CoreData.builder()
            .court(CourtTransformer.transformToDomain(pendingProceedingDTO.getCourt()))
            .procedure(
                ProcedureTransformer.transformToDomain(pendingProceedingDTO.getProcedure(), false))
            .previousProcedures(
                ProcedureTransformer.transformPreviousProceduresToLabel(
                    pendingProceedingDTO.getProcedureHistory()))
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    pendingProceedingDTO.getDocumentationOffice()))
            .creatingDocOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    pendingProceedingDTO.getCreatingDocumentationOffice()))
            .decisionDate(pendingProceedingDTO.getDate())
            .appraisalBody(pendingProceedingDTO.getJudicialBody());

    addFileNumbersToDomain(pendingProceedingDTO, coreDataBuilder);
    addDeviatingFileNumbersToDomain(pendingProceedingDTO, coreDataBuilder);
    addDeviatingCourtsToDomain(pendingProceedingDTO, coreDataBuilder);

    DocumentTypeDTO documentTypeDTO = pendingProceedingDTO.getDocumentType();
    if (documentTypeDTO != null) {
      coreDataBuilder.documentType(DocumentTypeTransformer.transformToDomain(documentTypeDTO));
    }

    return coreDataBuilder.build();
  }

  private static ContentRelatedIndexing buildContentRelatedIndexing(
      PendingProceedingDTO pendingProceedingDTO) {
    ContentRelatedIndexing.ContentRelatedIndexingBuilder contentRelatedIndexingBuilder =
        ContentRelatedIndexing.builder();

    if (pendingProceedingDTO.getDocumentationUnitKeywordDTOs() != null) {
      List<String> keywords =
          pendingProceedingDTO.getDocumentationUnitKeywordDTOs().stream()
              .map(
                  documentationUnitKeywordDTO ->
                      documentationUnitKeywordDTO.getKeyword().getValue())
              .toList();
      contentRelatedIndexingBuilder.keywords(keywords);
    }

    if (pendingProceedingDTO.getNormReferences() != null) {
      List<NormReference> norms = addNormReferencesToDomain(pendingProceedingDTO);
      contentRelatedIndexingBuilder.norms(norms);
    }

    if (pendingProceedingDTO.getDocumentationUnitFieldsOfLaw() != null) {
      List<FieldOfLaw> fieldOfLaws =
          pendingProceedingDTO.getDocumentationUnitFieldsOfLaw().stream()
              .map(
                  documentationUnitFieldOfLawDTO ->
                      FieldOfLawTransformer.transformToDomain(
                          documentationUnitFieldOfLawDTO.getFieldOfLaw(), false, false))
              .toList();

      contentRelatedIndexingBuilder.fieldsOfLaw(fieldOfLaws);
    }

    return contentRelatedIndexingBuilder.build();
  }

  /**
   * Adds norm references to the domain object based on the provided documentation unit DTO. A list
   * of NormReferenceDTOs with the same normAbbreviation are grouped into one NormReference, with a
   * list of {@link SingleNorm}.
   *
   * @param documentationUnitDTO The documentation unit DTO containing norm references to be added.
   * @return A list of NormReference objects representing the added norm references.
   */
  private static List<NormReference> addNormReferencesToDomain(
      PendingProceedingDTO documentationUnitDTO) {
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

  private static void addStatusToDomain(
      PendingProceedingDTO documentationUnitDTO,
      PendingProceeding.PendingProceedingBuilder builder) {
    builder.status(StatusTransformer.transformToDomain(documentationUnitDTO.getStatus()));
  }

  private static void addPreviousDecisionsToDomain(
      PendingProceedingDTO documentationUnitDTO,
      PendingProceeding.PendingProceedingBuilder builder) {
    if (documentationUnitDTO.getPreviousDecisions() == null) {
      return;
    }

    builder.previousDecisions(
        documentationUnitDTO.getPreviousDecisions().stream()
            .map(PreviousDecisionTransformer::transformToDomain)
            .toList());
  }

  private static void addDeviatingCourtsToDomain(
      PendingProceedingDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
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
      PendingProceedingDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
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
      PendingProceedingDTO documentationUnitDTO, CoreDataBuilder coreDataBuilder) {
    if (documentationUnitDTO.getFileNumbers() == null) {
      return;
    }

    List<String> fileNumbers =
        documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
    coreDataBuilder.fileNumbers(fileNumbers);
  }
}
