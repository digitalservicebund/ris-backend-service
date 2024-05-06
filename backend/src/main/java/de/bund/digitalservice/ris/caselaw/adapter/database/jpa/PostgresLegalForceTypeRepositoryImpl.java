package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalForceTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceTypeRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresLegalForceTypeRepositoryImpl implements LegalForceTypeRepository {

  private final DatabaseLegalForceTypeRepository repository;

  public PostgresLegalForceTypeRepositoryImpl(DatabaseLegalForceTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<LegalForceType> findBySearchStr(String searchString) {
    return repository.findAllByAbbreviationStartsWithIgnoreCase(searchString).stream()
        .map(LegalForceTypeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<LegalForceType> findAllByOrderByAbbreviation() {
    return repository.findAllByOrderByAbbreviation().stream()
        .map(LegalForceTypeTransformer::transformToDomain)
        .toList();
  }
}
