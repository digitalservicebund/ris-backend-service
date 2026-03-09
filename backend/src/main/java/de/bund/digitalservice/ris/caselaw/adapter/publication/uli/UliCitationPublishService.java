package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
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

    Optional<UliDTO> uliRefOptional = Optional.empty();
    if (passiveCitation.getSourceId() != null) {
      uliRefOptional = databaseUliRepository.findById(passiveCitation.getSourceId());
    }

    if (uliRefOptional.isEmpty() && passiveCitation.getSourceLiteratureDocumentNumber() != null) {
      uliRefOptional =
          databaseUliRepository.findByDocumentNumber(
              passiveCitation.getSourceLiteratureDocumentNumber());
    }
    if (uliRefOptional.isPresent()) {
      var uliRef = uliRefOptional.get();

      // Enrichment
      passiveCitation.setSourceId(uliRef.getId());
      passiveCitation.setSourceLiteratureDocumentNumber(uliRef.getDocumentNumber());
      passiveCitation.setSourceAuthor(uliRef.getAuthor());
      passiveCitation.setSourceCitation(uliRef.getCitation());
      passiveCitation.setSourceDocumentTypeRawValue(uliRef.getDocumentTypeRawValue());
      passiveCitation.setSourceLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());

      log.atInfo()
          .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, uliRef.getDocumentNumber())
          .addKeyValue("passiveCitationId", passiveCitation.getId())
          .setMessage("Enriched passive citation with metadata from ULI source document.")
          .log();
    } else {
      log.atWarn()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER,
              passiveCitation.getSourceLiteratureDocumentNumber())
          .addKeyValue("sourceId", passiveCitation.getSourceId())
          .addKeyValue(
              "missingSourceUliDocumentNumber", passiveCitation.getSourceLiteratureDocumentNumber())
          .setMessage("Unlinking passive citation: target ULI document not found in database.")
          .log();

      passiveCitation.setSourceId(null);
      passiveCitation.setSourceLiteratureDocumentNumber(null);
    }
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
      log.atInfo()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER, activeCitation.getSource().getDocumentNumber())
          .addKeyValue(
              "missingTargetUliDocumentNumber", activeCitation.getTargetLiteratureDocumentNumber())
          .setMessage("Unlinking active citation: target ULI document not found in database.")
          .log();
      activeCitation.setTargetLiteratureDocumentNumber(null);
    }

    return activeCitation;
  }

  private void checkAndLogMetadataDivergence(ActiveCitationUliDTO active, UliDTO target) {
    if (!Objects.equals(active.getTargetCitation(), target.getCitation())
        || !Objects.equals(active.getTargetAuthor(), target.getAuthor())
        || !Objects.equals(
            active.getTargetLegalPeriodicalRawValue(), target.getLegalPeriodicalRawValue())) {

      log.atInfo()
          .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, active.getSource().getDocumentNumber())
          .addKeyValue(
              "activeCitation.targetLiteratureDocumentNumber",
              active.getTargetLiteratureDocumentNumber())
          .addKeyValue("target.documentNumber", target.getDocumentNumber())
          .addKeyValue("activeCitation.targetAuthor", active.getTargetAuthor())
          .addKeyValue("target.author", target.getAuthor())
          .addKeyValue("activeCitation.targetCitation", active.getTargetCitation())
          .addKeyValue("target.citation", target.getCitation())
          .addKeyValue(
              "activeCitation.targetLegalPeriodicalRawValue",
              active.getTargetLegalPeriodicalRawValue())
          .addKeyValue("target.legalPeriodicalRawValue", target.getLegalPeriodicalRawValue())
          .setMessage(
              "Metadata divergence detected between caselaw active citation and target uli document.")
          .log();
    }
  }
}
