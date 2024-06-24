package de.bund.digitalservice.ris.caselaw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestConfiguration
public class TestConfig {

  @Autowired private WebTestClient webTestClient;

  @Bean
  public RisWebTestClient risWebTestClient() {
    return new RisWebTestClient(webTestClient);
  }
}
