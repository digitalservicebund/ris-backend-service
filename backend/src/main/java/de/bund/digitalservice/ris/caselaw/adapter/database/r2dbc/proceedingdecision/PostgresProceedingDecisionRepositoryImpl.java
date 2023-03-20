package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProceedingDecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresProceedingDecisionRepositoryImpl implements ProceedingDecisionRepository {

  private final DatabaseProceedingDecisionLinkRepository linkRepository;
  private final DatabaseProceedingDecisionRepository repository;

  PostgresProceedingDecisionRepositoryImpl(
          DatabaseProceedingDecisionRepository repository,
      DatabaseProceedingDecisionLinkRepository linkRepository) {
    this.linkRepository = linkRepository;
    this.repository = repository;
  }

  public Flux<ProceedingDecision> findAllForDocumentUnit(UUID parentDocumentUnitUuid){
    return repository.findAllById(
            repository.findByUuid(parentDocumentUnitUuid)
                    .map(ProceedingDecisionDTO::id)
                    .flatMapMany(linkRepository::findAllByParentDocumentUnitId)
                    .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId))
            .map(ProceedingDecisionTransformer::transformToDomain);
    };

    public Mono<ProceedingDecisionLinkDTO> linkProceedingDecisions(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<Long> parentDocumentUnitId =
            repository.findByUuid(parentDocumentUnitUuid).map(ProceedingDecisionDTO::id);
    Mono<Long> childDocumentUnitId =
            repository.findByUuid(childDocumentUnitUuid).map(ProceedingDecisionDTO::id);

    return Mono.zip(parentDocumentUnitId, childDocumentUnitId)
        .flatMap(
            tuple ->
               linkRepository.save(
                          ProceedingDecisionLinkDTO.builder()
                              .parentDocumentUnitId(tuple.getT1())
                              .childDocumentUnitId(tuple.getT2())
                              .build()));

  }
}
