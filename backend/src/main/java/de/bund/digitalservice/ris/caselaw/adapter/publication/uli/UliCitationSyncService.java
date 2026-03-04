package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
public class UliCitationSyncService {

  private final DatabaseDocumentationUnitRepository caselawRepository;
  private final PublishedUliRepository publishedUliRepository;
  private final RevokedUliRepository revokedUliRepository;
  private final PortalPublicationService portalPublicationService;

  public UliCitationSyncService(
      DatabaseDocumentationUnitRepository caselawRepository,
      PublishedUliRepository publishedUliRepository,
      RevokedUliRepository revokedUliRepository,
      PortalPublicationService portalPublicationService) {
    this.caselawRepository = caselawRepository;
    this.publishedUliRepository = publishedUliRepository;
    this.revokedUliRepository = revokedUliRepository;
    this.portalPublicationService = portalPublicationService;
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
  public Set<String> handleNewlyPublishedAfter(Instant lastRun) {
    // Delta of newly published ULI documents
    Set<String> documentsToRepublish =
        publishedUliRepository.findAllByPublishedAtAfter(lastRun).stream()
            .map(this::syncPassiveCitationsForUli)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    triggerRepublishing(documentsToRepublish);

    return documentsToRepublish;
  }

  private Set<String> syncPassiveCitationsForUli(UliDTO uli) {
    Set<String> affectedDocNumbers = new HashSet<>();

    uli.getActiveCaselawReferences()
        .forEach(
            link -> {
              caselawRepository
                  .findById(link.getTargetDocumentationUnitId())
                  .ifPresent(
                      docUnit -> {
                        if (docUnit instanceof DecisionDTO decision) {

                          boolean changed = checkIsChangedAndUpdate(uli, decision);

                          checkForMissingPassiveCitations(decision, uli.getId());

                          if (changed) {
                            caselawRepository.save(decision);
                            affectedDocNumbers.add(decision.getDocumentNumber());
                          }
                        }
                      });
            });

    return affectedDocNumbers;
  }

  private boolean checkIsChangedAndUpdate(UliDTO uli, DecisionDTO decision) {
    boolean changed = false;
    for (PassiveCitationUliDTO passive : decision.getPassiveUliCitations()) {
      if (uli.getId().equals(passive.getSourceId())) {
        if (updateMetadataIfChanged(passive, uli)) {
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Updates metadata only if values have actually changed.
   *
   * @return true if at least one field was updated.
   */
  private boolean updateMetadataIfChanged(PassiveCitationUliDTO passive, UliDTO uli) {
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

  private void checkForMissingPassiveCitations(DecisionDTO decision, UUID uliId) {
    boolean hasPassiveCounterpart =
        decision.getPassiveUliCitations().stream()
            .anyMatch(passive -> uliId.equals(passive.getSourceId()));

    if (!hasPassiveCounterpart) {
      log.warn(
          "Inconsistency: Active citation in ULI {} points to Caselaw doc {}, with id {} but passive counterpart is missing in Caselaw document.",
          uliId,
          decision.getDocumentNumber(),
          decision.getId());
    }
  }

  /** Case 3: Identify documents that point to revoked ULI documents. */
  @Transactional
  public void handleRevokedAfter(Instant lastRun) {
    List<RevokedUli> revokedEntries = revokedUliRepository.findAllByRevokedAtAfter(lastRun);

    if (revokedEntries.isEmpty()) {
      log.atInfo().addKeyValue("lastRun", lastRun).setMessage("No new revoked entries").log();
      return;
    }

    Set<String> documentsToRepublish =
        revokedEntries.stream()
            .map(this::syncCitationsForRevokedUli)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    triggerRepublishing(documentsToRepublish);
  }

  private Set<String> syncCitationsForRevokedUli(RevokedUli revokedUli) {
    log.info("Checking citations for revoked ULI: {}", revokedUli.getDocUnitId());
    Set<String> affectedDocNumbers = new HashSet<>();
    UUID revokedId = revokedUli.getDocUnitId();

    // remove passive citations that point to the revoked ULI
    List<DecisionDTO> documentsWithPassive =
        caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(revokedId);

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision.getPassiveUliCitations().removeIf(p -> revokedId.equals(p.getSourceId()));

      if (removed) {
        caselawRepository.save(decision);
        affectedDocNumbers.add(decision.getDocumentNumber());
        log.info(
            "Removed passive citation to revoked ULI {} from doc {}",
            revokedId,
            decision.getDocumentNumber());
      } else {
        log.error(
            "Inconsistency during revocation: Document {} was fetched as 'affected' from revoked case, but no matching passive ULI citation was found for the revoked ULI ID {}",
            decision.getDocumentNumber(),
            revokedId);
      }
    }

    // active citations will be just republished. This removes the target id and document number
    // from the published data.
    List<DecisionDTO> affectedByActive =
        caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(revokedId);

    for (DecisionDTO decision : affectedByActive) {
      affectedDocNumbers.add(decision.getDocumentNumber());
    }

    return affectedDocNumbers;
  }

  private void triggerRepublishing(Set<String> docNumbers) {
    if (docNumbers.isEmpty()) return;

    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              log.info(
                  "ULI Passive Citation Sync: Committed. Republishing {} docs", docNumbers.size());
              docNumbers.forEach(UliCitationSyncService.this::publishSafe);
            }
          });
    } else {
      docNumbers.forEach(this::publishSafe);
    }
  }

  private void publishSafe(String docNumber) {
    try {
      portalPublicationService.publishDocumentationUnit(docNumber);
      log.debug("Successfully republished {} after revoked check", docNumber);
    } catch (Exception e) {
      log.error("Failed to republish {} during ULI revoked sync", docNumber, e);
    }
  }
}
