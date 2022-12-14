package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviatingDecisionDateRepository
    extends ReactiveSortingRepository<DeviatingDecisionDateDTO, Long> {

  Mono<Void> deleteAllByDocumentUnitId(Long documentUnitId);

  Flux<DeviatingDecisionDateDTO> findAllByDocumentUnitId(Long documentUnitId);
}
