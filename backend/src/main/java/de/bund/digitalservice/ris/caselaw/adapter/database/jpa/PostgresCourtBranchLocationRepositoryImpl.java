package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.court.CourtBranchLocationRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCourtBranchLocationRepositoryImpl implements CourtBranchLocationRepository {

  private final DatabaseCourtBranchLocationRepository repository;

  public PostgresCourtBranchLocationRepositoryImpl(
      DatabaseCourtBranchLocationRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public List<String> findAllByCourtId(UUID courtId) {
    return repository.findAllByCourtId(courtId).stream()
        .map(CourtBranchLocationDTO::getValue)
        .toList();
  }
}
