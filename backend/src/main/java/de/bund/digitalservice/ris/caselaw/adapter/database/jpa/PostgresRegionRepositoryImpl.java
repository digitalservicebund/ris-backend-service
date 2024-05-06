package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.RegionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.RegionRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresRegionRepositoryImpl implements RegionRepository {

  private final DatabaseRegionRepository repository;

  public PostgresRegionRepositoryImpl(DatabaseRegionRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Region> findApplicableBySearchStr(String searchString) {
    return repository
        .findAllByCodeStartsWithIgnoreCaseOrLongTextStartsWithIgnoreCase(searchString, searchString)
        .stream()
        .filter(RegionDTO::isApplicability)
        .map(RegionTransformer::transformDTO)
        .toList();
  }

  @Override
  public List<Region> findAllApplicableByOrderByCode() {
    return repository.findAllByOrderByCode().stream()
        .filter(RegionDTO::isApplicability)
        .map(RegionTransformer::transformDTO)
        .toList();
  }
}
