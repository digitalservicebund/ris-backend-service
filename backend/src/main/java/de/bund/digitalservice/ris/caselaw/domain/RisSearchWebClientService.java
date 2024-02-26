package de.bund.digitalservice.ris.caselaw.domain;

import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RisSearchWebClientService {

  @Value("${ris-search.url:http://localhost:8090/v1/search}")
  private String risSearchUrl;

  @Value("${ris-search.basic-auth.username:}")
  private String risSearchUsername;

  @Value("${ris-search.basic-auth.password:}")
  private String risSearchPassword;

  private final WebClient webClient;

  public RisSearchWebClientService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public Mono<ResponseEntity<String>> callEndpoint(
      String query, int sz, int pg, DocumentationOffice documentationOffice) {
    AtomicReference<HttpStatusCode> statusCode = new AtomicReference<>();

    return webClient
        .get()
        .uri(buildUrl(query, sz, pg, documentationOffice.abbreviation()))
        .headers(headers -> headers.setBasicAuth(risSearchUsername, risSearchPassword))
        .retrieve()
        .onStatus(
            status -> true,
            clientResponse -> {
              statusCode.set(clientResponse.statusCode());
              return Mono.empty();
            })
        .bodyToMono(String.class)
        .map(responseBody -> ResponseEntity.status(statusCode.get()).body(responseBody));
  }

  private String buildUrl(String query, int sz, int pg, String docOfficeAbbreviation) {
    return UriComponentsBuilder.fromHttpUrl(risSearchUrl)
        .queryParam("query", query)
        .queryParam("sz", sz)
        .queryParam("pg", pg)
        .queryParam("documentationOfficeAbbreviation", docOfficeAbbreviation)
        .toUriString();
  }
}
