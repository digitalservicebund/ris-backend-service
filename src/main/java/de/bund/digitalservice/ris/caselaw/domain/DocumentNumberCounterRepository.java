package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface DocumentNumberCounterRepository
    extends R2dbcRepository<DocumentNumberCounter, Integer> {

  Mono<DocumentNumberCounter> save(DocumentNumberCounter entity);

  Mono<DocumentNumberCounter> findById(Integer id);

  default Mono<DocumentNumberCounter> getDocumentNumberCounterEntry() {
    return findById(1);
  }
}
