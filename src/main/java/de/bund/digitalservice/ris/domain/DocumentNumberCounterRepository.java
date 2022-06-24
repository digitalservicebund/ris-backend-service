package de.bund.digitalservice.ris.domain;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface DocumentNumberCounterRepository
    extends SingleRowRepository<DocumentNumberCounter, Integer> {

  default Mono<DocumentNumberCounter> getDocumentNumberCounterEntry() {
    return findById(1);
  }
}
