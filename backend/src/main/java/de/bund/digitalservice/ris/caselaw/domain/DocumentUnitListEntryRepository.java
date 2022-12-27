package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface DocumentUnitListEntryRepository {

  Flux<DocumentUnitListEntry> findAll(Sort sort);
}
