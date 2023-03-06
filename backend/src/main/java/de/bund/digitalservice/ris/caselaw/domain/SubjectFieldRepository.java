package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface SubjectFieldRepository {
  Flux<FieldOfLaw> getTopLevelNodes();

  Flux<FieldOfLaw> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber);

  Flux<FieldOfLaw> findBySearchStr(String searchStr, Pageable pageable);

  Mono<Long> countBySearchStr(String searchStr);

  Flux<FieldOfLaw> findByNormsStr(String normsStr, Pageable pageable);

  Mono<Long> countByNormsStr(String normsStr);

  Flux<FieldOfLaw> findByNormsAndSearchStr(String normsStr, String searchStr, Pageable pageable);

  Mono<Long> countByNormsAndSearchStr(String normsStr, String searchStr);

  Mono<FieldOfLaw> findBySubjectFieldNumber(String subjectFieldId);

  Mono<FieldOfLaw> findParentByChild(FieldOfLaw child);

  Flux<FieldOfLaw> findAllByOrderBySubjectFieldNumberAsc(Pageable pageable);

  Flux<FieldOfLaw> findAllForDocumentUnit(UUID documentUnitUuid);

  Flux<FieldOfLaw> addFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier);

  Flux<FieldOfLaw> removeFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier);

  Mono<Long> count();
}
