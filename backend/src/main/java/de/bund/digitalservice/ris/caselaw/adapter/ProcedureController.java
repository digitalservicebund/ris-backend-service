package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/procedure")
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
public class ProcedureController {

  private final ProcedureService service;
  private final UserService userService;

  public ProcedureController(ProcedureService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Flux<Procedure> getAllProcedures(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> query) {
    return Flux.fromIterable(
        service.search(query, userService.getDocumentationOffice(oidcUser).block()));
  }
}
