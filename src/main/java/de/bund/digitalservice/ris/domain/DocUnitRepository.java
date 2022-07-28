package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface DocUnitRepository extends ReactiveSortingRepository<DocUnit, Long> {

  Mono<DocUnit> findByDocumentnumber(String documentnumber);

  Mono<DocUnit> findByUuid(UUID uuid);
}
