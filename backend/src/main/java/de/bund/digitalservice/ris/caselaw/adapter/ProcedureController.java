package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  @GetMapping()
  @PreAuthorize("isAuthenticated()")
  public Flux<Procedure> search(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> query,
      @RequestParam(value = "sz") Integer size) {
    return service.search(
        query,
        userService.getDocumentationOffice(oidcUser).block(),
        PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  @GetMapping("searchWithDocumentUnits")
  @PreAuthorize("isAuthenticated()")
  public Mono<Page<Procedure>> searchWithDocumentUnits(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> query,
      @RequestParam(value = "sz") Integer size,
      @RequestParam(value = "pg") Integer page) {
    return Mono.just(
        service.searchWithDocumentUnits(
            query,
            userService.getDocumentationOffice(oidcUser).block(),
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
  }
}
