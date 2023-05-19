package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NormAbbreviationRepository {
  public Mono<NormAbbreviation> findById(UUID id);

  Flux<NormAbbreviation> findBySearchQuery(String query, Integer size, Integer page);
}
