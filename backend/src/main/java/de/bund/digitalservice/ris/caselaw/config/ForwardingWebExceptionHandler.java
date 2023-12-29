package de.bund.digitalservice.ris.caselaw.config;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

public class ForwardingWebExceptionHandler implements WebExceptionHandler {

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (ex instanceof ResponseStatusException) {
      exchange.getResponse().setStatusCode(HttpStatus.FOUND);
      exchange.getResponse().getHeaders().setLocation(URI.create("/404.index"));
      return exchange.getResponse().setComplete();
    }
    return Mono.error(ex);
  }
}
