package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
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
  private final DatabaseUliRepository databaseUliRepository;
  private final RevokedUliRepository revokedUliRepository;
  private final PortalPublicationService portalPublicationService;

  public UliCitationSyncService(
      DatabaseDocumentationUnitRepository caselawRepository,
      DatabaseUliRepository databaseUliRepository,
      RevokedUliRepository revokedUliRepository,
      PortalPublicationService portalPublicationService) {
    this.caselawRepository = caselawRepository;
    this.databaseUliRepository = databaseUliRepository;
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
  public void handleNewlyPublishedAfter(Instant lastRun) {
    // Delta of newly published ULI documents
    Set<UUID> documentsToRepublish =
        databaseUliRepository.findAllByPublishedAtAfter(lastRun).stream()
            .map(this::syncPassiveCitationsForUli)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    triggerRepublishing(documentsToRepublish);
  }

  private Set<UUID> syncPassiveCitationsForUli(UliDTO uli) {
    Set<UUID> documentsToRepublish = new HashSet<>();

    uli.getActiveCaselawReferences()
        .forEach(
            link ->
                caselawRepository
                    .findById(link.getTargetDocumentationUnitId())
                    .ifPresent(
                        docUnit -> {
                          if (docUnit instanceof DecisionDTO decision) {

                            boolean changed = checkIsChangedAndUpdate(uli, decision);

                            checkForMissingPassiveCitations(decision, uli.getId());

                            if (changed) {
                              caselawRepository.save(decision);
                              documentsToRepublish.add(decision.getId());
                            }
                          }
                        }));

    return documentsToRepublish;
  }

  private boolean checkIsChangedAndUpdate(UliDTO uli, DecisionDTO decision) {
    boolean changed = false;
    for (PassiveCitationUliDTO passive : decision.getPassiveUliCitations()) {
      boolean isSameSource =
          Objects.equals(passive.getSourceId(), uli.getId())
              || Objects.equals(
                  passive.getSourceLiteratureDocumentNumber(), uli.getDocumentNumber());
      if (isSameSource && updateMetadataIfChanged(passive, uli)) {
        changed = true;
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

    if (!Objects.equals(passive.getSourceId(), uli.getId())) {
      passive.setSourceId(uli.getId());
      hasChanged = true;
    }

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

    if (hasChanged) {
      log.atInfo()
          .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, uli.getId())
          .addKeyValue(LoggingKeys.TARGET_DOCUMENT_NUMBER, passive.getTarget().getDocumentNumber())
          .setMessage(
              "Updating metadata of matching passive citation due to changes in ULI source.")
          .log();
    }

    return hasChanged;
  }

  private void checkForMissingPassiveCitations(DecisionDTO decision, UUID uliId) {
    boolean hasPassiveCounterpart =
        decision.getPassiveUliCitations().stream()
            .anyMatch(passive -> uliId.equals(passive.getSourceId()));

    if (!hasPassiveCounterpart) {
      log.atWarn()
          .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, uliId)
          .addKeyValue(LoggingKeys.TARGET_DOCUMENT_NUMBER, decision.getDocumentNumber())
          .addKeyValue(LoggingKeys.DOCUMENT_ID, decision.getId())
          .setMessage(
              "DISABLED: Creating missing passive citation for published active citation in ULI document.")
          .log();
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

    Set<UUID> documentsToRepublish =
        revokedEntries.stream()
            .map(this::syncCitationsForRevokedUli)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    triggerRepublishing(documentsToRepublish);
  }

  private Set<UUID> syncCitationsForRevokedUli(RevokedUli revokedUli) {
    log.atInfo()
        .addKeyValue(LoggingKeys.REVOKED_ULI, revokedUli.getDocUnitId())
        .setMessage("Checking active and passive citations for references to revoked ULI.")
        .log();

    Set<UUID> documentsToRepublish = new HashSet<>();
    UUID revokedId = revokedUli.getDocUnitId();
    if (revokedId == null) {
      log.atError()
          .addKeyValue("revokedUliEntry", revokedUli)
          .setMessage("RevokedUli entry found without docUnitId. Skipping sync for this entry.")
          .log();
      return new HashSet<>();
    }

    // remove passive citations that point to the revoked ULI
    List<DecisionDTO> decisionsWithAffectedPassiveCitations =
        caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(revokedId);

    for (DecisionDTO decision : decisionsWithAffectedPassiveCitations) {
      documentsToRepublish.add(decision.getId());
      log.atInfo()
          .addKeyValue(LoggingKeys.REVOKED_ULI, revokedId)
          .addKeyValue(LoggingKeys.AFFECTED_DOCUMENT_NUMBER, decision.getDocumentNumber())
          .setMessage(
              "Passive Citation to revoked ULI detected. Document added to republishing queue for validation in publish step.")
          .log();
    }

    List<DecisionDTO> decisionsWithAffectedActiveCitations =
        caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(revokedId);

    for (DecisionDTO decision : decisionsWithAffectedActiveCitations) {
      documentsToRepublish.add(decision.getId());
      log.atInfo()
          .addKeyValue(LoggingKeys.REVOKED_ULI, revokedId)
          .addKeyValue(LoggingKeys.AFFECTED_DOCUMENT_NUMBER, decision.getDocumentNumber())
          .setMessage(
              "Active Citation to revoked ULI detected. Document added to republishing queue for validation in publish step.")
          .log();
    }

    return documentsToRepublish;
  }

  private void triggerRepublishing(Set<UUID> documentsToRepublish) {
    if (documentsToRepublish.isEmpty()) {
      log.atInfo().setMessage("No documents found for republishing. Skipping sync step.").log();
      return;
    }
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              log.atInfo()
                  .addKeyValue("count", documentsToRepublish.size())
                  .setMessage("ULI Citation Sync: Committed. Republishing docs")
                  .log();
              documentsToRepublish.forEach(
                  docId -> {
                    try {
                      portalPublicationService.publishDocumentationUnitWithChangelog(docId, null);
                      log.atDebug()
                          .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                          .setMessage("Successfully republished after ULI revoked sync")
                          .log();
                    } catch (Exception e) {
                      log.atError()
                          .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                          .addKeyValue("exception", e.getMessage())
                          .setMessage("Failed to republish during ULI revoked sync")
                          .log();
                    }
                  });
            }
          });
    } else {
      documentsToRepublish.forEach(
          docId -> {
            try {
              portalPublicationService.publishDocumentationUnitWithChangelog(docId, null);
            } catch (Exception e) {
              log.atError()
                  .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                  .addKeyValue("exception", e.getMessage())
                  .setMessage("Failed to republish during ULI sync")
                  .log();
            }
          });
    }
  }
}
