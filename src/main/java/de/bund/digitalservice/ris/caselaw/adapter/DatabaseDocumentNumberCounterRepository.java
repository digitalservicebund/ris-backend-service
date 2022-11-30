package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberCounter;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface DatabaseDocumentNumberCounterRepository
    extends Repository<DocumentNumberCounter, Integer> {

  Mono<DocumentNumberCounter> save(DocumentNumberCounter entity);

  Mono<DocumentNumberCounter> findById(Integer id);

  default Mono<DocumentNumberCounter> getDocumentNumberCounterEntry() {
    return findById(1);
  }
}
