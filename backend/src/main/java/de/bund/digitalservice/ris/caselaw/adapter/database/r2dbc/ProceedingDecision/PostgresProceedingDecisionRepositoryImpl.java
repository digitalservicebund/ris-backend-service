package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PostgresProceedingDecisionRepositoryImpl implements ProceedingDecisionRepository {

  private final DatabaseProceedingDecisionLinkRepository linkRepository;
  private final DatabaseDocumentUnitRepository documentUnitRepository;

  PostgresProceedingDecisionRepositoryImpl(
      DatabaseProceedingDecisionLinkRepository linkRepository,
      DatabaseDocumentUnitRepository documentUnitRepository) {
    this.linkRepository = linkRepository;
    this.documentUnitRepository = documentUnitRepository;
  }

  public Mono<Void> addProceedingDecision(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<DocumentUnitDTO> parentDocumentUnit =
        documentUnitRepository.findByUuid(parentDocumentUnitUuid);
    Mono<DocumentUnitDTO> childDocumentUnit =
        documentUnitRepository.findByUuid(childDocumentUnitUuid);

    return Mono.zip(parentDocumentUnit, childDocumentUnit)
        .flatMap(
            tuple ->
                Mono.fromRunnable(
                    () -> {
                      linkRepository.save(
                          ProceedingDecisionLinkDTO.builder()
                              .parentDocumentUnitId(tuple.getT1().getId())
                              .childDocumentUnitId(tuple.getT2().getId())
                              .build());
                    }));
  }
}
