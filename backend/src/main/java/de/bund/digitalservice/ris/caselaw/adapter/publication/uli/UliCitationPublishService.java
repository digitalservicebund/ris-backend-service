package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UliCitationPublishService {

  private final DatabaseUliRepository databaseUliRepository;

  public UliCitationPublishService(DatabaseUliRepository databaseUliRepository) {
    this.databaseUliRepository = databaseUliRepository;
  }

  /** Case 1: Validation and enrichment for ULI Passive Citations (Uli -> Caselaw) */
  @Transactional
  public Optional<PassiveCitationUliDTO> updatePassiveUliCitationWithInformationFromSource(
      PassiveCitationUliDTO passiveCitation) {

    if (passiveCitation.getSourceId() == null) {
      return Optional.of(passiveCitation);
    }

    var uliRefOptional = databaseUliRepository.findById(passiveCitation.getSourceId());

    if (uliRefOptional.isEmpty()) {
      log.atDebug()
          .addKeyValue("sourceDocumentNumber", passiveCitation.getSourceLiteratureDocumentNumber())
          .addKeyValue("passiveCitationCaselawId", passiveCitation.getId())
          .setMessage(
              "Skipping publishing of a passive citation uli as the source document can not be found")
          .log();
      return Optional.empty();
    }

    var uliRef = uliRefOptional.get();

    passiveCitation.setSourceCitation(uliRef.getCitation());
    passiveCitation.setSourceLiteratureDocumentNumber(uliRef.getDocumentNumber());
    passiveCitation.setSourceAuthor(uliRef.getAuthor());
    passiveCitation.setSourceDocumentTypeRawValue(uliRef.getDocumentTypeRawValue());
    passiveCitation.setSourceLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());

    return Optional.of(passiveCitation);
  }

  /** Case 1: Validation and enrichment for ULI Active Citations (Caselaw -> Uli) */
  @Transactional
  public ActiveCitationUliDTO updateActiveUliCitationWithInformationFromTarget(
      ActiveCitationUliDTO activeCitation) {

    if (activeCitation.getTargetLiteratureDocumentNumber() == null) {
      return activeCitation;
    }
    var uliOptional =
        databaseUliRepository.findByDocumentNumber(
            activeCitation.getTargetLiteratureDocumentNumber());
    if (uliOptional.isPresent()) {
      // for now, we only log the metadata divergence
      // var uliRef = uliOptional.get();
      // activeCitation.setTargetCitation(uliRef.getCitation());
      // activeCitation.setTargetAuthor(uliRef.getAuthor());
      // activeCitation.setTargetLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());
      checkAndLogMetadataDivergence(activeCitation, uliOptional.get());
    } else {
      activeCitation.setTargetLiteratureDocumentNumber(null);
    }

    return activeCitation;
  }

  private void checkAndLogMetadataDivergence(ActiveCitationUliDTO active, UliDTO target) {
    if (!Objects.equals(active.getTargetCitation(), target.getCitation())
        || !Objects.equals(active.getTargetAuthor(), target.getAuthor())
        || !Objects.equals(
            active.getTargetLegalPeriodicalRawValue(), target.getLegalPeriodicalRawValue())) {

      log.atWarn()
          .addKeyValue("sourceDocNumber", active.getSource().getDocumentNumber())
          .addKeyValue("targetUliNumber", target.getDocumentNumber())
          .addKeyValue("localAuthor", active.getTargetAuthor())
          .addKeyValue("targetAuthor", target.getAuthor())
          .addKeyValue("localCitation", active.getTargetCitation())
          .addKeyValue("targetCitation", target.getCitation())
          .addKeyValue("localPeriodical", active.getTargetLegalPeriodicalRawValue())
          .addKeyValue("targetPeriodical", target.getLegalPeriodicalRawValue())
          .setMessage(
              "Metadata divergence detected between caselaw active citation and target uli document.")
          .log();
    }
  }
}
