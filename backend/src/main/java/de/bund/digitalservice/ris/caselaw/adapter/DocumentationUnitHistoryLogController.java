package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{documentationUnitId}/historylogs")
public class DocumentationUnitHistoryLogController {
  private final DocumentationUnitHistoryLogService service;

  public DocumentationUnitHistoryLogController(DocumentationUnitHistoryLogService service) {
    this.service = service;
  }

  /**
   * Get all history logs of a documentation unit
   *
   * @param documentationUnitId UUID of the documentation unit
   * @return ordered list of history records (newest first)
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#documentationUnitId)")
  public List<HistoryLog> getHistoryLog(
      User user,
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID documentationUnitId) {
    return service.getHistoryLogs(documentationUnitId, user);
  }
}
