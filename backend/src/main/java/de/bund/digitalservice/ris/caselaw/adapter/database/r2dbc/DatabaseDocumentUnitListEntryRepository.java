package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseDocumentUnitListEntryRepository
    extends R2dbcRepository<DocumentUnitListEntry, Long> {

  Flux<DocumentUnitListEntry> findAllByDataSourceLike(Sort sort, String dataSource);
}
