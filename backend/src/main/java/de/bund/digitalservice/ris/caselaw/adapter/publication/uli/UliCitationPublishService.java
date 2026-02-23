package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UliCitationPublishService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public UliCitationPublishService(
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /** Case 1: Validation and enrichment for ULI Passive Citations (Uli -> Caselaw) */
  @Transactional(readOnly = true)
  public Optional<PassiveCitationUliDTO> updatePassiveUliCitationWithInformationFromSource(
      PassiveCitationUliDTO passiveCitation) {

    if (passiveCitation.getSourceLiteratureDocumentNumber() == null) {
      return Optional.of(passiveCitation);
    }

    // todo: look in ULI schema and find actual match by getSourceLiteratureDocumentNumber
    return Optional.of(new PassiveCitationUliDTO());
  }

  /** Case 1: Validation and enrichment for ULI Active Citations (Caselaw -> Uli) */
  @Transactional(readOnly = true)
  public ActiveCitationUliDTO updateActiveUliCitationWithInformationFromTarget(
      ActiveCitationUliDTO activeCitation) {

    if (activeCitation.getTargetLiteratureDocumentNumber() == null) {
      return activeCitation;
    }

    // todo: look in ULI schema and find actual match, updating the targetLiteratureDocumentNumber
    // activeCitation.setTargetLiteratureDocumentNumber(match.documentNumber);

    return activeCitation;
  }
}
