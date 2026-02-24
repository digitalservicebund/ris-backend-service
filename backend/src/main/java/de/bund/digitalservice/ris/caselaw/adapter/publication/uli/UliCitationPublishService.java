package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
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
  @Transactional(readOnly = true)
  public Optional<PassiveCitationUliDTO> updatePassiveUliCitationWithInformationFromSource(
      PassiveCitationUliDTO passiveCitation) {

    if (passiveCitation.getSourceLiteratureDocumentNumber() == null) {
      return Optional.of(passiveCitation);
    }

    return publishedUliRepository
        .findByDocumentNumber(passiveCitation.getSourceLiteratureDocumentNumber())
        .map(
            uliRef -> {
              passiveCitation.setSourceCitation(uliRef.getCitation());
              passiveCitation.setSourceAuthor(uliRef.getAuthor());
              passiveCitation.setSourceDocumentTypeRawValue(uliRef.getDocumentTypeRawValue());
              passiveCitation.setSourceLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());
              return passiveCitation;
            });
  }

  /** Case 1: Validation and enrichment for ULI Active Citations (Caselaw -> Uli) */
  @Transactional(readOnly = true)
  public ActiveCitationUliDTO updateActiveUliCitationWithInformationFromTarget(
      ActiveCitationUliDTO activeCitation) {

    if (activeCitation.getTargetLiteratureDocumentNumber() == null) {
      return activeCitation;
    }

    publishedUliRepository
        .findByDocumentNumber(activeCitation.getTargetLiteratureDocumentNumber())
        .ifPresentOrElse(
            uliRef -> {
              activeCitation.setTargetCitation(uliRef.getCitation());
              activeCitation.setTargetAuthor(uliRef.getAuthor());
              activeCitation.setTargetLegalPeriodicalRawValue(uliRef.getLegalPeriodicalRawValue());
            },
            () -> {
              activeCitation.setTargetLiteratureDocumentNumber(null);
            });

    return activeCitation;
  }
}
