package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationAdministrativeRegulationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdministrativeRegulationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdministrativeRegulationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdministrativeRegultationDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AdministrativeRegulationCitationPublishService {

  private final DatabaseAdministrativeRegulationRepository administrativeRegulationRepository;

  public AdministrativeRegulationCitationPublishService(
      DatabaseAdministrativeRegulationRepository administrativeRegulationRepository) {
    this.administrativeRegulationRepository = administrativeRegulationRepository;
  }

  private Optional<AdministrativeRegulationDTO> getPassiveCitationSource(
      PassiveCitationAdministrativeRegultationDTO passiveCitation) {
    if (passiveCitation.getSourceId() == null) {
      return Optional.empty();
    }

    return administrativeRegulationRepository.findById(passiveCitation.getSourceId());
  }

  private Optional<AdministrativeRegulationDTO> getActiveCitationTarget(
      ActiveCitationAdministrativeRegulationDTO activeCitation) {
    if (activeCitation.getTargetDocumentNumber() == null) {
      return Optional.empty();
    }

    return administrativeRegulationRepository.findById(activeCitation.getTargetId());
  }

  /**
   * Update the passive citation with the information from the source. If the source can not be
   * found we only want to keep the passive citation if it is an actual blind-link (so has no source
   * document number).
   */
  @Transactional
  public Optional<PassiveCitationAdministrativeRegultationDTO>
      updatePassiveCitationSourceWithInformationFromSource(
          PassiveCitationAdministrativeRegultationDTO passiveCitation) {
    if (passiveCitation.getSourceId() == null) {
      return Optional.of(passiveCitation);
    }

    var source = getPassiveCitationSource(passiveCitation);
    if (source.isEmpty()) {
      log.atDebug()
          .addKeyValue("sourceDocumentNumber", passiveCitation.getSourceDocumentNumber())
          .addKeyValue("passiveCitationCaselawId", passiveCitation.getId())
          .setMessage(
              "Skipping publishing of a passive citation adm as the source document can not be found")
          .log();
      return Optional.empty();
    }

    passiveCitation.setSourceDocumentNumber(source.get().getDocumentNumber());
    passiveCitation.setSourceDirective(source.get().getJurisAbbreviation());

    return Optional.of(passiveCitation);
  }

  /** Update the citation target with the information from the actual target document. */
  @Transactional
  public ActiveCitationAdministrativeRegulationDTO
      updateActiveCitationTargetWithInformationFromTarget(
          ActiveCitationAdministrativeRegulationDTO activeCitation) {
    var target = getActiveCitationTarget(activeCitation);

    if (target.isEmpty()) {
      activeCitation.setTargetDocumentNumber(null);
    } else {
      activeCitation.setTargetDocumentNumber(target.get().getDocumentNumber());
      activeCitation.setTargetDirective(target.get().getJurisAbbreviation());
    }

    return activeCitation;
  }
}
