package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

  @Override
  public Optional<HistoryLog> findUpdateLogForToday(
      UUID documentationUnitId, User user, Instant start, Instant end) {
    return databaseRepository
        .findFirstByDocumentationUnitIdAndUserNameAndEventTypeAndCreatedAtBetween(
            documentationUnitId, user.name(), HistoryLogEventType.UPDATE, start, end)
        .map(dto -> HistoryLogTransformer.transformToDomain(dto, user));
  }

  @Override
  public HistoryLog saveUpdateLog(UUID existingId, UUID documentationUnitId, User user) {
    UUID historyLogId = UUID.randomUUID();
    if (existingId != null) {
      historyLogId = existingId;
    }
    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(historyLogId)
            .createdAt(Instant.now())
            .documentationUnitId(documentationUnitId)
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDTO(user.documentationOffice()))
            .userId(user.id())
            .userName(user.name())
            .description("Dokumentationseinheit wurde aktualisiert")
            .eventType(HistoryLogEventType.UPDATE)
            .build();
    return HistoryLogTransformer.transformToDomain(databaseRepository.save(dto), user);
  }
}
