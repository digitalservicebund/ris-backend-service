package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Deprecated
public interface DatabaseDeviatingDecisionDateRepository
    extends R2dbcRepository<DeviatingDecisionDateDTO, Long> {

  Mono<Void> deleteAllByDocumentUnitId(Long documentUnitId);

  Flux<DeviatingDecisionDateDTO> findAllByDocumentUnitId(Long documentUnitId);
}
