package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedSli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import java.time.Instant;
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
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class SliCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseSliRepository sliRepository;
  private final RevokedSliRepository revokedSliRepository;
  private final PortalPublicationService portalPublicationService;
  private final TransactionTemplate transactionTemplate;

  public SliCitationSyncService(
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseSliRepository sliRepository,
      RevokedSliRepository revokedSliRepository,
      PortalPublicationService portalPublicationService,
      TransactionTemplate transactionTemplate) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.sliRepository = sliRepository;
    this.revokedSliRepository = revokedSliRepository;
    this.portalPublicationService = portalPublicationService;
    this.transactionTemplate = transactionTemplate;
  }

  public void handleNewlyPublishedAfter(Instant after) {
    var documentsToPublish =
        transactionTemplate.execute(
            (status) ->
                sliRepository.findAllByPublishedAtAfter(after).stream()
                    .map(this::syncCitations)
                    .flatMap(Set::stream)
                    // documents that have changed and need to be republished
                    .distinct()
                    .toList());

    documentsToPublish.forEach(
        docId -> {
          try {
            portalPublicationService.publishDocumentationUnitWithChangelog(docId, null);
            log.atInfo()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .setMessage("Successfully republished after SLI newly published sync")
                .log();
          } catch (Exception e) {
            log.atError()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .addKeyValue("exception", e)
                .setMessage("Failed to republish during SLI newly published sync")
                .log();
          }
        });
  }

  /**
   * Create passive citations for all active citations of the doc unit.
   *
   * @return ids of all other documents that have been changed. These should be published again.
   */
  private Set<UUID> syncCitations(SliDTO sli) {
    Set<UUID> documentsToRepublish = new HashSet<>();

    sli.getActiveCaselawReferences()
        .forEach(
            activeCitation -> {
              var target =
                  documentationUnitRepository.findById(
                      activeCitation.getTargetDocumentationUnitId());

              if (target.isPresent() && target.get() instanceof DecisionDTO targetDecision) {
                var matchingPassiveCitation =
                    findMatchingPassiveCitation(
                        targetDecision, sli.getId(), sli.getDocumentNumber());
                if (matchingPassiveCitation.isPresent()) {
                  if (updateOfMatchingCitationNeeded(
                      matchingPassiveCitation.get(), activeCitation)) {
                    log.atInfo()
                        .addKeyValue("publishedSli", sli.getDocumentNumber())
                        .addKeyValue("targetDocumentationUnit", targetDecision.getDocumentNumber())
                        .addKeyValue("activeCitation", activeCitation)
                        .addKeyValue("matchingPassiveCitation", matchingPassiveCitation.get())
                        .setMessage("Updating data of matching passive citation.")
                        .log();
                    matchingPassiveCitation.get().setSourceId(sli.getId());
                    matchingPassiveCitation.get().setSourceDocumentNumber(sli.getDocumentNumber());
                    matchingPassiveCitation.get().setSourceAuthor(sli.getAuthor());
                    matchingPassiveCitation.get().setSourceBookTitle(sli.getBookTitle());
                    matchingPassiveCitation
                        .get()
                        .setSourceYearOfPublication(sli.getYearOfPublication());

                    documentationUnitRepository.save(targetDecision);

                    documentsToRepublish.add(targetDecision.getId());
                  }
                } else {
                  targetDecision
                      .getPassiveSliCitations()
                      .add(
                          createMatchingPassiveCitation(
                              targetDecision,
                              sli,
                              targetDecision.getPassiveCaselawCitations().size() + 1));

                  log.atInfo()
                      .addKeyValue("publishedSli", sli.getDocumentNumber())
                      .addKeyValue("targetDocumentationUnit", targetDecision.getDocumentNumber())
                      .addKeyValue("activeCitation", activeCitation)
                      .setMessage("Creating passive citation for published active citation.")
                      .log();

                  documentationUnitRepository.save(targetDecision);

                  documentsToRepublish.add(targetDecision.getId());
                }
              }
            });

    return documentsToRepublish;
  }

  private PassiveCitationSliEntity createMatchingPassiveCitation(
      DecisionDTO target, SliDTO source, Integer rank) {
    return PassiveCitationSliEntity.builder()
        .target(target)
        .sourceId(source.getId())
        .sourceDocumentNumber(source.getDocumentNumber())
        .sourceAuthor(source.getAuthor())
        .sourceBookTitle(source.getBookTitle())
        .sourceYearOfPublication(source.getYearOfPublication())
        .rank(rank)
        .build();
  }

  private Optional<PassiveCitationSliEntity> findMatchingPassiveCitation(
      DecisionDTO decision, UUID sliUuid, String documentNumber) {
    return decision.getPassiveSliCitations().stream()
        .filter(
            passiveCitation -> {
              if (passiveCitation.getSourceId() != null) {
                return passiveCitation.getSourceId().equals(sliUuid);
              }

              if (passiveCitation.getSourceDocumentNumber() != null) {
                return passiveCitation.getSourceDocumentNumber().equals(documentNumber);
              }

              return false;
            })
        .findFirst();
  }

  private boolean updateOfMatchingCitationNeeded(
      PassiveCitationSliEntity passive, SliActiveCaselawReferenceDTO active) {
    if (active.getSource() == null) {
      return false;
    }

    if (!Objects.equals(passive.getSourceId(), active.getSource().getId())) {
      return true;
    }

    if (!Objects.equals(passive.getSourceAuthor(), active.getSource().getAuthor())) {
      return true;
    }

    if (!Objects.equals(passive.getSourceBookTitle(), active.getSource().getBookTitle())) {
      return true;
    }

    if (!Objects.equals(
        passive.getSourceYearOfPublication(), active.getSource().getYearOfPublication())) {
      return true;
    }

    return !Objects.equals(
        passive.getSourceDocumentNumber(), active.getSource().getDocumentNumber());
  }

  /** Case 3: Identify documents that point to revoked SLI documents. */
  @Transactional
  public void handleRevokedAfter(Instant after) {
    List<RevokedSli> revokedEntries = revokedSliRepository.findAllByRevokedAtAfter(after);

    if (revokedEntries.isEmpty()) {
      log.atInfo().addKeyValue("after", after).setMessage("No new revoked entries").log();
      return;
    }

    var documentsToRepublish =
        revokedEntries.stream()
            .map(this::removeCitationsToRevokedSli)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    documentsToRepublish.forEach(
        docId -> {
          try {
            portalPublicationService.publishDocumentationUnitWithChangelog(docId, null);
            log.atDebug()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .setMessage("Successfully republished after SLI revoked sync")
                .log();
          } catch (Exception e) {
            log.atDebug()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .addKeyValue("exception", e)
                .setMessage("Failed to republish during SLI revoked sync")
                .log();
          }
        });
  }

  private Set<UUID> removeCitationsToRevokedSli(RevokedSli revokedSli) {
    log.atInfo()
        .addKeyValue(LoggingKeys.REVOKED_SLI, revokedSli.getDocUnitId())
        .setMessage("Checking active and passive citations for references to revoked SLI.")
        .log();

    Set<UUID> documentsToRepublish = new HashSet<>();

    List<DecisionDTO> documentsWithPassive =
        documentationUnitRepository.findAllByPassiveSliSourceIdAndPendingRevocation(
            revokedSli.getDocUnitId());

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision
              .getPassiveSliCitations()
              .removeIf(
                  p ->
                      p.getSourceId() != null && revokedSli.getDocUnitId().equals(p.getSourceId()));

      if (removed) {
        documentationUnitRepository.save(decision);
        documentsToRepublish.add(decision.getId());
        log.atInfo()
            .addKeyValue(LoggingKeys.REVOKED_SLI, revokedSli.getDocUnitId())
            .addKeyValue("docunitWithPassiveCitation", decision.getDocumentNumber())
            .setMessage(
                "Passive Citation to Revoked SLI found and removed. Doc unit scheduled for republishing.")
            .log();
      } else {
        // TODO: (Malte Laukötter, 2026-03-02) this shouldn't happen so maybe we should log
        // something
      }
    }

    // active citations will be just republished. This removes the target id and document number
    // from the published data.
    List<DecisionDTO> affectedByActive =
        documentationUnitRepository.findAllByActiveSliTargetIdAndPendingRevocation(
            revokedSli.getDocUnitId());

    for (DecisionDTO decision : affectedByActive) {
      documentsToRepublish.add(decision.getId());
      log.atInfo()
          .addKeyValue(LoggingKeys.REVOKED_SLI, revokedSli.getDocUnitId())
          .addKeyValue("docunitWithActiveCitation", decision.getDocumentNumber())
          .setMessage("Active Citation to Revoked SLI found. Doc unit scheduled for republishing.")
          .log();
    }

    return documentsToRepublish;
  }
}
