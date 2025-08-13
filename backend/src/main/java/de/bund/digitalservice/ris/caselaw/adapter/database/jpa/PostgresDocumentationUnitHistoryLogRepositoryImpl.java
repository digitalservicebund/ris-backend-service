package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.HistoryLogTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

    // --- 1. Collect all unique IDs for creators and process step users ---
    Set<UUID> allUserIds = new HashSet<>();
    Set<UUID> logIdsWithProcessSteps = new HashSet<>();

    historyLogDTOs.forEach(
        dto -> {
          allUserIds.add(dto.getUserId());
          if (dto.getEventType() == HistoryLogEventType.PROCESS_STEP
              || dto.getEventType() == HistoryLogEventType.PROCESS_STEP_USER) {
            logIdsWithProcessSteps.add(dto.getId());
          }
        });
    allUserIds.remove(null);

    // a) Fetch all mappings for the relevant logs in a single query
    List<HistoryLogDocumentationUnitProcessStepDTO> mappings =
        historyLogDocumentationUnitProcessStepRepository.findByHistoryLogIdIn(
            logIdsWithProcessSteps);
    Map<UUID, HistoryLogDocumentationUnitProcessStepDTO> mappingMap =
        mappings.stream().collect(Collectors.toMap(m -> m.getHistoryLog().getId(), m -> m));

    // b) Add from/to user IDs to the collection for batch fetching
    mappings.forEach(
        m -> {
          Optional.ofNullable(m.getToDocumentationUnitProcessStep())
              .map(DocumentationUnitProcessStepDTO::getUserId)
              .ifPresent(allUserIds::add);
          Optional.ofNullable(m.getFromDocumentationUnitProcessStep())
              .map(DocumentationUnitProcessStepDTO::getUserId)
              .ifPresent(allUserIds::add);
        });

    // --- 2. Fetch all User objects in a single batch query to the API ---
    Map<UUID, User> allUserMap = new HashMap<>();
    for (UUID userId : allUserIds) {
      allUserMap.put(userId, userService.getUser(userId));
    }

    // --- 3. Transform the DTOs and pass all the data to the transformer ---
    return historyLogDTOs.stream()
        .map(
            dto -> {
              // Get the user from the map for this specific history log
              User creatorUser = allUserMap.get(dto.getUserId());

              // Get the mapping for this log
              HistoryLogDocumentationUnitProcessStepDTO mapping = mappingMap.get(dto.getId());

              // Get the from and to users from the mapping
              User fromUser =
                  Optional.ofNullable(mapping)
                      .map(
                          HistoryLogDocumentationUnitProcessStepDTO
                              ::getFromDocumentationUnitProcessStep)
                      .map(DocumentationUnitProcessStepDTO::getUserId)
                      .map(allUserMap::get)
                      .orElse(null);

              User toUser =
                  Optional.ofNullable(mapping)
                      .map(
                          HistoryLogDocumentationUnitProcessStepDTO
                              ::getToDocumentationUnitProcessStep)
                      .map(DocumentationUnitProcessStepDTO::getUserId)
                      .map(allUserMap::get)
                      .orElse(null);

              // Pass all the necessary data to your transformer
              return HistoryLogTransformer.transformToDomain(
                  dto, currentUser, creatorUser, mapping, fromUser, toUser);
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
        .map(dto -> HistoryLogTransformer.transformToDomain(dto, user, user, null, null, null));
  }

  @Override
  public HistoryLogDTO saveHistoryLog(
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
    return databaseRepository.save(dto);
  }
}
