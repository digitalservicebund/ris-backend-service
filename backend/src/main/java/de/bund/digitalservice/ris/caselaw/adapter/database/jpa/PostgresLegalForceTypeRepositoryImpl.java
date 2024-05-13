package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalForceTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceTypeRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the {@link LegalForceTypeRepository} interface for accessing legal force types
 * stored in a PostgreSQL database.
 */
@Repository
public class PostgresLegalForceTypeRepositoryImpl implements LegalForceTypeRepository {

  private final DatabaseLegalForceTypeRepository repository;

  /**
   * Constructs a {@code PostgresLegalForceTypeRepositoryImpl} with the specified {@link
   * DatabaseLegalForceTypeRepository}.
   *
   * @param repository The repository used to access legal force type data from the database.
   */
  public PostgresLegalForceTypeRepositoryImpl(DatabaseLegalForceTypeRepository repository) {
    this.repository = repository;
  }

  /**
   * Retrieves a list of legal force types filtered by the provided search string.
   *
   * @param searchString The search string used to filter legal force types by abbreviation.
   * @return A list of {@link LegalForceType} objects matching the search criteria.
   */
  @Override
  public List<LegalForceType> findBySearchStr(String searchString) {
    return repository.findAllByAbbreviationStartsWithIgnoreCase(searchString).stream()
        .map(LegalForceTypeTransformer::transformToDomain)
        .toList();
  }

  /**
   * Retrieves a list of all legal force types ordered by abbreviation.
   *
   * @return A list of {@link LegalForceType} objects ordered by abbreviation.
   */
  @Override
  public List<LegalForceType> findAllByOrderByAbbreviation() {
    return repository.findAllByOrderByAbbreviation().stream()
        .map(LegalForceTypeTransformer::transformToDomain)
        .toList();
  }
}
