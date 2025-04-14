package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresDocumentationUnitHistoryLogRepositoryImpl
    implements DocumentationUnitHistoryLogRepository {

  private final DatabaseDocumentationUnitHistoryLogRepository databaseRepository;

  public PostgresDocumentationUnitHistoryLogRepositoryImpl(
      DatabaseDocumentationUnitHistoryLogRepository databaseRepository) {
    this.databaseRepository = databaseRepository;
  }

  @Override
  public List<HistoryLog> findByDocumentationUnitId(UUID documentationUnitId, @Nullable User user) {
    return databaseRepository
        .findByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId)
        .stream()
        .map(historyLogDTO -> HistoryLogTransformer.transformToDomain(historyLogDTO, user))
        .toList();
  }
}
