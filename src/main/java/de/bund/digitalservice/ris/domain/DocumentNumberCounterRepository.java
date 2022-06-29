package de.bund.digitalservice.ris.domain;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface DocumentNumberCounterRepository
    extends Repository<DocumentNumberCounter, Integer> {

  Mono<DocumentNumberCounter> save(DocumentNumberCounter entity);

  Mono<DocumentNumberCounter> findById(Integer id);

  default Mono<DocumentNumberCounter> getDocumentNumberCounterEntry() {
    return findById(1);
  }
}
