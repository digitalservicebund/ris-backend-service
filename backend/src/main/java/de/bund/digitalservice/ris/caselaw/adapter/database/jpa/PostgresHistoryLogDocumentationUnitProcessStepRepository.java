package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.HistoryLogDocumentationUnitProcessStepRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresHistoryLogDocumentationUnitProcessStepRepository
    implements HistoryLogDocumentationUnitProcessStepRepository {

  private final DatabaseHistoryLogDocumentationUnitProcessStepRepository repository;

  public PostgresHistoryLogDocumentationUnitProcessStepRepository(
      DatabaseHistoryLogDocumentationUnitProcessStepRepository jpaRepository) {
    this.repository = jpaRepository;
  }

  @Override
  public void save(HistoryLogDocumentationUnitProcessStepDTO mappingDto) {
    repository.save(mappingDto);
  }
}
