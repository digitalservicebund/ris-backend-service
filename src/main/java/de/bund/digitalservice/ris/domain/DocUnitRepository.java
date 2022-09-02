package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DocUnitRepository extends ReactiveSortingRepository<DocUnit, Long> {

  Mono<DocUnit> findByDocumentnumber(String documentnumber);

  Mono<DocUnit> findByUuid(UUID uuid);
}
