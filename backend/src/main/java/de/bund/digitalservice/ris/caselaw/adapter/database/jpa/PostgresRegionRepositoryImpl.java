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
  public List<Region> findBySearchStr(String searchString) {
    return repository
        .findAllByCodeStartsWithOrLongTextStartsWith(searchString, searchString)
        .stream()
        .map(RegionTransformer::transformDTO)
        .toList();
  }

  @Override
  public List<Region> findAllByOrderByCode() {
    return repository.findAllByOrderByCode().stream().map(RegionTransformer::transformDTO).toList();
  }
}
