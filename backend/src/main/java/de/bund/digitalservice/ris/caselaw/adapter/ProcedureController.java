package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/procedure")
public class ProcedureController {
  private final ProcedureService service;
  private final UserService userService;

  public ProcedureController(ProcedureService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public Page<Procedure> search(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> query,
      @RequestParam(value = "pg") Optional<Integer> page,
      @RequestParam(value = "sz") Integer size) {
    return service.search(
        query, userService.getDocumentationOffice(oidcUser), PageRequest.of(page.orElse(0), size));
  }

  @GetMapping(value = "/{procedureUUID}/documentunits", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<DocumentationUnitListItem> getDocumentUnits(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable UUID procedureUUID) {
    return service.getDocumentUnits(procedureUUID);
  }

  @DeleteMapping(value = "/{procedureUUID}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable UUID procedureUUID) {

    service.delete(procedureUUID);
    return ResponseEntity.ok().build();
  }
}
