package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
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

  public List<HistoryLog> getHistoryLogs(UUID uuid, OidcUser oidcUser) {
    return repository.findByDocumentationUnitId(uuid, userService.getUser(oidcUser));
  }
}
