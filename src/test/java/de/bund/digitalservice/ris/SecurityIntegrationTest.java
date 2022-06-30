package de.bund.digitalservice.ris;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"otc.obs.bucket-name=testBucket", "otc.obs.url=testUrl"})
@Tag("integration")
class SecurityIntegrationTest {

  @Autowired WebTestClient webTestClient;

  @Test
  void shouldHaveEnabledCSPHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals(
            "Content-Security-Policy",
            "default-src 'self'; image-src 'self' data:; style-src 'self' 'unsafe-inline'");
  }
}
