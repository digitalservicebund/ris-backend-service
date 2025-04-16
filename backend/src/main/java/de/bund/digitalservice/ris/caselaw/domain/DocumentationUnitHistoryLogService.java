package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationUnitHistoryLogService {
  private final DocumentationUnitHistoryLogRepository repository;
  private final UserService userService;

  public DocumentationUnitHistoryLogService(
      DocumentationUnitHistoryLogRepository repository, UserService userService) {
    this.repository = repository;
    this.userService = userService;
  }

  public List<HistoryLog> getHistoryLogs(UUID documentationUnitId, OidcUser oidcUser) {
    return repository.findByDocumentationUnitId(documentationUnitId, userService.getUser(oidcUser));
  }

  public HistoryLog saveUpdateHistoryLog(UUID documentationUnitId, User user) {
    UUID existingLogId =
        findUpdateHistoryLogForToday(documentationUnitId, user).map(HistoryLog::id).orElse(null);

    return repository.saveUpdateLog(existingLogId, documentationUnitId, user);
  }

  private Optional<HistoryLog> findUpdateHistoryLogForToday(UUID uuid, User user) {
    ZoneOffset zone = ZoneOffset.UTC;
    Instant startOfDay = LocalDate.now(zone).atStartOfDay().toInstant(zone);
    Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

    return repository.findUpdateLogForToday(uuid, user, startOfDay, endOfDay);
  }
}
