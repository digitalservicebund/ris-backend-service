package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebExceptionHandler;

@Configuration
public class ErrorHandlerConfig {

  @Bean
  public WebExceptionHandler forwardingExceptionHandler() {
    return new ForwardingWebExceptionHandler();
  }
}
