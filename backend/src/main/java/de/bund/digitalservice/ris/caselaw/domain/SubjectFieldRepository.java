package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface SubjectFieldRepository {
  Flux<SubjectField> getTopLevelNodes();

  Flux<SubjectField> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber);

  Flux<SubjectField> findBySearchStr(String searchStr);

  Mono<SubjectField> findBySubjectFieldNumber(String subjectFieldId);

  Mono<SubjectField> findById(Long id);

  Mono<SubjectField> findParentByChild(SubjectField child);
}
