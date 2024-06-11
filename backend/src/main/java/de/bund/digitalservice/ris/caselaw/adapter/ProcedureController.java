package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/procedure")
public class ProcedureController {
  private final ProcedureService service;
  private final UserService userService;

  public ProcedureController(ProcedureService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  @GetMapping()
  @PreAuthorize("isAuthenticated()")
  public Mono<Page<Procedure>> search(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> query,
      @RequestParam(value = "pg") Optional<Integer> page,
      @RequestParam(value = "sz") Integer size) {
    return Mono.just(
        service.search(
            query,
            userService.getDocumentationOffice(oidcUser).block(),
            PageRequest.of(page.orElse(0), size)));
  }

  @GetMapping(value = "/{procedureUUID}/documentunits")
  @PreAuthorize("isAuthenticated()")
  public Flux<DocumentationUnitListItem> getDocumentUnits(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable UUID procedureUUID) {
    return Flux.fromIterable(service.getDocumentUnits(procedureUUID));
  }

  @DeleteMapping(value = "/{procedureUUID}")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<Void>> delete(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable UUID procedureUUID) {

    service.delete(procedureUUID);
    return Mono.just(ResponseEntity.ok().build());
  }
}
