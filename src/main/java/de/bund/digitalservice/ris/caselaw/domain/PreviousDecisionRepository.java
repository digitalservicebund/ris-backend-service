package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PreviousDecisionRepository
    extends ReactiveCrudRepository<PreviousDecision, String> {
  @Query("SELECT id FROM previous_decision WHERE documentnumber = $1")
  Flux<Long> getAllIdsByDocumentnumber(String documentnumber);

  Flux<PreviousDecision> findAllByDocumentnumber(String documentnumber);
}
