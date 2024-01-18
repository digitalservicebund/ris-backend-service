package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  private final WebClient webClient;

  public RisSearchController(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  @GetMapping(value = "")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> callRisSearchEndpoint(@RequestParam String query) {
    String url = "https://ris-search.dev.ds4g.net/v1/search";
    if (risSearchUsername.isEmpty() || risSearchPassword.isEmpty()) {
      url = "http://localhost:8090/v1/search";
    }
    url = UriComponentsBuilder.fromHttpUrl(url).queryParam("query", query).toUriString();
    return webClient
        .get()
        .uri(url)
        .headers(headers -> headers.setBasicAuth(risSearchUsername, risSearchPassword))
        .retrieve()
        .bodyToMono(String.class)
        .map(ResponseEntity::ok);
  }
}
