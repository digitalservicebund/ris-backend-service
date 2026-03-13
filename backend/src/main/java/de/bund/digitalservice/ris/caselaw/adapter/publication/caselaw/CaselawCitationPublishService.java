package de.bund.digitalservice.ris.caselaw.adapter.publication.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedDocumentationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PendingProceedingTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import java.util.Objects;
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

    return documentationUnitRepository.findPublishedByDocumentNumber(
        passiveCitationCaselaw.getSourceDocumentNumber());
  }

  private Optional<DocumentationUnitDTO> getActiveCitationTarget(
      ActiveCitationCaselawDTO activeCitationCaselaw) {
    if (activeCitationCaselaw.getTargetDocumentNumber() == null) {
      return Optional.empty();
    }

    return documentationUnitRepository.findPublishedByDocumentNumber(
        activeCitationCaselaw.getTargetDocumentNumber());
  }

  private Optional<DocumentationUnitDTO> getRelatedDocumentationTarget(
      RelatedDocumentationDTO relatedDocumentation) {
    if (relatedDocumentation.getDocumentNumber() == null) {
      return Optional.empty();
    }

    return documentationUnitRepository.findPublishedByDocumentNumber(
        relatedDocumentation.getDocumentNumber());
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
              "SKIPPED: Skipping publishing of a passive citation caselaw as the source document can not be found")
          .log();
      // return Optional.empty();
      return Optional.of(passiveCitationCaselaw);
    }

    toDomain(
        source.get()); // load the lazy properties of the source, we need them later on after the
    // transaction is closed

    //    passiveCitationCaselaw.setSourceDocumentNumber(source.get().getDocumentNumber());
    //    passiveCitationCaselaw.setSourceCourt(source.get().getCourt());
    //    passiveCitationCaselaw.setSourceDate(source.get().getDate());
    //    passiveCitationCaselaw.setSourceFileNumber(
    //        source.get().getFileNumbers().stream()
    //            .findFirst()
    //            .map(FileNumberDTO::getValue)
    //            .orElse(null));
    //    passiveCitationCaselaw.setSourceDocumentType(source.get().getDocumentType());

    if (!Objects.equals(
            passiveCitationCaselaw.getSourceDocumentNumber(), source.get().getDocumentNumber())
        || !Objects.equals(passiveCitationCaselaw.getSourceCourt(), source.get().getCourt())
        || !Objects.equals(passiveCitationCaselaw.getSourceDate(), source.get().getDate())
        || !Objects.equals(
            passiveCitationCaselaw.getSourceFileNumber(),
            source.get().getFileNumbers().stream()
                .findFirst()
                .map(FileNumberDTO::getValue)
                .orElse(null))
        || !Objects.equals(
            passiveCitationCaselaw.getSourceDocumentType(), source.get().getDocumentType())) {

      log.atInfo()
          .addKeyValue(
              LoggingKeys.SOURCE_DOCUMENT_NUMBER,
              passiveCitationCaselaw.getTarget() != null
                  ? passiveCitationCaselaw.getTarget().getDocumentNumber()
                  : null)
          .addKeyValue(
              "passiveCitationCaselaw.sourceDocumentNumber",
              passiveCitationCaselaw.getSourceDocumentNumber())
          .addKeyValue("source.documentNumber", source.get().getDocumentNumber())
          .addKeyValue(
              "passiveCitationCaselaw.sourceCourt", passiveCitationCaselaw.getSourceCourt())
          .addKeyValue("source.court", source.get().getCourt())
          .addKeyValue("passiveCitationCaselaw.sourceDate", passiveCitationCaselaw.getSourceDate())
          .addKeyValue("source.date", source.get().getDate())
          .addKeyValue(
              "passiveCitationCaselaw.sourceFileNumber",
              passiveCitationCaselaw.getSourceFileNumber())
          .addKeyValue(
              "source.fileNumber[0]",
              source.get().getFileNumbers().stream()
                  .findFirst()
                  .map(FileNumberDTO::getValue)
                  .orElse(null))
          .addKeyValue(
              "passiveCitationCaselaw.sourceDocumentType",
              passiveCitationCaselaw.getSourceDocumentType())
          .addKeyValue("source.documentType", source.get().getDocumentType())
          .setMessage(
              "Metadata divergence detected between caselaw passive citation and source caselaw document.")
          .log();
    }

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
      // activeCitationCaselaw.setTargetDocumentNumber(null);
    } else {
      toDomain(
          target.get()); // load the lazy properties of the target, we need them later on after the
      // transaction is closed

      //      activeCitationCaselaw.setTargetDocumentNumber(target.get().getDocumentNumber());
      //      activeCitationCaselaw.setTargetCourt(target.get().getCourt());
      //      activeCitationCaselaw.setTargetDate(target.get().getDate());
      //      activeCitationCaselaw.setTargetFileNumber(
      //          target.get().getFileNumbers().stream()
      //              .findFirst()
      //              .map(FileNumberDTO::getValue)
      //              .orElse(null));
      //      activeCitationCaselaw.setTargetDocumentType(target.get().getDocumentType());
      if (!Objects.equals(
              activeCitationCaselaw.getTargetDocumentNumber(), target.get().getDocumentNumber())
          || !Objects.equals(activeCitationCaselaw.getTargetCourt(), target.get().getCourt())
          || !Objects.equals(activeCitationCaselaw.getTargetDate(), target.get().getDate())
          || !Objects.equals(
              activeCitationCaselaw.getTargetFileNumber(),
              target.get().getFileNumbers().stream()
                  .findFirst()
                  .map(FileNumberDTO::getValue)
                  .orElse(null))
          || !Objects.equals(
              activeCitationCaselaw.getTargetDocumentType(), target.get().getDocumentType())) {

        log.atInfo()
            .addKeyValue(
                LoggingKeys.SOURCE_DOCUMENT_NUMBER,
                activeCitationCaselaw.getSource() != null
                    ? activeCitationCaselaw.getSource().getDocumentNumber()
                    : null)
            .addKeyValue(
                "activeCitation.targetDocumentNumber",
                activeCitationCaselaw.getTargetDocumentNumber())
            .addKeyValue("target.documentNumber", target.get().getDocumentNumber())
            .addKeyValue("activeCitation.targetCourt", activeCitationCaselaw.getTargetCourt())
            .addKeyValue("target.court", target.get().getCourt())
            .addKeyValue("activeCitation.targetDate", activeCitationCaselaw.getTargetDate())
            .addKeyValue("target.date", target.get().getDate())
            .addKeyValue(
                "activeCitation.targetFileNumber", activeCitationCaselaw.getTargetFileNumber())
            .addKeyValue(
                "target.fileNumber[0]",
                target.get().getFileNumbers().stream()
                    .findFirst()
                    .map(FileNumberDTO::getValue)
                    .orElse(null))
            .addKeyValue(
                "activeCitation.targetDocumentType", activeCitationCaselaw.getTargetDocumentType())
            .addKeyValue("target.documentType", target.get().getDocumentType())
            .setMessage(
                "Metadata divergence detected between caselaw active citation and target caselaw document.")
            .log();
      }
    }

    return activeCitationCaselaw;
  }

  /** Update the citation target with the information from the actual target document. */
  @Transactional
  public <T extends RelatedDocumentationDTO> T updateRelatedDocumentationWithInformationFromTarget(
      T relatedDocumentation) {
    var target = getRelatedDocumentationTarget(relatedDocumentation);

    if (target.isEmpty()) {
      // relatedDocumentation.setDocumentNumber(null);
    } else {
      toDomain(
          target.get()); // load the lazy properties of the target, we need them later on after the
      // transaction is closed

      //      relatedDocumentation.setDocumentNumber(target.get().getDocumentNumber());
      //      relatedDocumentation.setCourt(target.get().getCourt());
      //      relatedDocumentation.setDate(target.get().getDate());
      //      relatedDocumentation.setFileNumber(
      //          target.get().getFileNumbers().stream()
      //              .findFirst()
      //              .map(FileNumberDTO::getValue)
      //              .orElse(null));
      //      relatedDocumentation.setDocumentType(target.get().getDocumentType());

      if (!Objects.equals(
              relatedDocumentation.getDocumentNumber(), target.get().getDocumentNumber())
          || !Objects.equals(relatedDocumentation.getCourt(), target.get().getCourt())
          || !Objects.equals(relatedDocumentation.getDate(), target.get().getDate())
          || !Objects.equals(
              relatedDocumentation.getFileNumber(),
              target.get().getFileNumbers().stream()
                  .findFirst()
                  .map(FileNumberDTO::getValue)
                  .orElse(null))
          || !Objects.equals(
              relatedDocumentation.getDocumentType(), target.get().getDocumentType())) {

        log.atInfo()
            .addKeyValue("relatedDocumentation.id", relatedDocumentation.getId())
            .addKeyValue(
                "relatedDocumentation.documentNumber", relatedDocumentation.getDocumentNumber())
            .addKeyValue("target.documentNumber", target.get().getDocumentNumber())
            .addKeyValue("relatedDocumentation.court", relatedDocumentation.getCourt())
            .addKeyValue("target.court", target.get().getCourt())
            .addKeyValue("relatedDocumentation.date", relatedDocumentation.getDate())
            .addKeyValue("target.date", target.get().getDate())
            .addKeyValue("relatedDocumentation.fileNumber", relatedDocumentation.getFileNumber())
            .addKeyValue(
                "target.fileNumber[0]",
                target.get().getFileNumbers().stream()
                    .findFirst()
                    .map(FileNumberDTO::getValue)
                    .orElse(null))
            .addKeyValue(
                "relatedDocumentation.documentType", relatedDocumentation.getDocumentType())
            .addKeyValue("target.documentType", target.get().getDocumentType())
            .setMessage(
                "Metadata divergence detected between caselaw related document and target caselaw document.")
            .log();
      }
    }

    return relatedDocumentation;
  }
}
