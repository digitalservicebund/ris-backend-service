package de.bund.digitalservice.ris;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage"
    })
@Tag("integration")
class HealthEndpointIntegrationTest {

  @Autowired WebTestClient webTestClient;

  @Test
  void shouldExposeHealthEndpoint() {
    webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
  }
}
