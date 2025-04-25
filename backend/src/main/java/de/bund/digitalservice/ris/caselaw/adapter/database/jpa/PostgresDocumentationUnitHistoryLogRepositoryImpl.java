package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
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
  public Optional<HistoryLog> findUpdateLogForDuration(
      UUID documentationUnitId, @Nullable User user, Instant start, Instant end) {
    String userName = Optional.ofNullable(user).map(User::name).orElse(null);
    return databaseRepository
        .findFirstByDocumentationUnitIdAndUserNameAndEventTypeAndCreatedAtBetween(
            documentationUnitId, userName, HistoryLogEventType.UPDATE, start, end)
        .map(dto -> HistoryLogTransformer.transformToDomain(dto, user));
  }

  @Override
  public void saveHistoryLog(
      UUID existingId,
      UUID documentationUnitId,
      @Nullable User user,
      HistoryLogEventType eventType,
      String description) {
    String userName = null;
    String systemName = null;
    DocumentationOffice office = null;
    UUID userId = null;

    if (user != null) {
      userName = user.name();
      office = user.documentationOffice();
      userId = user.id();
    } else {
      systemName = "NeuRIS";
    }
    UUID historyLogId = null;
    if (existingId != null) {
      historyLogId = existingId;
    }
    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(historyLogId)
            .createdAt(Instant.now())
            .documentationUnitId(documentationUnitId)
            .documentationOffice(DocumentationOfficeTransformer.transformToDTO(office))
            .userId(userId)
            .userName(userName)
            .systemName(systemName)
            .description(description)
            .eventType(eventType)
            .build();
    databaseRepository.save(dto);
  }
}
