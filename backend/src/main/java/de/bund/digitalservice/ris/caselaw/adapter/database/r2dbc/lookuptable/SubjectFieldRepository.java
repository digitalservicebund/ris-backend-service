package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubjectFieldRepository extends R2dbcRepository<SubjectFieldDTO, Long> {

  Flux<SubjectFieldDTO> findAllByParentIdOrderBySubjectFieldNumberAsc(Long id);

  @Query( // TODO  schlagworte und normen mit durchsuchen?
      "SELECT * FROM lookuptable_subject_field WHERE UPPER(CONCAT(subject_field_text, ' ', subject_field_number, ' ', navigation_term)) LIKE UPPER('%'||:searchStr||'%') ORDER BY subject_field_number")
  Flux<SubjectFieldDTO> findBySearchStr(String searchStr);

  Flux<SubjectFieldDTO> findAllByOrderBySubjectFieldNumberAsc();
}
