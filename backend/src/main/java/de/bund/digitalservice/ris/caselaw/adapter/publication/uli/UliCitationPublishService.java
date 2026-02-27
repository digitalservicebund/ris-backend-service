package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUliRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UliCitationPublishService {

  private final PublishedUliRepository publishedUliRepository;

  public UliCitationPublishService(PublishedUliRepository publishedUliRepository) {
    this.publishedUliRepository = publishedUliRepository;
  }

  /** Case 1: Validation and enrichment for ULI Passive Citations (Uli -> Caselaw) */
  @Transactional
  public Optional<PassiveCitationUliDTO> updatePassiveUliCitationWithInformationFromSource(
      PassiveCitationUliDTO passiveCitation) {

    if (passiveCitation.getSourceLiteratureDocumentNumber() == null) {
      return Optional.of(passiveCitation);
    }

    var uliRefOptional =
        publishedUliRepository.findByDocumentNumber(
            passiveCitation.getSourceLiteratureDocumentNumber());

    if (uliRefOptional.isEmpty()) {
      return Optional.empty();
    }

    var uliRef = uliRefOptional.get();

    passiveCitation.setSourceCitation(uliRef.getCitation());
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
    var uliRefOptional =
        publishedUliRepository.findByDocumentNumber(
            activeCitation.getTargetLiteratureDocumentNumber());
    if (uliRefOptional.isPresent()) {
      var uliRef = uliRefOptional.get(); // load the lazy properties of the target
      activeCitation.setTargetCitation(uliRef.getCitation());
      activeCitation.setTargetAuthor(uliRef.getAuthor());
      activeCitation.setTargetLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());
    } else {
      activeCitation.setTargetLiteratureDocumentNumber(null);
    }

    return activeCitation;
  }
}
