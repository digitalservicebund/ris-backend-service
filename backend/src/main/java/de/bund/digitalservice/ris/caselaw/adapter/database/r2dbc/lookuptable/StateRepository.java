package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StateRepository extends R2dbcRepository<StateDTO, Long> {

  Mono<StateDTO> findByJurisshortcut(String jurisshortcut);
}
