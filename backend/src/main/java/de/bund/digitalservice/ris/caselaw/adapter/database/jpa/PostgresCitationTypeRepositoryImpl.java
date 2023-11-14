package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCitationTypeRepositoryImpl implements CitationTypeRepository {
  private final DatabaseCitationTypeRepository repository;

  public PostgresCitationTypeRepositoryImpl(DatabaseCitationTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CitationType> findAllBySearchStr(String searchStr) {
    return repository.findBySearchStr(searchStr).stream()
        .map(CitationTypeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<CitationType> findAllByCitationDocumentCategoryOrderByAbbreviation(
      String documentCategoryLabel) {
    return repository
        .findAllByCitationDocumentCategoryOrderByAbbreviation(documentCategoryLabel)
        .stream()
        .map(CitationTypeTransformer::transformToDomain)
        .toList();
  }
}
