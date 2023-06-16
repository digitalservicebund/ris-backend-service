package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationStyleTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresCitationStyleRepositoryImpl implements CitationStyleRepository {
  private final DatabaseCitationStyleRepository repository;

  public PostgresCitationStyleRepositoryImpl(DatabaseCitationStyleRepository repository) {
    this.repository = repository;
  }

  @Override
  public Flux<CitationStyle> findBySearchStr(String searchString) {
    return repository
        .findBySearchStr(searchString)
        .map(CitationStyleTransformer::transformToDomain);
  }

  @Override
  public Flux<CitationStyle> findAllByOrderByCitationDocumentTypeAsc() {
    return repository
        .findAllByDocumentTypeAndCitationDocumentTypeOrderByCitationDocumentTypeAsc('R', 'R')
        .map(CitationStyleTransformer::transformToDomain);
  }
}
