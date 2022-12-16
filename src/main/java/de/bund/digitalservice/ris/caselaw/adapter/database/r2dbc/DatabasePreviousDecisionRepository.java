package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.List;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabasePreviousDecisionRepository
    extends ReactiveCrudRepository<PreviousDecisionDTO, Long> {
  Flux<PreviousDecisionDTO> findAllByDocumentUnitId(Long documentUnitId);

  Flux<PreviousDecisionDTO> saveAll(List<PreviousDecisionDTO> previousDecisions);

  Mono<Void> deleteAllByDocumentUnitId(Long id);
}
