package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliDTO;
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
    if (activeCitation.getTargetDocumentNumber() == null) {
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
          .addKeyValue("sourceDocumentNumber", passiveCitation.getSourceDocumentNumber())
          .addKeyValue("passiveCitationCaselawId", passiveCitation.getId())
          .setMessage(
              "Skipping publishing of a passive citation sli as the source document can not be found")
          .log();
      return Optional.empty();
    }

    passiveCitation.setSourceDocumentNumber(source.get().getDocumentNumber());
    passiveCitation.setSourceAuthor(source.get().getAuthor());
    passiveCitation.setSourceBookTitle(source.get().getBookTitle());
    passiveCitation.setSourceYearOfPublication(source.get().getYearOfPublication());

    return Optional.of(passiveCitation);
  }

  /** Update the citation target with the information from the actual target document. */
  @Transactional
  public ActiveCitationSliEntity updateActiveCitationTargetWithInformationFromTarget(
      ActiveCitationSliEntity activeCitation) {
    var target = getActiveCitationTarget(activeCitation);

    if (target.isEmpty()) {
      activeCitation.setTargetId(null);
      activeCitation.setTargetDocumentNumber(null);
    } else {
      activeCitation.setTargetDocumentNumber(target.get().getDocumentNumber());
      activeCitation.setTargetAuthor(target.get().getAuthor());
      activeCitation.setTargetBookTitle(target.get().getBookTitle());
      activeCitation.setTargetYearOfPublication(target.get().getYearOfPublication());
    }

    return activeCitation;
  }
}
