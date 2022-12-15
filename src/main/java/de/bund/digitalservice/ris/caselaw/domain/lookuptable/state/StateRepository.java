package de.bund.digitalservice.ris.caselaw.domain.lookuptable.state;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StateRepository extends ReactiveSortingRepository<StateDTO, Long> {

  Mono<StateDTO> findByJurisshortcut(String jurisshortcut);
}
