package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface FieldOfLawRepository {
  Flux<FieldOfLaw> getTopLevelNodes();

  Flux<FieldOfLaw> findAllByParentIdentifierOrderByIdentifierAsc(String identifier);

  Mono<FieldOfLaw> findByIdentifier(String identifier);

  Mono<FieldOfLaw> findParentByChild(FieldOfLaw child);

  Flux<FieldOfLaw> findAllByOrderByIdentifierAsc(Pageable pageable);

  Mono<List<FieldOfLaw>> findAllForDocumentUnit(UUID documentUnitUuid);

  Mono<List<FieldOfLaw>> addFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier);

  Mono<List<FieldOfLaw>> removeFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier);

  Mono<Long> count();

  Flux<FieldOfLaw> findBySearchTerms(String[] searchTerms);

  Flux<FieldOfLaw> findByNormStr(String normStr);

  Flux<FieldOfLaw> findByNormStrAndSearchTerms(String normStr, String[] searchTerms);
}
