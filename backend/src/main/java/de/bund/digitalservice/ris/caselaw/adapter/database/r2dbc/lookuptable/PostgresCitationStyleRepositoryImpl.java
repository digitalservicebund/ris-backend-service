package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationTypeRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCitationStyleRepositoryImpl implements CitationTypeRepository {
  private final DatabaseCitationTypeRepository repository;

  public PostgresCitationStyleRepositoryImpl(DatabaseCitationTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public CitationType findBySearchStr(String searchString) {
    return CitationTypeTransformer.transformToDomain(repository.findBySearchStr(searchString));
  }

  @Override
  public CitationType findAllByOrderByCitationDocumentTypeAsc() {
    return CitationTypeTransformer.transformToDomain(repository.findAllByDocumentTypes('R', 'R'));
  }
}
