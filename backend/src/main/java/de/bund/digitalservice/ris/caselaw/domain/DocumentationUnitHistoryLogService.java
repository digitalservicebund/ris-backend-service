package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDocumentationUnitProcessStepDTO;
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
  private final HistoryLogDocumentationUnitProcessStepRepository
      historyLogDocumentationUnitProcessStepRepository;

  public DocumentationUnitHistoryLogService(
      DocumentationUnitHistoryLogRepository repository,
      HistoryLogDocumentationUnitProcessStepRepository
          historyLogDocumentationUnitProcessStepRepository) {
    this.repository = repository;
    this.historyLogDocumentationUnitProcessStepRepository =
        historyLogDocumentationUnitProcessStepRepository;
  }

  public List<HistoryLog> getHistoryLogs(UUID documentationUnitId, User user) {
    return repository.findByDocumentationUnitId(documentationUnitId, user);
  }

  public HistoryLogDTO saveHistoryLog(
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

    return repository.saveHistoryLog(
        existingLogId, documentationUnitId, user, eventType, description);
  }

  @Transactional
  public void saveProcessStepHistoryLog(
      UUID documentationUnitId,
      @Nullable User user,
      HistoryLogEventType eventType,
      String description,
      @Nullable DocumentationUnitProcessStepDTO fromStepDto,
      @Nullable DocumentationUnitProcessStepDTO toStepDto) {

    HistoryLogDTO savedHistoryLogDto =
        saveHistoryLog(documentationUnitId, user, eventType, description);

    HistoryLogDocumentationUnitProcessStepDTO historyLogDocumentationUnitProcessStepDTO =
        HistoryLogDocumentationUnitProcessStepDTO.builder()
            .createdAt(Instant.now())
            .historyLog(savedHistoryLogDto)
            .toDocumentationUnitProcessStep(toStepDto)
            .fromDocumentationUnitProcessStep(fromStepDto)
            .build();

    historyLogDocumentationUnitProcessStepRepository.save(
        historyLogDocumentationUnitProcessStepDTO);
  }

  private Optional<HistoryLog> findUpdateHistoryLogForToday(UUID uuid, User user) {
    Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
    Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

    return repository.findUpdateLogForDuration(uuid, user, startOfDay, endOfDay);
  }
}
