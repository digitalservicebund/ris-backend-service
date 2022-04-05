package de.bund.digitalservice.template;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Tag("integration")
class HealthEndpointIntegrationTest {

  @Autowired WebTestClient webTestClient;

  @Test
  void shouldExposeHealthEndpoint() {
    webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
  }
}
