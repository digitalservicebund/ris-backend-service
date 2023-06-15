package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface CitationStyleRepository {
  Flux<CitationStyle> findBySearchStr(String searchString);

  Flux<CitationStyle> findAllByOrderByCitationDocumentTypeAsc();
}
