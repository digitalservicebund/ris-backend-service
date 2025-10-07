package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCourtRepositoryImpl implements CourtRepository {

  private final DatabaseCourtRepository repository;

  public PostgresCourtRepositoryImpl(DatabaseCourtRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public List<Court> findBySearchStr(String searchString, Integer size) {
    return repository.findBySearchStr(searchString, size).stream()
        .map(CourtTransformer::transformToDomain)
        .toList();
  }

  @Override
  @Transactional
  public Optional<Court> findByTypeAndLocation(String type, String location) {
    if (type == null) {
      return Optional.empty();
    }
    if (location == null) {
      return repository.findOneByType(type).map(CourtTransformer::transformToDomain);
    }
    return repository
        .findOneByTypeAndLocation(type, location)
        .map(CourtTransformer::transformToDomain);
  }

  @Override
  @Transactional
  public Optional<Court> findUniqueBySearchString(String searchString) {
    List<CourtDTO> foundCourts = repository.findByExactSearchString(searchString);

    if (foundCourts.size() == 1) {
      return Optional.of(CourtTransformer.transformToDomain(foundCourts.get(0)));
    }
    return Optional.empty();
  }

  @Override
  @Transactional
  public List<Court> findAllByOrderByTypeAscLocationAsc(Integer size) {
    return repository.findByOrderByTypeAscLocationAsc(Limit.of(size)).stream()
        .map(CourtTransformer::transformToDomain)
        .toList();
  }
}
