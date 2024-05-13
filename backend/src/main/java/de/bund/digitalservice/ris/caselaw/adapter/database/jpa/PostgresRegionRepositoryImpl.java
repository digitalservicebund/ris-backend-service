package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.RegionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.RegionRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the {@link RegionRepository} interface for accessing region data stored in a
 * PostgreSQL database.
 */
@Repository
public class PostgresRegionRepositoryImpl implements RegionRepository {

  private final DatabaseRegionRepository repository;

  /**
   * Constructs a {@code PostgresRegionRepositoryImpl} with the specified {@link
   * DatabaseRegionRepository}.
   *
   * @param repository The repository used to access region data from the database.
   */
  public PostgresRegionRepositoryImpl(DatabaseRegionRepository repository) {
    this.repository = repository;
  }

  /**
   * Retrieves a list of applicable regions filtered by the provided search string. The property
   * 'applicability' is true, when the region could be used as 'Geltungsbereich' for
   * 'Gesetzeskraft'.
   *
   * @param searchString The search string used to filter applicable regions by code or long text.
   * @return A list of {@link Region} objects representing applicable regions matching the search
   *     criteria.
   */
  @Override
  public List<Region> findApplicableBySearchStr(String searchString) {
    return repository
        .findAllByCodeStartsWithIgnoreCaseOrLongTextStartsWithIgnoreCase(searchString, searchString)
        .stream()
        .filter(RegionDTO::isApplicability)
        .map(RegionTransformer::transformDTO)
        .toList();
  }

  /**
   * Retrieves a list of all applicable regions ordered by code.
   *
   * @return A list of {@link Region} objects representing all applicable regions ordered by code.
   */
  @Override
  public List<Region> findAllApplicableByOrderByCode() {
    return repository.findAllByOrderByCode().stream()
        .filter(RegionDTO::isApplicability)
        .map(RegionTransformer::transformDTO)
        .toList();
  }
}
