package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.RisSearchWebClientService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/search")
@Slf4j
public class RisSearchController {

  private final UserService userService;
  private final RisSearchWebClientService risSearchWebClientService;

  public RisSearchController(
      UserService userService, RisSearchWebClientService risSearchWebClientService) {
    this.userService = userService;
    this.risSearchWebClientService = risSearchWebClientService;
  }

  @GetMapping(value = "")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> callRisSearchEndpoint(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam String query,
      @RequestParam int sz,
      @RequestParam int pg,
      @RequestParam String sort) {
    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice ->
                risSearchWebClientService.callEndpoint(query, sz, pg, sort, documentationOffice));
  }
}
