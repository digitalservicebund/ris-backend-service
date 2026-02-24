package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.PublishedUli;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.RevokedUli;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
   * Case 2: Find documents that need new passive ULI citations based on active citations from other
   * streams. * @return Set of document numbers that need to be republished.
   */
  @Transactional
  public Set<String> syncUliPassiveCitations() {
    Set<String> documentsToRepublish = new HashSet<>();

    var allUliToCaselawActiveCitations = activeCitationUliCaselawRepository.findAll();

    for (var link : allUliToCaselawActiveCitations) {
      caselawRepository
          .findById(link.getTargetId())
          .ifPresent(
              documentationUnit -> {
                if (documentationUnit instanceof DecisionDTO decision) {

                  publishedUliRepository
                      .findById(link.getSourceId())
                      .ifPresent(
                          uliRef -> {
                            var existingPassiveCitationOpt =
                                decision.getPassiveUliCitations().stream()
                                    .filter(
                                        p ->
                                            uliRef
                                                .getDocumentNumber()
                                                .equals(p.getSourceLiteratureDocumentNumber()))
                                    .findFirst();

                            if (existingPassiveCitationOpt.isEmpty()) {
                              var newPassive =
                                  PassiveCitationUliDTO.builder()
                                      .sourceId(uliRef.getId())
                                      .sourceLiteratureDocumentNumber(uliRef.getDocumentNumber())
                                      .sourceCitation(uliRef.getCitation())
                                      .sourceAuthor(uliRef.getAuthor())
                                      .sourceDocumentTypeRawValue(uliRef.getDocumentTypeRawValue())
                                      .sourceLegalPeriodicalRawValue(
                                          uliRef.getLegalPeriodicalRawValue())
                                      .rank(decision.getPassiveUliCitations().size() + 1)
                                      .build();

                              decision.getPassiveUliCitations().add(newPassive);
                              caselawRepository.save(decision);
                              documentsToRepublish.add(decision.getDocumentNumber());
                            } else {
                              var existingPassive = existingPassiveCitationOpt.get();
                              if (existingCitationChanged(existingPassive, uliRef)) {
                                existingPassive.setSourceCitation(uliRef.getCitation());
                                existingPassive.setSourceAuthor(uliRef.getAuthor());
                                existingPassive.setSourceDocumentTypeRawValue(
                                    uliRef.getDocumentTypeRawValue());
                                existingPassive.setSourceLegalPeriodicalRawValue(
                                    uliRef.getLegalPeriodicalRawValue());

                                caselawRepository.save(decision);
                                documentsToRepublish.add(decision.getDocumentNumber());
                              }
                            }
                          });
                }
              });
    }

    return documentsToRepublish;
  }

  private boolean existingCitationChanged(PassiveCitationUliDTO existing, PublishedUli ref) {
    return !Objects.equals(existing.getSourceCitation(), ref.getCitation())
        || !Objects.equals(existing.getSourceAuthor(), ref.getAuthor())
        || !Objects.equals(existing.getSourceDocumentTypeRawValue(), ref.getDocumentTypeRawValue())
        || !Objects.equals(
            existing.getSourceLegalPeriodicalRawValue(), ref.getLegalPeriodicalRawValue());
  }

  /** Case 3: Identify documents that point to repealed or deleted ULI documents. */
  @Transactional
  public Set<String> handleUliRevoked() {
    Set<String> documentsToRepublish = new HashSet<>();
    String jobName = "ULI_REVOKED_SYNC";

    Instant lastRun =
        jobSyncStatusRepository
            .findById(jobName)
            .map(JobSyncStatus::getLastRun)
            .orElse(Instant.EPOCH);

    List<RevokedUli> revokedEntries = revokedUliRepository.findAllByRevokedAtAfter(lastRun);

    if (revokedEntries.isEmpty()) {
      log.info("No new revoked entries since {}", lastRun);
      return documentsToRepublish;
    }

    Set<UUID> revokedUliIds =
        revokedEntries.stream().map(RevokedUli::getDocUnitId).collect(Collectors.toSet());

    // remove passive citations
    List<DecisionDTO> documentsWithPassive =
        caselawRepository.findAllByPassiveUliCitationSourceId(revokedUliIds);

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision
              .getPassiveUliCitations()
              .removeIf(p -> p.getSourceId() != null && revokedUliIds.contains(p.getSourceId()));

      if (removed) {
        caselawRepository.save(decision);
        documentsToRepublish.add(decision.getDocumentNumber());
      }
    }

    // active citations will be just republished, targetId is deleted and metadata stay
    List<DecisionDTO> affectedByActive =
        caselawRepository.findAllByActiveUliCitationTargetId(revokedUliIds);

    for (DecisionDTO decision : affectedByActive) {
      documentsToRepublish.add(decision.getDocumentNumber());
    }

    // get the highest timestamp of revoked entries and not Instant.now() because in the meantime a
    // new entry
    // could have been saved to the revoked table
    Instant newestRevokedAt =
        revokedEntries.stream()
            .map(RevokedUli::getRevokedAt)
            .max(Comparator.naturalOrder())
            .orElse(lastRun);

    updateJobStatus(jobName, newestRevokedAt);

    return documentsToRepublish;
  }

  private void updateJobStatus(String name, Instant time) {
    JobSyncStatus status =
        jobSyncStatusRepository.findById(name).orElse(new JobSyncStatus(name, time));
    status.setLastRun(time);
    jobSyncStatusRepository.save(status);
  }
}
