package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdministrativeRegulationActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdministrativeRegulationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdministrativeRegultationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdministrativeDirective;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdministrativeDirectiveRepository;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AdministrativeRegulationCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseCitationTypeRepository citationTypeRepository;
  private final JobSyncStatusRepository jobSyncStatusRepository;
  private final RevokedAdministrativeDirectiveRepository revokedAdministrativeDirectiveRepository;

  public AdministrativeRegulationCitationSyncService(
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseCitationTypeRepository citationTypeRepository,
      RevokedAdministrativeDirectiveRepository revokedAdministrativeDirectiveRepository,
      JobSyncStatusRepository jobSyncStatusRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.citationTypeRepository = citationTypeRepository;
    this.revokedAdministrativeDirectiveRepository = revokedAdministrativeDirectiveRepository;
    this.jobSyncStatusRepository = jobSyncStatusRepository;
  }

  /**
   * Create passive citations for all active citations of the doc unit.
   *
   * @return list Document numbers of all other documents that have been changed. These should be
   *     published again.
   */
  @Transactional
  public Set<String> syncCitations(AdministrativeRegulationDTO adm) {
    Set<String> documentsToRepublish = new HashSet<>();

    adm.getActiveCaselawReferences()
        .forEach(
            activeCitation -> {
              var target =
                  documentationUnitRepository.findById(
                      activeCitation.getTargetDocumentationUnitId());

              if (target.isPresent() && target.get() instanceof DecisionDTO targetDecision) {
                var matchingPassiveCitation =
                    findMatchingPassiveCitation(
                        targetDecision, adm.getDocumentNumber(), activeCitation.getCitationType());
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
                    matchingPassiveCitation.get().setSourceDocumentNumber(adm.getDocumentNumber());
                    matchingPassiveCitation.get().setSourceDirective(adm.getJurisAbbreviation());

                    documentationUnitRepository.save(targetDecision);

                    documentsToRepublish.add(targetDecision.getDocumentNumber());
                  }
                } else {
                  targetDecision
                      .getPassiveAdministrativeRegulationCitations()
                      .add(
                          createMatchingPassiveCitation(
                              activeCitation,
                              targetDecision,
                              adm,
                              targetDecision.getPassiveCaselawCitations().size()));

                  log.atInfo()
                      .addKeyValue("publishedAdm", adm.getDocumentNumber())
                      .addKeyValue("targetDocumentationUnit", targetDecision.getDocumentNumber())
                      .addKeyValue("activeCitation", activeCitation)
                      .setMessage("Creating passive citation for published active citation.")
                      .log();

                  documentationUnitRepository.save(targetDecision);

                  documentsToRepublish.add(targetDecision.getDocumentNumber());
                }
              }
            });

    return documentsToRepublish;
  }

  private PassiveCitationAdministrativeRegultationDTO createMatchingPassiveCitation(
      AdministrativeRegulationActiveCaselawReferenceDTO activeCitation,
      DecisionDTO target,
      AdministrativeRegulationDTO source,
      Integer rank) {
    return PassiveCitationAdministrativeRegultationDTO.builder()
        .target(target)
        .sourceId(source.getId())
        .sourceDocumentNumber(source.getDocumentNumber())
        .sourceDirective(source.getDocumentNumber())
        .citationTypeRaw(activeCitation.getCitationType())
        .citationType(
            citationTypeRepository
                .findByAbbreviation(activeCitation.getCitationType())
                .orElse(null))
        // do not add legal periodical and citation as adm does not get them at the moment
        .rank(rank)
        .build();
  }

  private Optional<PassiveCitationAdministrativeRegultationDTO> findMatchingPassiveCitation(
      DecisionDTO decision, String documentNumber, String citationType) {
    return decision.getPassiveAdministrativeRegulationCitations().stream()
        .filter(
            passiveCitation -> {
              if (passiveCitation.getSourceId() == null) {
                return false;
              }

              return passiveCitation.getSourceDocumentNumber().equals(documentNumber)
                  && Objects.equals(passiveCitation.getCitationTypeRaw(), citationType);
            })
        .findFirst();
  }

  private boolean updateOfMatchingCitationNeeded(
      PassiveCitationAdministrativeRegultationDTO passive,
      AdministrativeRegulationActiveCaselawReferenceDTO active) {
    if (active.getSource() == null) {
      return false;
    }

    if (!Objects.equals(passive.getCitationType().getLabel(), active.getCitationType())) {
      return true;
    }

    if (!Objects.equals(passive.getSourceDirective(), active.getSource().getJurisAbbreviation())) {
      return true;
    }

    return !Objects.equals(
        passive.getSourceDocumentNumber(), active.getSource().getDocumentNumber());
  }

  /** Case 3: Identify documents that point to revoked ADM documents. */
  @Transactional
  public Set<String> handleRevoked() {
    String jobName = "ADM_REVOKED_SYNC";

    Instant lastRun =
        jobSyncStatusRepository
            .findById(jobName)
            .map(JobSyncStatus::getLastRun)
            .orElse(Instant.EPOCH);

    List<RevokedAdministrativeDirective> revokedEntries =
        revokedAdministrativeDirectiveRepository.findAllByRevokedAtAfter(lastRun);

    if (revokedEntries.isEmpty()) {
      log.atInfo().addKeyValue("lastRun", lastRun).setMessage("No new revoked entries").log();
      return Set.of();
    }

    var documentsToRepublish =
        revokedEntries.stream()
            .map(this::removeCitationsToRevokedAdministrativeDirective)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    // get the highest timestamp of revoked entries and not Instant.now() because in
    // the meantime a new entry could have been saved to the revoked table
    Instant newestRevokedAt =
        revokedEntries.stream()
            .map(RevokedAdministrativeDirective::getRevokedAt)
            .max(Comparator.naturalOrder())
            .orElse(lastRun);

    updateJobStatus(jobName, newestRevokedAt);

    return documentsToRepublish;
  }

  private Set<String> removeCitationsToRevokedAdministrativeDirective(
      RevokedAdministrativeDirective revokedAdministrativeDirective) {
    log.atInfo()
        .addKeyValue(
            LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE,
            revokedAdministrativeDirective.getDocUnitId())
        .setMessage(
            "Checking active and passive citations for references to revoked Administrative Directive.")
        .log();

    Set<String> documentsToRepublish = new HashSet<>();

    List<DecisionDTO> documentsWithPassive =
        documentationUnitRepository
            .findAllByPassiveAdministrativeRegulationSourceIdAndPendingRevocation(
                revokedAdministrativeDirective.getDocUnitId());

    for (DecisionDTO decision : documentsWithPassive) {
      boolean removed =
          decision
              .getPassiveUliCitations()
              .removeIf(
                  p ->
                      p.getSourceId() != null
                          && revokedAdministrativeDirective.getDocUnitId().equals(p.getSourceId()));

      if (removed) {
        documentationUnitRepository.save(decision);
        documentsToRepublish.add(decision.getDocumentNumber());
        log.atInfo()
            .addKeyValue(
                LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE,
                revokedAdministrativeDirective.getDocUnitId())
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
        documentationUnitRepository
            .findAllByActiveAdministrativeRegulationTargetIdAndPendingRevocation(
                revokedAdministrativeDirective.getDocUnitId());

    for (DecisionDTO decision : affectedByActive) {
      documentsToRepublish.add(decision.getDocumentNumber());
      log.atInfo()
          .addKeyValue(
              LoggingKeys.REVOKED_ADMINISTRATIVE_DIRECTIVE,
              revokedAdministrativeDirective.getDocUnitId())
          .addKeyValue("docunitWithActiveCitation", decision.getDocumentNumber())
          .setMessage(
              "Active Citation to Revoked Administrative Directive found. Doc unit scheduled for republishing.")
          .log();
    }

    return documentsToRepublish;
  }

  private void updateJobStatus(String name, Instant time) {
    JobSyncStatus status =
        jobSyncStatusRepository.findById(name).orElse(new JobSyncStatus(name, time));
    status.setLastRun(time);
    jobSyncStatusRepository.save(status);
  }
}
