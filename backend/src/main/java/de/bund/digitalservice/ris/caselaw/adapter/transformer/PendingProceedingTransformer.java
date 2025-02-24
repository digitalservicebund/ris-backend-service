package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
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
   * /** Transforms a pending proceeding object from its database representation into a domain
   * object that is suitable to be consumed by clients of the REST service.
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
        .status(getStatus(pendingProceedingDTO))
        .previousDecisions(getPreviousDecisions(pendingProceedingDTO))
        .resolutionNote(pendingProceedingDTO.getResolutionNote())
        .isResolved(pendingProceedingDTO.isResolved())
        .legalIssue(pendingProceedingDTO.getLegalIssue())
        .admissionOfAppeal(pendingProceedingDTO.getAdmissionOfAppeal())
        .appellant(pendingProceedingDTO.getAppellant())
        .build();
  }
}
