package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface NormAbbreviationRepository {
  public Mono<NormAbbreviation> findById(UUID id);

  Flux<NormAbbreviation> findBySearchQuery(String query, Integer size, Integer page);

  Mono<List<NormAbbreviation>> findByAwesomeSearchQuery(String query, Integer size, Integer page);

  Mono<Void> refreshMaterializedViews();
}
