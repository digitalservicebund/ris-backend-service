package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaselawCitationPublishService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public CaselawCitationPublishService(
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
  }

  private Optional<DocumentationUnitDTO> getPassiveCitationSource(
      PassiveCitationCaselawDTO passiveCitationCaselaw) {
    if (passiveCitationCaselaw.getSourceDocumentNumber() == null) {
      return Optional.empty();
    }

    return documentationUnitRepository.findByDocumentNumber(
        passiveCitationCaselaw.getSourceDocumentNumber());
  }

  private Optional<DocumentationUnitDTO> getActiveCitationTarget(
      ActiveCitationCaselawDTO activeCitationCaselaw) {
    if (activeCitationCaselaw.getTargetDocumentNumber() == null) {
      return Optional.empty();
    }

    return documentationUnitRepository.findByDocumentNumber(
        activeCitationCaselaw.getTargetDocumentNumber());
  }

  /**
   * Update the passive citation with the information from the source. If the source can not be
   * found we only want to keep the passive citation if it is an actual blind-link (so has no source
   * document number).
   */
  public Optional<PassiveCitationCaselawDTO> updatePassiveCitationSourceWithInformationFromSource(
      PassiveCitationCaselawDTO passiveCitationCaselaw) {
    if (passiveCitationCaselaw.getSourceDocumentNumber() == null) {
      return Optional.of(passiveCitationCaselaw);
    }

    var source = getPassiveCitationSource(passiveCitationCaselaw);
    if (source.isEmpty()) {
      log.atDebug()
          .addKeyValue("sourceDocumentNumber", passiveCitationCaselaw.getSourceDocumentNumber())
          .addKeyValue("passiveCitationCaselawId", passiveCitationCaselaw.getId())
          .setMessage(
              "Skipping publishing of a passive citation caselaw as the source document can not be found")
          .log();
      return Optional.empty();
    }

    passiveCitationCaselaw.setSourceDocumentNumber(source.get().getDocumentNumber());
    passiveCitationCaselaw.setSourceCourt(source.get().getCourt());
    passiveCitationCaselaw.setSourceDate(source.get().getDate());
    passiveCitationCaselaw.setSourceFileNumber(
        source.get().getFileNumbers().stream()
            .findFirst()
            .map(FileNumberDTO::getValue)
            .orElse(null));
    passiveCitationCaselaw.setSourceDocumentType(source.get().getDocumentType());

    return Optional.of(passiveCitationCaselaw);
  }

  /** Update the citation target with the information from the actual target document. */
  public ActiveCitationCaselawDTO updateActiveCitationTargetWithInformationFromTarget(
      ActiveCitationCaselawDTO activeCitationCaselaw) {
    var target = getActiveCitationTarget(activeCitationCaselaw);

    if (target.isEmpty()) {
      activeCitationCaselaw.setTargetDocumentNumber(null);
    } else {
      activeCitationCaselaw.setTargetDocumentNumber(target.get().getDocumentNumber());
      activeCitationCaselaw.setTargetCourt(target.get().getCourt());
      activeCitationCaselaw.setTargetDate(target.get().getDate());
      activeCitationCaselaw.setTargetFileNumber(
          target.get().getFileNumbers().stream()
              .findFirst()
              .map(FileNumberDTO::getValue)
              .orElse(null));
      activeCitationCaselaw.setTargetDocumentType(target.get().getDocumentType());
    }

    return activeCitationCaselaw;
  }
}
