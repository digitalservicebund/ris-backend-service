package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.UliRefView;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UliCitationSyncService {

  private final DatabaseDocumentationUnitRepository caselawRepository;
  private final UliActiveCitationRefViewRepository uliActiveCitationRefViewRepository;
  private final UliRefViewRepository uliRefViewRepository;

  public UliCitationSyncService(
      DatabaseDocumentationUnitRepository caselawRepository,
      UliActiveCitationRefViewRepository uliActiveCitationRefViewRepository,
      UliRefViewRepository uliRefViewRepository) {
    this.caselawRepository = caselawRepository;
    this.uliActiveCitationRefViewRepository = uliActiveCitationRefViewRepository;
    this.uliRefViewRepository = uliRefViewRepository;
  }

  /**
   * Case 2: Find documents that need new passive ULI citations based on active citations from other
   * streams. * @return Set of document numbers that need to be republished.
   */
  @Transactional
  public Set<String> syncUliPassiveCitations() {
    Set<String> documentsToRepublish = new HashSet<>();

    var allUliToCaselawActiveCitations = uliActiveCitationRefViewRepository.findAll();

    for (var link : allUliToCaselawActiveCitations) {
      caselawRepository
          .findById(link.getTargetId())
          .ifPresent(
              documentationUnit -> {
                if (documentationUnit instanceof DecisionDTO decision) {

                  uliRefViewRepository
                      .findById(link.getSourceId())
                      .ifPresent(
                          uliRef -> {
                            var existingPassiveCitationOpt =
                                decision.getPassiveUliCitations().stream()
                                    .filter(
                                        p ->
                                            uliRef
                                                .getDocumentNumber()
                                                .equals(p.getSourceLiteratureDocumentNumber()))
                                    .findFirst();

                            if (existingPassiveCitationOpt.isEmpty()) {
                              var newPassive =
                                  PassiveCitationUliDTO.builder()
                                      .sourceLiteratureDocumentNumber(uliRef.getDocumentNumber())
                                      .sourceCitation(uliRef.getCitation())
                                      .sourceAuthor(uliRef.getAuthor())
                                      .sourceDocumentTypeRawValue(uliRef.getDocumentTypeRawValue())
                                      .sourceLegalPeriodicalRawValue(
                                          uliRef.getLegalPeriodicalRawValue())
                                      .rank(decision.getPassiveUliCitations().size() + 1)
                                      .build();

                              decision.getPassiveUliCitations().add(newPassive);
                              caselawRepository.save(decision);
                              documentsToRepublish.add(decision.getDocumentNumber());
                            } else {
                              var existingPassive = existingPassiveCitationOpt.get();
                              if (existingCitationChanged(existingPassive, uliRef)) {
                                existingPassive.setSourceCitation(uliRef.getCitation());
                                existingPassive.setSourceAuthor(uliRef.getAuthor());
                                existingPassive.setSourceDocumentTypeRawValue(
                                    uliRef.getDocumentTypeRawValue());
                                existingPassive.setSourceLegalPeriodicalRawValue(
                                    uliRef.getLegalPeriodicalRawValue());

                                caselawRepository.save(decision);
                                documentsToRepublish.add(decision.getDocumentNumber());
                              }
                            }
                          });
                }
              });
    }

    return documentsToRepublish;
  }

  private boolean existingCitationChanged(PassiveCitationUliDTO existing, UliRefView ref) {
    return !Objects.equals(existing.getSourceCitation(), ref.getCitation())
        || !Objects.equals(existing.getSourceAuthor(), ref.getAuthor())
        || !Objects.equals(existing.getSourceDocumentTypeRawValue(), ref.getDocumentTypeRawValue())
        || !Objects.equals(
            existing.getSourceLegalPeriodicalRawValue(), ref.getLegalPeriodicalRawValue());
  }

  /**
   * Case 3: Identify documents that point to repealed or deleted ULI documents. * @return Set of
   * document numbers (the sources of the links) that need to be republished to remove the links
   * from the portal.
   */
  @Transactional
  public Set<String> handleUliRepeals() {
    log.info("Starting ULI repeal sync");
    Set<String> documentsToRepublish = new HashSet<>();

    // Find in repeal table all ULI documentNumbers, that are the target of an activecitation or the
    // source of a passive citation in caselaw and republish them.
    // TODO:
    // documentsToRepublish.addAll(documentationUnitRepository.findDocumentsPointingToRepealedUli());

    return documentsToRepublish;
  }
}
