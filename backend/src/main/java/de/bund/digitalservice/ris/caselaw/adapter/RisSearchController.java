package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/search")
@Slf4j
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
public class RisSearchController {

  @Value("${ris-search.basic-auth.username:}")
  private String risSearchUsername;

  @Value("${ris-search.basic-auth.password:}")
  private String risSearchPassword;

  @Value("${ris-search.url:http://localhost:8090/v1/search}")
  private String risSearchUrl;

  private final UserService userService;
  private final WebClient webClient;

  public RisSearchController(UserService userService, WebClient.Builder webClientBuilder) {
    this.userService = userService;
    this.webClient = webClientBuilder.build();
  }

  @GetMapping(value = "")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> callRisSearchEndpoint(
      @AuthenticationPrincipal OidcUser oidcUser, @RequestParam String query) {
    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice ->
                webClient
                    .get()
                    .uri(buildUrl(query, documentationOffice.abbreviation()))
                    .headers(headers -> headers.setBasicAuth(risSearchUsername, risSearchPassword))
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(ResponseEntity::ok));
  }

  private String buildUrl(String query, String abbreviation) {
    return UriComponentsBuilder.fromHttpUrl(risSearchUrl)
        .queryParam("query", query)
        .queryParam("documentationOfficeAbbreviation", abbreviation)
        .toUriString();
  }
}
