package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PendingProceedingTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
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

    toDomain(
        source.get()); // load the lazy properties of the source, we need them later on after the
    // transaction is closed

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

  private DocumentationUnit toDomain(DocumentationUnitDTO documentationUnit) {
    if (documentationUnit instanceof DecisionDTO decisionDTO) {
      return DecisionTransformer.transformToDomain(decisionDTO, null);
    }
    if (documentationUnit instanceof PendingProceedingDTO pendingProceedingDTO) {
      return PendingProceedingTransformer.transformToDomain(pendingProceedingDTO, null);
    }
    return null;
  }

  /** Update the citation target with the information from the actual target document. */
  @Transactional
  public ActiveCitationCaselawDTO updateActiveCitationTargetWithInformationFromTarget(
      ActiveCitationCaselawDTO activeCitationCaselaw) {
    var target = getActiveCitationTarget(activeCitationCaselaw);

    if (target.isEmpty()) {
      activeCitationCaselaw.setTargetDocumentNumber(null);
    } else {
      toDomain(
          target.get()); // load the lazy properties of the target, we need them later on after the
      // transaction is closed

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
