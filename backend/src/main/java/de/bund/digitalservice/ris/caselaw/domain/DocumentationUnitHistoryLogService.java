package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationUnitHistoryLogService {
  private final DocumentationUnitHistoryLogRepository repository;

  public DocumentationUnitHistoryLogService(DocumentationUnitHistoryLogRepository repository) {
    this.repository = repository;
  }

  public List<HistoryLog> getHistoryLogs(UUID documentationUnitId, User user) {
    return repository.findByDocumentationUnitId(documentationUnitId, user);
  }

  public void saveHistoryLog(
      UUID documentationUnitId,
      @Nullable User user,
      HistoryLogEventType eventType,
      String description) {
    UUID existingLogId = null;
    // accumulate UPDATE logs per day
    if (eventType == HistoryLogEventType.UPDATE) {
      existingLogId =
          findUpdateHistoryLogForToday(documentationUnitId, user).map(HistoryLog::id).orElse(null);
    }

    repository.saveHistoryLog(existingLogId, documentationUnitId, user, eventType, description);
  }

  private Optional<HistoryLog> findUpdateHistoryLogForToday(UUID uuid, User user) {
    Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
    Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

    return repository.findUpdateLogForDuration(uuid, user, startOfDay, endOfDay);
  }
}
