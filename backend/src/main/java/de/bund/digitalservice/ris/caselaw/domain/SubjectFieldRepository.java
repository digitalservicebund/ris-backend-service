package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface SubjectFieldRepository {
  Flux<FieldOfLaw> getTopLevelNodes();

  Flux<FieldOfLaw> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber);

  Flux<FieldOfLaw> findBySearchStr(String searchStr);

  Mono<FieldOfLaw> findBySubjectFieldNumber(String subjectFieldId);

  Mono<FieldOfLaw> findParentByChild(FieldOfLaw child);

  Flux<FieldOfLaw> findAllByOrderBySubjectFieldNumberAsc();
}
