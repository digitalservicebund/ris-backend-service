package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface SubjectFieldRepository {
  Flux<SubjectField> findAllByParentIdOrderBySubjectFieldNumberAsc(Long id);

  Flux<SubjectField> findBySearchStr(String searchStr);
}
