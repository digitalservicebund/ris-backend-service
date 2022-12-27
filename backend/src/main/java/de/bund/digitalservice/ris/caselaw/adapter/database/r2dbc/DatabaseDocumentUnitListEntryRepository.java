package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseDocumentUnitListEntryRepository
    extends ReactiveSortingRepository<DocumentUnitListEntry, Long> {

  Flux<DocumentUnitListEntry> findAll(Sort sort);
}
