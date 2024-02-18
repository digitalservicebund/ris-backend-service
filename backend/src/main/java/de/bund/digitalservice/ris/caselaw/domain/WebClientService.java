package de.bund.digitalservice.ris.caselaw.domain;

import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WebClientService {

  private final WebClient webClient;

  public WebClientService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public Mono<ResponseEntity<String>> callExternalService(
      String uri, String username, String password) {
    AtomicReference<HttpStatusCode> statusCode = new AtomicReference<>();

    return webClient
        .get()
        .uri(uri)
        .headers(headers -> headers.setBasicAuth(username, password))
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
}
