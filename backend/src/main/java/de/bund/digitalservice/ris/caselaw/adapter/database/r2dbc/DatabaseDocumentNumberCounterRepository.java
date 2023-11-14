package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberCounter;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentNumberCounterRepository
    extends R2dbcRepository<DocumentNumberCounter, Integer> {

  Mono<DocumentNumberCounter> save(DocumentNumberCounter entity);

  Mono<DocumentNumberCounter> findById(Integer id);

  default Mono<DocumentNumberCounter> getDocumentNumberCounterEntry() {
    return findById(1);
  }
}
