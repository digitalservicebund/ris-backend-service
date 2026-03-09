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

    Optional<UliDTO> uliOptional = Optional.empty();
    if (passiveCitation.getSourceId() != null) {
      uliOptional = databaseUliRepository.findById(passiveCitation.getSourceId());
    }

    if (uliOptional.isEmpty() && passiveCitation.getSourceLiteratureDocumentNumber() != null) {
      uliOptional =
          databaseUliRepository.findByDocumentNumber(
              passiveCitation.getSourceLiteratureDocumentNumber());
    }
    if (uliOptional.isPresent()) {

      // for now, we only log the metadata divergence, later we enrich the passive citation
      var target = uliOptional.get();
      //      passiveCitation.setSourceId(target.getId());
      //      passiveCitation.setSourceLiteratureDocumentNumber(target.getDocumentNumber());
      //      passiveCitation.setSourceAuthor(target.getAuthor());
      //      passiveCitation.setSourceCitation(target.getCitation());
      //      passiveCitation.setSourceDocumentTypeRawValue(target.getDocumentTypeRawValue());
      //      passiveCitation.setSourceLegalPeriodicalRawValue(target.getLegalPeriodicalRawValue());

      //        log.atInfo()
      //                .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, target.getDocumentNumber())
      //                .addKeyValue("passiveCitationId", passiveCitation.getId())
      //                .setMessage("Enriched passive citation with metadata from ULI source
      // document.")
      //                .log();

      if (!Objects.equals(passiveCitation.getSourceCitation(), target.getCitation())
          || !Objects.equals(passiveCitation.getSourceCitation(), target.getAuthor())
          || !Objects.equals(
              passiveCitation.getSourceLegalPeriodicalRawValue(),
              target.getLegalPeriodicalRawValue())) {

        log.atInfo()
            .addKeyValue(
                LoggingKeys.SOURCE_DOCUMENT_NUMBER, passiveCitation.getTarget().getDocumentNumber())
            .addKeyValue(
                "activeCitation.sourceLiteratureDocumentNumber",
                passiveCitation.getSourceLiteratureDocumentNumber())
            .addKeyValue("target.documentNumber", target.getDocumentNumber())
            .addKeyValue("activeCitation.sourceAuthor", passiveCitation.getSourceAuthor())
            .addKeyValue("target.author", target.getAuthor())
            .addKeyValue("passiveCitation.sourceCitation", passiveCitation.getSourceCitation())
            .addKeyValue("target.citation", target.getCitation())
            .addKeyValue(
                "passiveCitation.sourceLegalPeriodicalRawValue",
                passiveCitation.getSourceLegalPeriodicalRawValue())
            .addKeyValue("target.legalPeriodicalRawValue", target.getLegalPeriodicalRawValue())
            .setMessage(
                "Metadata divergence detected between caselaw passive citation and source uli document.")
            .log();
      }

    } else {
      log.atWarn()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER,
              passiveCitation.getSourceLiteratureDocumentNumber())
          .addKeyValue("sourceId", passiveCitation.getSourceId())
          .addKeyValue(
              "missingSourceUliDocumentNumber", passiveCitation.getSourceLiteratureDocumentNumber())
          .setMessage("Unlinking passive citation: source ULI document not found in database.")
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
      // for now, we only log the metadata divergence, later we enrich the active citation
      var target = uliOptional.get();
      // activeCitation.setTargetCitation(target.getCitation());
      // activeCitation.setTargetAuthor(target.getAuthor());
      // activeCitation.setTargetLegalPeriodicalRawValue(target.getLegalPeriodicalRawValue());

      if (!Objects.equals(activeCitation.getTargetCitation(), target.getCitation())
          || !Objects.equals(activeCitation.getTargetAuthor(), target.getAuthor())
          || !Objects.equals(
              activeCitation.getTargetLegalPeriodicalRawValue(),
              target.getLegalPeriodicalRawValue())) {

        log.atInfo()
            .addKeyValue(
                LoggingKeys.SOURCE_DOCUMENT_NUMBER, activeCitation.getSource().getDocumentNumber())
            .addKeyValue(
                "activeCitation.targetLiteratureDocumentNumber",
                activeCitation.getTargetLiteratureDocumentNumber())
            .addKeyValue("target.documentNumber", target.getDocumentNumber())
            .addKeyValue("activeCitation.targetAuthor", activeCitation.getTargetAuthor())
            .addKeyValue("target.author", target.getAuthor())
            .addKeyValue("activeCitation.targetCitation", activeCitation.getTargetCitation())
            .addKeyValue("target.citation", target.getCitation())
            .addKeyValue(
                "activeCitation.targetLegalPeriodicalRawValue",
                activeCitation.getTargetLegalPeriodicalRawValue())
            .addKeyValue("target.legalPeriodicalRawValue", target.getLegalPeriodicalRawValue())
            .setMessage(
                "Metadata divergence detected between caselaw active citation and target uli document.")
            .log();
      }

    } else {
      log.atInfo()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER, activeCitation.getSource().getDocumentNumber())
          .addKeyValue(
              "missingTargetUliDocumentNumber", activeCitation.getTargetLiteratureDocumentNumber())
          .setMessage("Unlinking active citation: target ULI document not found in database.")
          .log();
      activeCitation.setTargetId(null);
      activeCitation.setTargetLiteratureDocumentNumber(null);
    }

    return activeCitation;
  }
}
