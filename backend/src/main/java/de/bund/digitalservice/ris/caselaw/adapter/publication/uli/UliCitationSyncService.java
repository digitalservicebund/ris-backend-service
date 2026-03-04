package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliCaselaw;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliCaselawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUliRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UliCitationSyncService {

  private final DatabaseDocumentationUnitRepository caselawRepository;
  private final ActiveCitationUliCaselawRepository activeCitationUliCaselawRepository;
  private final PublishedUliRepository publishedUliRepository;
  private final RevokedUliRepository revokedUliRepository;
  private final JobSyncStatusRepository jobSyncStatusRepository;

  public UliCitationSyncService(
      DatabaseDocumentationUnitRepository caselawRepository,
      ActiveCitationUliCaselawRepository activeCitationUliCaselawRepository,
      PublishedUliRepository publishedUliRepository,
      RevokedUliRepository revokedUliRepository,
      JobSyncStatusRepository jobSyncStatusRepository) {
    this.caselawRepository = caselawRepository;
    this.activeCitationUliCaselawRepository = activeCitationUliCaselawRepository;
    this.publishedUliRepository = publishedUliRepository;
    this.revokedUliRepository = revokedUliRepository;
    this.jobSyncStatusRepository = jobSyncStatusRepository;
  }

  /**
   * Case 2: Synchronizes ULI metadata and monitors citation consistency based on new ULI
   * publications.
   *
   * <p>1. Metadata Refresh: Iterates through newly published ULI documents and updates existing
   * passive counterparts in the referenced Caselaw documents. This ensures that metadata (author,
   * citation, etc.) remains synchronized.
   *
   * <p>2. Inconsistency Monitoring: Detects missing links by checking if an Active Citation within
   * a new ULI document has a corresponding Passive Citation in the targeted Caselaw document. If
   * the counterpart is missing, a warning is logged for monitoring purposes. No citations are
   * created automatically.
   */
  @Transactional
  public Set<String> handleUliPassiveSync() {
    Set<String> documentsToRepublish = new HashSet<>();
    String jobName = "ULI_PASSIVE_CITATION_SYNC";

    Instant lastRun =
        jobSyncStatusRepository
            .findById(jobName)
            .map(JobSyncStatus::getLastRun)
            .orElse(Instant.EPOCH);

    // Delta of newly published ULI documents
    List<PublishedUli> newUlis = publishedUliRepository.findAllByPublishedAtAfter(lastRun);
    if (newUlis.isEmpty()) return documentsToRepublish;

    Set<UUID> uliIds = newUlis.stream().map(PublishedUli::getId).collect(Collectors.toSet());

    // Get all relevant links from ref_view_active_citation_uli_caselaw for the loop
    List<ActiveCitationUliCaselaw> uliToCaselawActiveCitations =
        activeCitationUliCaselawRepository.findAllBySourceIdIn(uliIds);

    // Get all affected caselaw documents
    List<DecisionDTO> affectedCaselawDecisions =
        caselawRepository.findAllAffectedByUliUpdates(uliIds);

    for (DecisionDTO decision : affectedCaselawDecisions) {
      boolean changed = false;

      for (PassiveCitationUliDTO passive : decision.getPassiveUliCitations()) {

        if (uliIds.contains(passive.getSourceId())) {
          Optional<PublishedUli> uliDataOpt =
              newUlis.stream().filter(u -> u.getId().equals(passive.getSourceId())).findFirst();

          if (uliDataOpt.isPresent()) {
            if (updateMetadataIfChanged(passive, uliDataOpt.get())) {
              changed = true;
            }
          }
        }
      }

      checkForMissingPassiveCitations(decision, uliToCaselawActiveCitations);

      if (changed) {
        caselawRepository.save(decision);
        documentsToRepublish.add(decision.getDocumentNumber());
      }
    }

    // Update status table with new timestamp
    newUlis.stream()
        .map(PublishedUli::getPublishedAt)
        .max(Comparator.naturalOrder())
        .ifPresent(ts -> updateJobStatus(jobName, ts));

    return documentsToRepublish;
  }

  /**
   * Updates metadata only if values have actually changed.
   *
   * @return true if at least one field was updated.
   */
  private boolean updateMetadataIfChanged(PassiveCitationUliDTO passive, PublishedUli uli) {
    boolean hasChanged = false;

    if (!Objects.equals(passive.getSourceAuthor(), uli.getAuthor())) {
      passive.setSourceAuthor(uli.getAuthor());
      hasChanged = true;
    }
    if (!Objects.equals(passive.getSourceCitation(), uli.getCitation())) {
      passive.setSourceCitation(uli.getCitation());
      hasChanged = true;
    }
    if (!Objects.equals(passive.getSourceDocumentTypeRawValue(), uli.getDocumentTypeRawValue())) {
      passive.setSourceDocumentTypeRawValue(uli.getDocumentTypeRawValue());
      hasChanged = true;
    }
    if (!Objects.equals(
        passive.getSourceLegalPeriodicalRawValue(), uli.getLegalPeriodicalRawValue())) {
      passive.setSourceLegalPeriodicalRawValue(uli.getLegalPeriodicalRawValue());
      hasChanged = true;
    }

    return hasChanged;
  }

  private void checkForMissingPassiveCitations(
      DecisionDTO decision, List<ActiveCitationUliCaselaw> allLinks) {

    // Get uli to caselaw active citations pointing to this decision
    List<UUID> expectedUliSourceIds =
        allLinks.stream()
            .filter(link -> link.getTargetId().equals(decision.getId()))
            .map(ActiveCitationUliCaselaw::getSourceId)
            .toList();

    for (UUID expectedId : expectedUliSourceIds) {
      // Check for each uli Id, if there is a corresponding passive citation
      boolean hasPassiveCounterpart =
          decision.getPassiveUliCitations().stream()
              .anyMatch(passive -> passive.getSourceId().equals(expectedId));

      if (!hasPassiveCounterpart) {
        log.warn(
            "Inconsistency: Active citation in ULI {} points to Caselaw doc {}, with id {} but passive counterpart is missing in Caselaw document.",
            expectedId,
            decision.getDocumentNumber(),
            decision.getId());
      }
    }
  }

  /** Case 3: Identify documents that point to revoked ULI documents. */
  @Transactional
  public Set<String> handleUliRevoked() {
    Set<String> documentsToRepublish = new HashSet<>();
    String jobName = "ULI_REVOKED_SYNC";

    Instant lastRun =
        jobSyncStatusRepository
            .findById(jobName)
            .map(JobSyncStatus::getLastRun)
            .orElse(Instant.EPOCH);

    var currentRunTimestamp = Instant.now();
    List<RevokedUli> revokedEntries = revokedUliRepository.findAllByRevokedAtAfter(lastRun);

    if (revokedEntries.isEmpty()) {
      log.info("No new revoked entries since {}", lastRun);
      return documentsToRepublish;
    }

    Set<UUID> revokedUliIds =
        revokedEntries.stream().map(RevokedUli::getDocUnitId).collect(Collectors.toSet());

    // remove passive citations
    List<DecisionDTO> documentsWithPassive =
        caselawRepository.findAllByPassiveUliSourceIdInAndPendingRevocation(revokedUliIds);

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision
              .getPassiveUliCitations()
              .removeIf(p -> p.getSourceId() != null && revokedUliIds.contains(p.getSourceId()));

      if (removed) {
        caselawRepository.save(decision);
        documentsToRepublish.add(decision.getDocumentNumber());
      } else {
        log.error(
            "Inconsistency during revocation: Document {} was fetched as 'affected' from revoked case, but no matching passive ULI citation was found for the revoked ULI IDs {}",
            decision.getDocumentNumber(),
            revokedUliIds);
      }
    }

    // active citations will be just republished, targetId will be deleted
    List<DecisionDTO> affectedByActive =
        caselawRepository.findAllByActiveUliTargetIdInAndPendingRevocation(revokedUliIds);

    for (DecisionDTO decision : affectedByActive) {
      documentsToRepublish.add(decision.getDocumentNumber());
    }

    updateJobStatus(jobName, currentRunTimestamp);

    return documentsToRepublish;
  }

  private void updateJobStatus(String name, Instant time) {
    JobSyncStatus status =
        jobSyncStatusRepository.findById(name).orElse(new JobSyncStatus(name, time));
    status.setLastRun(time);
    jobSyncStatusRepository.save(status);
  }
}
