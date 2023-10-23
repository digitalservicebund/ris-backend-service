package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCourtRepositoryImpl implements CourtRepository {

  private final DatabaseCourtRepository repository;

  public PostgresCourtRepositoryImpl(DatabaseCourtRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Court> findBySearchStr(String searchString) {
    return repository.findBySearchStr(searchString).stream()
        .map(CourtTransformer::transformDTO)
        .toList();
  }

  @Override
  public List<Court> findAllByOrderByTypeAscLocationAsc() {
    return repository.findAllByOrderByTypeAscLocationAsc().stream()
        .map(CourtTransformer::transformDTO)
        .toList();
  }
}
