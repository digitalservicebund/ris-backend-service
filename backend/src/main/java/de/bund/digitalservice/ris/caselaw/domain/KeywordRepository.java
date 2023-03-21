package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface KeywordRepository {
  Mono<List<String>> findAllByDocumentUnit(UUID documentUnitUuid);

  Mono<List<String>> addKeywordToDocumentUnit(UUID documentUnitUuid, String keyword);

  Mono<List<String>> deleteKeywordFromDocumentUnit(UUID documentUnitUuid, String keyword);
}
