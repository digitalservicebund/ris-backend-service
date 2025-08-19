package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgresDocumentationUnitHistoryLogRepositoryImpl
    implements DocumentationUnitHistoryLogRepository {

  private final DatabaseDocumentationUnitHistoryLogRepository databaseRepository;
  private final DatabaseHistoryLogDocumentationUnitProcessStepRepository
      historyLogDocumentationUnitProcessStepRepository;
  private final UserService userService;

  public PostgresDocumentationUnitHistoryLogRepositoryImpl(
      DatabaseDocumentationUnitHistoryLogRepository databaseRepository,
      UserService userService,
      DatabaseHistoryLogDocumentationUnitProcessStepRepository
          historyLogDocumentationUnitProcessStepRepository) {
    this.databaseRepository = databaseRepository;
    this.userService = userService;
    this.historyLogDocumentationUnitProcessStepRepository =
        historyLogDocumentationUnitProcessStepRepository;
  }

  @Transactional
  @Override
  public List<HistoryLog> findByDocumentationUnitId(
      UUID documentationUnitId, @Nullable User currentUser) {

    List<HistoryLogDTO> historyLogDTOs =
        databaseRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId);

    return historyLogDTOs.stream()
        .map(
            dto -> {
              User creatorUser = userService.getUser(dto.getUserId());
              User fromUser = null;
              User toUser = null;

              if (dto.getEventType() == HistoryLogEventType.PROCESS_STEP_USER) {
                HistoryLogDocumentationUnitProcessStepDTO historyLogProcessStepDTO =
                    historyLogDocumentationUnitProcessStepRepository
                        .findByHistoryLogId(dto.getId())
                        .orElse(null);
                if (historyLogProcessStepDTO != null) {
                  fromUser =
                      Optional.ofNullable(
                              historyLogProcessStepDTO.getFromDocumentationUnitProcessStep())
                          .map(DocumentationUnitProcessStepDTO::getUserId)
                          .map(userService::getUser)
                          .orElse(null);

                  toUser =
                      Optional.ofNullable(
                              historyLogProcessStepDTO.getToDocumentationUnitProcessStep())
                          .map(DocumentationUnitProcessStepDTO::getUserId)
                          .map(userService::getUser)
                          .orElse(null);
                }
              }

              return HistoryLogTransformer.transformToDomain(
                  dto, currentUser, creatorUser, fromUser, toUser);
            })
        .toList();
  }

  @Override
  public Optional<HistoryLog> findUpdateLogForDuration(
      UUID documentationUnitId, @Nullable User user, Instant start, Instant end) {
    UUID userId = Optional.ofNullable(user).map(User::id).orElse(null);

    if (userId == null) {
      return Optional.empty();
    }
    return databaseRepository
        .findFirstByDocumentationUnitIdAndUserIdAndEventTypeAndCreatedAtBetween(
            documentationUnitId, userId, HistoryLogEventType.UPDATE, start, end)
        .map(dto -> HistoryLogTransformer.transformToDomain(dto, user, user, null, null));
  }

  @Override
  public void saveHistoryLog(
      UUID existingId,
      UUID documentationUnitId,
      @Nullable User user,
      HistoryLogEventType eventType,
      String description) {
    String systemName = null;
    UUID userId = null;

    if (user != null) {
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
            .userId(userId)
            .systemName(systemName)
            .description(description)
            .eventType(eventType)
            .build();
    databaseRepository.save(dto);
  }

  @Transactional
  @Override
  public void saveProcessStepHistoryLog(
      UUID documentationUnitId,
      @Nullable User user,
      HistoryLogEventType eventType,
      String description,
      @Nullable DocumentationUnitProcessStep fromStep,
      @Nullable DocumentationUnitProcessStep toStep) {

    // Convert domain objects to DTOs for persistence
    DocumentationUnitProcessStepDTO fromStepDto =
        Optional.ofNullable(fromStep)
            .map(DocumentationUnitProcessStepTransformer::toDto)
            .orElse(null);
    DocumentationUnitProcessStepDTO toStepDto =
        Optional.ofNullable(toStep)
            .map(DocumentationUnitProcessStepTransformer::toDto)
            .orElse(null);

    HistoryLogDTO historyLogDTO =
        HistoryLogDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitId(documentationUnitId)
            .userId(user != null ? user.id() : null) // Handle null user for system logs
            .description(description)
            .eventType(eventType)
            .build();
    databaseRepository.save(historyLogDTO);

    HistoryLogDocumentationUnitProcessStepDTO historyLogDocumentationUnitProcessStepDTO =
        HistoryLogDocumentationUnitProcessStepDTO.builder()
            .createdAt(Instant.now())
            .historyLog(historyLogDTO)
            .toDocumentationUnitProcessStep(toStepDto)
            .fromDocumentationUnitProcessStep(fromStepDto)
            .build();

    historyLogDocumentationUnitProcessStepRepository.save(
        historyLogDocumentationUnitProcessStepDTO);
  }
}
