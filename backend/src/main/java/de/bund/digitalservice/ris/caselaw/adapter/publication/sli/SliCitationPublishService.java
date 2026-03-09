package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliDTO;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SliCitationPublishService {

  private final DatabaseSliRepository sliRepository;

  public SliCitationPublishService(DatabaseSliRepository sliRepository) {
    this.sliRepository = sliRepository;
  }

  private Optional<SliDTO> getPassiveCitationSource(PassiveCitationSliEntity passiveCitation) {
    if (passiveCitation.getSourceId() == null) {
      return Optional.empty();
    }

    return sliRepository.findById(passiveCitation.getSourceId());
  }

  private Optional<SliDTO> getActiveCitationTarget(ActiveCitationSliEntity activeCitation) {
    if (activeCitation.getTargetId() == null) {
      return Optional.empty();
    }

    return sliRepository.findById(activeCitation.getTargetId());
  }

  /**
   * Update the passive citation with the information from the source. If the source can not be
   * found we only want to keep the passive citation if it is an actual blind-link (so has no source
   * document number).
   */
  @Transactional
  public Optional<PassiveCitationSliEntity> updatePassiveCitationSourceWithInformationFromSource(
      PassiveCitationSliEntity passiveCitation) {
    if (passiveCitation.getSourceId() == null) {
      return Optional.of(passiveCitation);
    }

    var source = getPassiveCitationSource(passiveCitation);
    if (source.isEmpty()) {
      log.atDebug()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER, passiveCitation.getSourceDocumentNumber())
          .addKeyValue("passiveCitationCaselawId", passiveCitation.getId())
          .setMessage(
              "Skipping publishing of a passive citation sli as the source document can not be found")
          .log();
      return Optional.empty();
    }

    // passiveCitation.setSourceDocumentNumber(source.get().getDocumentNumber());
    // passiveCitation.setSourceAuthor(source.get().getAuthor());
    // passiveCitation.setSourceBookTitle(source.get().getBookTitle());
    // passiveCitation.setSourceYearOfPublication(source.get().getYearOfPublication());
    if (!Objects.equals(passiveCitation.getSourceDocumentNumber(), source.get().getDocumentNumber())
        || !Objects.equals(passiveCitation.getSourceAuthor(), source.get().getAuthor())
        || !Objects.equals(passiveCitation.getSourceBookTitle(), source.get().getBookTitle())
        || !Objects.equals(
            passiveCitation.getSourceYearOfPublication(), source.get().getYearOfPublication())) {
      log.atInfo()
          .addKeyValue(LoggingKeys.SOURCE_DOCUMENT_NUMBER, source.get().getDocumentNumber())
          .addKeyValue(
              "passiveCitation.sourceDocumentNumber", passiveCitation.getSourceDocumentNumber())
          .addKeyValue("passiveCitation.sourceAuthor", passiveCitation.getSourceAuthor())
          .addKeyValue("passiveCitation.sourceBookTitle", passiveCitation.getSourceBookTitle())
          .addKeyValue(
              "passiveCitation.sourceYearOfPublication",
              passiveCitation.getSourceYearOfPublication())
          .addKeyValue("target.documentNumber", source.get().getDocumentNumber())
          .addKeyValue("target.author", source.get().getAuthor())
          .addKeyValue("target.bookTitle", source.get().getBookTitle())
          .addKeyValue("target.yearOfPublication", source.get().getYearOfPublication())
          .setMessage(
              "Metadata divergence detected between caselaw passive citation and source sli document.")
          .log();
    }

    return Optional.of(passiveCitation);
  }

  /** Update the citation target with the information from the actual target document. */
  @Transactional
  public ActiveCitationSliEntity updateActiveCitationTargetWithInformationFromTarget(
      ActiveCitationSliEntity activeCitation) {
    var target = getActiveCitationTarget(activeCitation);

    if (target.isEmpty()) {
      log.atInfo()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER, activeCitation.getSource().getDocumentNumber())
          .addKeyValue("missingTargetSliDocumentNumber", activeCitation.getTargetDocumentNumber())
          .setMessage("Unlinking active citation: target SLI document not found in database.")
          .log();
      activeCitation.setTargetId(null);
      activeCitation.setTargetDocumentNumber(null);
    } else {
      // Commented out for now as we first want to just log about the differences to understand them
      // better
      // activeCitation.setTargetDocumentNumber(target.get().getDocumentNumber());
      // activeCitation.setTargetAuthor(target.get().getAuthor());
      // activeCitation.setTargetBookTitle(target.get().getBookTitle());
      // activeCitation.setTargetYearOfPublication(target.get().getYearOfPublication());

      if (!Objects.equals(
              activeCitation.getTargetDocumentNumber(), target.get().getDocumentNumber())
          || !Objects.equals(activeCitation.getTargetAuthor(), target.get().getAuthor())
          || !Objects.equals(activeCitation.getTargetBookTitle(), target.get().getBookTitle())
          || !Objects.equals(
              activeCitation.getTargetYearOfPublication(), target.get().getYearOfPublication())) {
        log.atInfo()
            .addKeyValue(
                LoggingKeys.SOURCE_DOCUMENT_NUMBER, activeCitation.getSource().getDocumentNumber())
            .addKeyValue(
                "activeCitation.targetDocumentNumber", activeCitation.getTargetDocumentNumber())
            .addKeyValue("activeCitation.targetAuthor", activeCitation.getTargetAuthor())
            .addKeyValue("activeCitation.targetBookTitle", activeCitation.getTargetBookTitle())
            .addKeyValue(
                "activeCitation.targetYearOfPublication",
                activeCitation.getTargetYearOfPublication())
            .addKeyValue("target.documentNumber", target.get().getDocumentNumber())
            .addKeyValue("target.author", target.get().getAuthor())
            .addKeyValue("target.bookTitle", target.get().getBookTitle())
            .addKeyValue("target.yearOfPublication", target.get().getYearOfPublication())
            .setMessage(
                "Metadata divergence detected between caselaw active citation and target sli document.")
            .log();
      }
    }

    return activeCitation;
  }
}
