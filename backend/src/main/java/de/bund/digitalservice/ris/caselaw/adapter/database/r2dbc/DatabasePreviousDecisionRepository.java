package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabasePreviousDecisionRepository
    extends R2dbcRepository<PreviousDecisionDTO, Long> {
  Flux<PreviousDecisionDTO> findAllByDocumentUnitId(Long documentUnitId);

  Flux<PreviousDecisionDTO> saveAll(List<PreviousDecisionDTO> previousDecisions);

  Mono<Void> deleteAllByDocumentUnitId(Long id);
}
