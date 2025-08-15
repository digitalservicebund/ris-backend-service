package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import jakarta.validation.Valid;
import java.util.ArrayList;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for transforming a pending proceeding object from its domain
 * representation into a database object and back
 */
@Slf4j
public class PendingProceedingTransformer extends DocumentableTransformer {
  private PendingProceedingTransformer() {}

  /**
   * Transforms a pending proceeding domain object into its database representation.
   *
   * @param pendingProceeding the domain object
   * @return a transformed database pending proceeding object
   */
  public static PendingProceedingDTO transformToDTO(
      PendingProceedingDTO currentDto, PendingProceeding pendingProceeding) {
    if (pendingProceeding == null) {
      throw new DocumentationUnitTransformerException(
          "Pending proceeding is null and won't transform");
    }

    final var builder = currentDto.toBuilder();

    builder
        .id(pendingProceeding.uuid())
        .documentNumber(pendingProceeding.documentNumber())
        .version(pendingProceeding.version());
    addPreviousDecisions(pendingProceeding, builder);

    if (pendingProceeding.coreData() != null) {
      var coreData = pendingProceeding.coreData();

      builder
          .judicialBody(StringUtils.normalizeSpace(coreData.appraisalBody()))
          .date(coreData.decisionDate()) // Mitteilungsdatum
          .documentType(
              coreData.documentType() != null
                  ? DocumentTypeTransformer.transformToDTO(coreData.documentType())
                  : null)
          .court(CourtTransformer.transformToDTO(coreData.court()))
          .isResolved(coreData.isResolved())
          .resolutionDate(coreData.resolutionDate());

      addDeviatingDocumentNumbers(builder, coreData, currentDto);
      addFileNumbers(builder, coreData, currentDto);
      addDeviationCourts(builder, coreData);
      addDeviatingDecisionDates(builder, coreData);
      addDeviatingFileNumbers(builder, coreData, currentDto);
    } else {
      builder
          .judicialBody(null)
          .date(null)
          .court(null)
          .documentType(null)
          .documentationOffice(null);
    }

    if (pendingProceeding.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = pendingProceeding.contentRelatedIndexing();
      addNormReferences(builder, contentRelatedIndexing);
      // TODO: Passivzitierung Verwaltungsvorschriften
    }

    if (pendingProceeding.shortTexts() != null) {
      PendingProceedingShortTexts shortTexts = pendingProceeding.shortTexts();
      builder
          .headline(shortTexts.headline())
          .resolutionNote(shortTexts.resolutionNote())
          .legalIssue(shortTexts.legalIssue())
          .admissionOfAppeal(shortTexts.admissionOfAppeal())
          .appellant(shortTexts.appellant());
    } else {
      builder.headline(null);
    }

    // Calls to pre-build helper methods that populate the builder
    addCaselawReferences(pendingProceeding, builder, currentDto);
    addLiteratureReferences(pendingProceeding, builder, currentDto);
    addManagementData(pendingProceeding, builder);

    PendingProceedingDTO result = builder.build();

    return DocumentableTransformer.postProcessRelationships(result, currentDto);
  }

  public static PendingProceeding transformToDomain(PendingProceedingDTO pendingProceedingDTO) {
    return transformToDomain(pendingProceedingDTO, null);
  }

  /**
   * Transforms a pending proceeding object from its database representation into a domain object
   * that is suitable to be consumed by clients of the REST service.
   *
   * @param pendingProceedingDTO the database pending proceeding object
   * @return a transformed domain object, or an empty domain object if the input is null
   */
  public static PendingProceeding transformToDomain(
      PendingProceedingDTO pendingProceedingDTO, @Nullable User user) {
    if (pendingProceedingDTO == null) {
      throw new DocumentationUnitTransformerException(
          "Pending proceeding is null and won't transform");
    }

    log.debug(
        "transfer database pending proceeding '{}' to domain object", pendingProceedingDTO.getId());

    return PendingProceeding.builder()
        .uuid(pendingProceedingDTO.getId())
        .version(pendingProceedingDTO.getVersion())
        .documentNumber(pendingProceedingDTO.getDocumentNumber())
        .portalPublicationStatus(pendingProceedingDTO.getPortalPublicationStatus())
        .coreData(buildCoreData(pendingProceedingDTO))
        .shortTexts(
            PendingProceedingShortTexts.builder()
                .headline(pendingProceedingDTO.getHeadline())
                .resolutionNote(pendingProceedingDTO.getResolutionNote())
                .legalIssue(pendingProceedingDTO.getLegalIssue())
                .admissionOfAppeal(pendingProceedingDTO.getAdmissionOfAppeal())
                .appellant(pendingProceedingDTO.getAppellant())
                .build())
        .contentRelatedIndexing(buildContentRelatedIndexing(pendingProceedingDTO))
        .managementData(ManagementDataTransformer.transformToDomain(pendingProceedingDTO, user))
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
                    .toList())
        .documentalists(
            pendingProceedingDTO.getDocumentalists() == null
                ? new ArrayList<>()
                : pendingProceedingDTO.getDocumentalists().stream()
                    .map(DocumentalistDTO::getValue)
                    .toList())
        .status(getStatus(pendingProceedingDTO))
        .previousDecisions(getPreviousDecisions(pendingProceedingDTO))
        .isDeletable(false)
        .isEditable(false)
        .build();
  }

  private static @Valid CoreData buildCoreData(PendingProceedingDTO pendingProceedingDTO) {
    CoreData mutualCoreData = buildMutualCoreData(pendingProceedingDTO);

    // transform pending proceeding specific core data fields
    return mutualCoreData.toBuilder()
        .isResolved(pendingProceedingDTO.isResolved())
        .resolutionDate(pendingProceedingDTO.getResolutionDate())
        .build();
  }
}
