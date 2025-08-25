package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  /**
   * Saves a history log entry specifically for process step changes, including from/to user
   * context. This method now accepts domain objects for process steps.
   *
   * @param documentationUnitId The UUID of the documentation unit.
   * @param user The user performing the action (can be null for system actions).
   * @param systemName Or a sysmtem name instead of a user, if step was set by the system
   * @param eventType The type of history log event (e.g., PROCESS_STEP_USER).
   * @param description A description of the event.
   * @param fromStep The previous process step (domain object).
   * @param toStep The new process step (domain object).
   */
  @Transactional
  public void saveProcessStepHistoryLog(
      UUID documentationUnitId,
      @Nullable User user,
      String systemName,
      HistoryLogEventType eventType,
      String description,
      @Nullable DocumentationUnitProcessStep fromStep,
      @Nullable DocumentationUnitProcessStep toStep) {

    repository.saveProcessStepHistoryLog(
        documentationUnitId, user, systemName, eventType, description, fromStep, toStep);
  }

  private Optional<HistoryLog> findUpdateHistoryLogForToday(UUID uuid, User user) {
    Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
    Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

    return repository.findUpdateLogForDuration(uuid, user, startOfDay, endOfDay);
  }
}
