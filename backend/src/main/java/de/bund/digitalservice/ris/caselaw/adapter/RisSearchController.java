package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/search")
@Slf4j
public class RisSearchController {

  @Value("${ris-search.url:http://localhost:8090/v1/search}")
  private String risSearchUrl;

  @Value("${ris-search.basic-auth.username:}")
  private String risSearchUsername;

  @Value("${ris-search.basic-auth.password:}")
  private String risSearchPassword;

  private final UserService userService;
  private final WebClientService webClientService;

  public RisSearchController(UserService userService, WebClientService webClientService) {
    this.userService = userService;
    this.webClientService = webClientService;
  }

  @GetMapping(value = "")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> callRisSearchEndpoint(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam String query,
      @RequestParam String sz,
      @RequestParam String pg) {
    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice ->
                webClientService.callExternalService(
                    buildUrl(query, sz, pg, documentationOffice.abbreviation()),
                    risSearchUsername,
                    risSearchPassword));
  }

  private String buildUrl(String query, String sz, String pg, String docOfficeAbbreviation) {
    return UriComponentsBuilder.fromHttpUrl(risSearchUrl)
        .queryParam("query", query)
        .queryParam("sz", sz)
        .queryParam("pg", pg)
        .queryParam("documentationOfficeAbbreviation", docOfficeAbbreviation)
        .toUriString();
  }
}
