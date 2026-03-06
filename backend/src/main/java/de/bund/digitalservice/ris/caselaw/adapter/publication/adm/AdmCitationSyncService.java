package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdmRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdm;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdmRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class AdmCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseAdmRepository admRepository;
  private final DatabaseCitationTypeRepository citationTypeRepository;
  private final RevokedAdmRepository revokedAdmRepository;
  private final PortalPublicationService portalPublicationService;
  private final TransactionTemplate transactionTemplate;

  public AdmCitationSyncService(
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseAdmRepository admRepository,
      DatabaseCitationTypeRepository citationTypeRepository,
      RevokedAdmRepository revokedAdmRepository,
      PortalPublicationService portalPublicationService,
      TransactionTemplate transactionTemplate) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.admRepository = admRepository;
    this.citationTypeRepository = citationTypeRepository;
    this.revokedAdmRepository = revokedAdmRepository;
    this.portalPublicationService = portalPublicationService;
    this.transactionTemplate = transactionTemplate;
  }

  public void handleNewlyPublishedAfter(Instant after) {
    var documentsToPublish =
        transactionTemplate.execute(
            (status) ->
                admRepository.findAllByPublishedAtAfter(after).stream()
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
                .setMessage("Successfully republished after ADM newly published sync")
                .log();
          } catch (Exception e) {
            log.atError()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .addKeyValue("exception", e)
                .setMessage("Failed to republish during ADM newly published sync")
                .log();
          }
        });
  }

  /**
   * Create passive citations for all active citations of the doc unit.
   *
   * @return list UUIDs of all other documents that have been changed. These should be published
   *     again.
   */
  private Set<UUID> syncCitations(AdmDTO adm) {
    Set<UUID> documentsToRepublish = new HashSet<>();

    adm.getActiveCaselawReferences()
        .forEach(
            activeCitation -> {
              var target =
                  documentationUnitRepository.findById(
                      activeCitation.getTargetDocumentationUnitId());

              if (target.isPresent() && target.get() instanceof DecisionDTO targetDecision) {
                var matchingPassiveCitation =
                    findMatchingPassiveCitation(
                        targetDecision,
                        adm.getId(),
                        adm.getDocumentNumber(),
                        activeCitation.getCitationType());
                if (matchingPassiveCitation.isPresent()) {
                  if (updateOfMatchingCitationNeeded(
                      matchingPassiveCitation.get(), activeCitation)) {
                    log.atInfo()
                        .addKeyValue("publishedAdm", adm.getDocumentNumber())
                        .addKeyValue("targetDocumentationUnit", targetDecision.getDocumentNumber())
                        .addKeyValue("activeCitation", activeCitation)
                        .addKeyValue("matchingPassiveCitation", matchingPassiveCitation.get())
                        .setMessage("Updating data of matching passive citation.")
                        .log();
                    matchingPassiveCitation.get().setSourceId(adm.getId());
                    matchingPassiveCitation.get().setSourceDocumentNumber(adm.getDocumentNumber());
                    matchingPassiveCitation.get().setSourceDirective(adm.getJurisAbbreviation());

                    documentationUnitRepository.save(targetDecision);

                    documentsToRepublish.add(targetDecision.getId());
                  }
                } else {
                  targetDecision
                      .getPassiveAdmCitations()
                      .add(
                          createMatchingPassiveCitation(
                              activeCitation,
                              targetDecision,
                              adm,
                              targetDecision.getPassiveCaselawCitations().size() + 1));

                  log.atInfo()
                      .addKeyValue("publishedAdm", adm.getDocumentNumber())
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

  private PassiveCitationAdmDTO createMatchingPassiveCitation(
      AdmActiveCaselawReferenceDTO activeCitation,
      DecisionDTO target,
      AdmDTO source,
      Integer rank) {
    return PassiveCitationAdmDTO.builder()
        .target(target)
        .sourceId(source.getId())
        .sourceDocumentNumber(source.getDocumentNumber())
        .sourceDirective(source.getJurisAbbreviation())
        .citationTypeRaw(activeCitation.getCitationType())
        .citationType(
            citationTypeRepository
                .findByAbbreviation(activeCitation.getCitationType())
                .orElse(null))
        // do not add legal periodical and citation as adm does not get them at the moment
        .rank(rank)
        .build();
  }

  private Optional<PassiveCitationAdmDTO> findMatchingPassiveCitation(
      DecisionDTO decision, UUID admUuid, String documentNumber, String citationType) {
    return decision.getPassiveAdmCitations().stream()
        .filter(
            passiveCitation -> {
              if (passiveCitation.getSourceId() != null) {
                return passiveCitation.getSourceId().equals(admUuid)
                    && Objects.equals(
                        Optional.ofNullable(passiveCitation.getCitationType())
                            .map(CitationTypeDTO::getAbbreviation),
                        Optional.ofNullable(citationType));
              }

              if (passiveCitation.getSourceDocumentNumber() != null) {
                return passiveCitation.getSourceDocumentNumber().equals(documentNumber)
                    && Objects.equals(
                        Optional.ofNullable(passiveCitation.getCitationType())
                            .map(CitationTypeDTO::getAbbreviation),
                        Optional.ofNullable(citationType));
              }

              return false;
            })
        .findFirst();
  }

  private boolean updateOfMatchingCitationNeeded(
      PassiveCitationAdmDTO passive, AdmActiveCaselawReferenceDTO active) {
    if (active.getSource() == null) {
      return false;
    }

    if (!Objects.equals(passive.getSourceId(), active.getSource().getId())) {
      return true;
    }

    if (!Objects.equals(passive.getSourceDirective(), active.getSource().getJurisAbbreviation())) {
      return true;
    }

    return !Objects.equals(
        passive.getSourceDocumentNumber(), active.getSource().getDocumentNumber());
  }

  /** Case 3: Identify documents that point to revoked ADM documents. */
  public void handleRevokedAfter(Instant after) {
    List<RevokedAdm> revokedEntries = revokedAdmRepository.findAllByRevokedAtAfter(after);

    if (revokedEntries.isEmpty()) {
      log.atInfo().addKeyValue("after", after).setMessage("No new revoked entries").log();
      return;
    }

    var documentsToRepublish =
        transactionTemplate.execute(
            (status) ->
                revokedEntries.stream()
                    .map(this::removeCitationsToRevokedAdministrativeDirective)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet()));

    documentsToRepublish.forEach(
        docId -> {
          try {
            portalPublicationService.publishDocumentationUnitWithChangelog(docId, null);
            log.atInfo()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .setMessage("Successfully republished after ADM revoked sync")
                .log();
          } catch (Exception e) {
            log.atError()
                .addKeyValue(LoggingKeys.DOCUMENT_ID, docId)
                .addKeyValue("exception", e)
                .setMessage("Failed to republish during ADM revoked sync")
                .log();
          }
        });
  }

  private Set<UUID> removeCitationsToRevokedAdministrativeDirective(RevokedAdm revokedAdm) {
    log.atInfo()
        .addKeyValue(LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE, revokedAdm.getDocUnitId())
        .setMessage(
            "Checking active and passive citations for references to revoked Administrative Directive.")
        .log();

    Set<UUID> documentsToRepublish = new HashSet<>();

    List<DecisionDTO> documentsWithPassive =
        documentationUnitRepository.findAllByPassiveAdmSourceIdAndPendingRevocation(
            revokedAdm.getDocUnitId());

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision
              .getPassiveAdmCitations()
              .removeIf(
                  p ->
                      p.getSourceId() != null && revokedAdm.getDocUnitId().equals(p.getSourceId()));

      if (removed) {
        documentationUnitRepository.save(decision);
        documentsToRepublish.add(decision.getId());
        log.atInfo()
            .addKeyValue(LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE, revokedAdm.getDocUnitId())
            .addKeyValue("docunitWithPassiveCitation", decision.getDocumentNumber())
            .setMessage(
                "Passive Citation to Revoked Administrative Directive found and removed. Doc unit scheduled for republishing.")
            .log();
      } else {
        // TODO: (Malte Laukötter, 2026-03-02) this shouldn't happen so maybe we should log
        // something
      }
    }

    // active citations will be just republished. This removes the target id and document number
    // from the published data.
    List<DecisionDTO> affectedByActive =
        documentationUnitRepository.findAllByActiveAdmTargetIdAndPendingRevocation(
            revokedAdm.getDocUnitId());

    for (DecisionDTO decision : affectedByActive) {
      documentsToRepublish.add(decision.getId());
      log.atInfo()
          .addKeyValue(LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE, revokedAdm.getDocUnitId())
          .addKeyValue("docunitWithActiveCitation", decision.getDocumentNumber())
          .setMessage(
              "Active Citation to Revoked Administrative Directive found. Doc unit scheduled for republishing.")
          .log();
    }

    return documentsToRepublish;
  }
}
