package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationAdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdmRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdmDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AdmCitationPublishService {

  private final DatabaseAdmRepository admRepository;

  public AdmCitationPublishService(DatabaseAdmRepository admRepository) {
    this.admRepository = admRepository;
  }

  private Optional<AdmDTO> getPassiveCitationSource(PassiveCitationAdmDTO passiveCitation) {
    if (passiveCitation.getSourceId() == null) {
      return Optional.empty();
    }

    return admRepository.findById(passiveCitation.getSourceId());
  }

  private Optional<AdmDTO> getActiveCitationTarget(ActiveCitationAdmDTO activeCitation) {
    if (activeCitation.getTargetDocumentNumber() == null) {
      return Optional.empty();
    }

    return admRepository.findById(activeCitation.getTargetId());
  }

  /**
   * Update the passive citation with the information from the source. If the source can not be
   * found we only want to keep the passive citation if it is an actual blind-link (so has no source
   * document number).
   */
  @Transactional
  public Optional<PassiveCitationAdmDTO> updatePassiveCitationSourceWithInformationFromSource(
      PassiveCitationAdmDTO passiveCitation) {
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
  public ActiveCitationAdmDTO updateActiveCitationTargetWithInformationFromTarget(
      ActiveCitationAdmDTO activeCitation) {
    var target = getActiveCitationTarget(activeCitation);

    if (target.isEmpty()) {
      activeCitation.setTargetId(null);
      activeCitation.setTargetDocumentNumber(null);
    } else {
      activeCitation.setTargetDocumentNumber(target.get().getDocumentNumber());
      activeCitation.setTargetDirective(target.get().getJurisAbbreviation());
    }

    return activeCitation;
  }
}
