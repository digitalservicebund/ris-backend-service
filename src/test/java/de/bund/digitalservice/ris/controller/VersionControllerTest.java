package de.bund.digitalservice.ris.controller;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class VersionControllerTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldGetVersionInfo() {
    this.webTestClient
        .get()
        .uri("/api/v1/version")
        .exchange()
        .expectBody()
        .jsonPath("$.version")
        .isEqualTo("0.0.1")
        .jsonPath("$.commitSHA")
        .exists();
  }
}
