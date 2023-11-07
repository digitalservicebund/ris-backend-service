package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationTypeRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCitationStyleRepositoryImpl implements CitationTypeRepository {
  private final DatabaseCitationTypeRepository repository;

  public PostgresCitationStyleRepositoryImpl(DatabaseCitationTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CitationType> findBySearchStr(String searchString) {
    return repository.findBySearchStr(searchString).stream()
        .map(CitationTypeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<CitationType> findAllByOrderByCitationDocumentTypeAsc() {
    return repository.findAllByDocumentTypes('R', 'R').stream()
        .map(CitationTypeTransformer::transformToDomain)
        .toList();
  }
}
