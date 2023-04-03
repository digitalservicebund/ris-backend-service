package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresCourtRepositoryImpl implements CourtRepository {
  private final DatabaseCourtRepository repository;

  public PostgresCourtRepositoryImpl(DatabaseCourtRepository repository) {
    this.repository = repository;
  }

  @Override
  public Flux<Court> findBySearchStr(String searchString) {
    return repository.findBySearchStr(searchString).map(CourtTransformer::transformDTO);
  }

  @Override
  public Flux<Court> findAllByOrderByCourttypeAscCourtlocationAsc() {
    return repository
        .findAllByOrderByCourttypeAscCourtlocationAsc()
        .map(CourtTransformer::transformDTO);
  }
}
