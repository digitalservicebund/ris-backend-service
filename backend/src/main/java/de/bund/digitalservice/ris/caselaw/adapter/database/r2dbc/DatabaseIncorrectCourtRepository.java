package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DatabaseIncorrectCourtRepository
    extends ReactiveSortingRepository<IncorrectCourtDTO, Long> {

  Mono<Void> deleteAllByDocumentUnitId(Long documentUnitId);

  Flux<IncorrectCourtDTO> findAllByDocumentUnitId(Long documentUnitId);
}
