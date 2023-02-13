package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface KeywordRepository extends R2dbcRepository<KeywordDTO, Long> {

  Flux<KeywordDTO> findAllByOrderBySubjectFieldIdAscValueAsc();

  Flux<KeywordDTO> findAllBySubjectFieldIdOrderByValueAsc(Long subjectFieldId);
}
