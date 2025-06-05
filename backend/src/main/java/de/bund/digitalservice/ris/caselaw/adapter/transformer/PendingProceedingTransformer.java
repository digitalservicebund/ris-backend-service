package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import java.util.ArrayList;
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

    builder.id(pendingProceeding.uuid()).documentNumber(pendingProceeding.documentNumber());
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
          .court(CourtTransformer.transformToDTO(coreData.court()));

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

    builder
        .resolutionNote(pendingProceeding.resolutionNote())
        .isResolved(pendingProceeding.isResolved())
        .legalIssue(pendingProceeding.legalIssue())
        .admissionOfAppeal(pendingProceeding.admissionOfAppeal())
        .appellant(pendingProceeding.appellant());
    // TODO: Erledigungsmitteilung

    if (pendingProceeding.shortTexts() != null) {
      ShortTexts shortTexts = pendingProceeding.shortTexts();
      builder.headline(shortTexts.headline());
    } else {
      builder.headline(null);
    }

    // TODO: move managementData from decision to documentation unit
    //    if (pendingProceeding.managementData() != null) {
    //      var managementData = updatedDomainObject.managementData();
    //      builder.scheduledPublicationDateTime(managementData.scheduledPublicationDateTime());
    //      builder.lastPublicationDateTime(managementData.lastPublicationDateTime());
    //      builder.scheduledByEmail(managementData.scheduledByEmail());
    //    }

    addCaselawReferences(pendingProceeding, builder, currentDto);
    addLiteratureReferences(pendingProceeding, builder, currentDto);

    PendingProceedingDTO result = builder.build();
    if (currentDto.getManagementData() != null) {
      currentDto.getManagementData().setDocumentationUnit(result);
      result.setManagementData(currentDto.getManagementData());
    }
    return result;
  }

  /**
   * Transforms a pending proceeding object from its database representation into a domain object
   * that is suitable to be consumed by clients of the REST service.
   *
   * @param pendingProceedingDTO the database pending proceeding object
   * @return a transformed domain object, or an empty domain object if the input is null
   */
  public static PendingProceeding transformToDomain(PendingProceedingDTO pendingProceedingDTO) {
    if (pendingProceedingDTO == null) {
      throw new DocumentationUnitTransformerException(
          "Pending proceeding is null and won't transform");
    }

    log.debug(
        "transfer database pending proceeding '{}' to domain object", pendingProceedingDTO.getId());

    return PendingProceeding.builder()
        .uuid(pendingProceedingDTO.getId())
        .documentNumber(pendingProceedingDTO.getDocumentNumber())
        .coreData(buildCoreData(pendingProceedingDTO))
        .shortTexts(
            ShortTexts.builder()
                .headline(pendingProceedingDTO.getHeadline())
                .guidingPrinciple(pendingProceedingDTO.getGuidingPrinciple())
                .headnote(pendingProceedingDTO.getHeadnote())
                .build())
        .contentRelatedIndexing(buildContentRelatedIndexing(pendingProceedingDTO))
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
        .resolutionNote(pendingProceedingDTO.getResolutionNote())
        .isResolved(pendingProceedingDTO.isResolved())
        .legalIssue(pendingProceedingDTO.getLegalIssue())
        .admissionOfAppeal(pendingProceedingDTO.getAdmissionOfAppeal())
        .appellant(pendingProceedingDTO.getAppellant())
        .isDeletable(false)
        .isEditable(false)
        .build();
  }
}
