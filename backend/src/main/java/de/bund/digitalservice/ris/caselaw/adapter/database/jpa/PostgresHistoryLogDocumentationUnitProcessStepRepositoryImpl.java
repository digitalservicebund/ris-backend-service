package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogDocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogDocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogDocumentationUnitProcessStepRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresHistoryLogDocumentationUnitProcessStepRepositoryImpl
    implements HistoryLogDocumentationUnitProcessStepRepository {

  private final DatabaseHistoryLogDocumentationUnitProcessStepRepository databaseRepository;

  public PostgresHistoryLogDocumentationUnitProcessStepRepositoryImpl(
      DatabaseHistoryLogDocumentationUnitProcessStepRepository databaseRepository) {
    this.databaseRepository = databaseRepository;
  }

  @Override
  public Optional<HistoryLogDocumentationUnitProcessStep> findByHistoryLogId(UUID historyLogId) {
    return databaseRepository
        .findByHistoryLogId(historyLogId)
        .map(HistoryLogDocumentationUnitProcessStepTransformer::toDomain);
  }
}
