package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CaselawCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public CaselawCitationSyncService(
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /**
   * Create passive citations for all active citations of the doc unit.
   *
   * @return list Document numbers of all other documents that have been changed. These should be
   *     published again.
   */
  @Transactional
  public Set<String> syncCitations(DocumentationUnitDTO documentationUnit) {
    Set<String> documentsToRepublish = new HashSet<>();

    if (documentationUnit instanceof DecisionDTO decision) {
      decision
          .getActiveCaselawCitations()
          .forEach(
              activeCitationCaselaw -> {
                var target =
                    documentationUnitRepository.findByDocumentNumber(
                        activeCitationCaselaw.getTargetDocumentNumber());

                if (target.isPresent() && target.get() instanceof DecisionDTO targetDecision) {
                  var matchingPassiveCitation =
                      findMatchingPassiveCitation(
                          targetDecision,
                          decision.getDocumentNumber(),
                          activeCitationCaselaw.getCitationType());
                  if (matchingPassiveCitation.isPresent()) {
                    if (!matchingPassiveCitation
                        .get()
                        .equals(
                            createMatchingPassiveCitation(
                                activeCitationCaselaw, targetDecision, decision, 0))) {
                      log.atInfo()
                          .addKeyValue("publishedDocumentationUnit", decision.getDocumentNumber())
                          .addKeyValue(
                              "targetDocumentationUnit", targetDecision.getDocumentNumber())
                          .addKeyValue("activeCitation", activeCitationCaselaw)
                          .addKeyValue("matchingPassiveCitation", matchingPassiveCitation.get())
                          .setMessage("Updating data of matching passive citation.")
                          .log();
                      matchingPassiveCitation
                          .get()
                          .setCitationType(activeCitationCaselaw.getCitationType());
                      matchingPassiveCitation.get().setSourceDate(decision.getDate());
                      matchingPassiveCitation
                          .get()
                          .setSourceDocumentType(decision.getDocumentType());
                      matchingPassiveCitation
                          .get()
                          .setSourceDocumentNumber(decision.getDocumentNumber());
                      matchingPassiveCitation.get().setSourceCourt(decision.getCourt());
                      matchingPassiveCitation
                          .get()
                          .setSourceFileNumber(
                              decision.getFileNumbers().stream()
                                  .sorted()
                                  .findFirst()
                                  .map(FileNumberDTO::getValue)
                                  .orElse(null));

                      documentationUnitRepository.save(targetDecision);

                      documentsToRepublish.add(targetDecision.getDocumentNumber());
                    }
                  } else {
                    /* targetDecision
                        .getPassiveCaselawCitations()
                        .add(
                            createMatchingPassiveCitation(
                                activeCitationCaselaw,
                                targetDecision,
                                decision,
                                targetDecision.getPassiveCaselawCitations().size()));

                    log.atInfo()
                        .addKeyValue("publishedDocumentationUnit", decision.getDocumentNumber())
                        .addKeyValue("targetDocumentationUnit", targetDecision.getDocumentNumber())
                        .addKeyValue("activeCitation", activeCitationCaselaw)
                        .setMessage("Creating passive citation for published active citation.")
                        .log();

                    documentationUnitRepository.save(targetDecision);

                    documentsToRepublish.add(targetDecision.getDocumentNumber());*/
                  }
                }
              });
    }

    return documentsToRepublish;
  }

  private PassiveCitationCaselawDTO createMatchingPassiveCitation(
      ActiveCitationCaselawDTO activeCitationCaselaw,
      DecisionDTO target,
      DecisionDTO source,
      Integer rank) {
    return PassiveCitationCaselawDTO.builder()
        .citationType(activeCitationCaselaw.getCitationType())
        .target(target)
        .sourceDate(source.getDate())
        .sourceDocumentType(source.getDocumentType())
        .sourceCourt(source.getCourt())
        .sourceDocumentNumber(source.getDocumentNumber())
        .sourceFileNumber(
            source.getFileNumbers().stream().findFirst().map(FileNumberDTO::getValue).orElse(null))
        .rank(rank)
        .build();
  }

  private Optional<PassiveCitationCaselawDTO> findMatchingPassiveCitation(
      DecisionDTO decision, String documentNumber, CitationTypeDTO citationType) {
    return decision.getPassiveCaselawCitations().stream()
        .filter(
            passiveCitationCaselaw -> {
              if (passiveCitationCaselaw.getSourceDocumentNumber() == null) {
                return false;
              }

              return passiveCitationCaselaw.getSourceDocumentNumber().equals(documentNumber)
                  && Objects.equals(passiveCitationCaselaw.getCitationType(), citationType);
            })
        .findFirst();
  }
}
