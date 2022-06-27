package de.bund.digitalservice.ris.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface DocUnitRepository extends ReactiveCrudRepository<DocUnit, Long> {}
