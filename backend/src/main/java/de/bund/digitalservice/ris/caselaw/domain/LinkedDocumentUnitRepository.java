package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface LinkedDocumentUnitRepository {
  Flux<LinkedDocumentUnit> findAllByDocumentUnitId(Long id);
}
