package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviatingEcliRepository extends ReactiveSortingRepository<DeviatingEcliDTO, Long> {

  Mono<Void> deleteAllByDocumentUnitId(Long documentUnitId);

  Flux<DeviatingEcliDTO> findAllByDocumentUnitId(Long documentUnitId);
}
