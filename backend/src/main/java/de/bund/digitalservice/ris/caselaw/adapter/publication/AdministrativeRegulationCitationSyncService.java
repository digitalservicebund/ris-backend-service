package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdministrativeRegulationActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdministrativeRegulationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdministrativeRegulationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdministrativeRegultationDTO;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AdministrativeRegulationCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseCitationTypeRepository citationTypeRepository;

  public AdministrativeRegulationCitationSyncService(
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseAdministrativeRegulationRepository administrativeRegulationRepository,
      DatabaseCitationTypeRepository citationTypeRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.citationTypeRepository = citationTypeRepository;
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
                  // TODO: (Malte Laukötter, 2026-02-27) add a propert matching method
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
}
