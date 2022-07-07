package de.bund.digitalservice.ris;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@Tag("journey")
class APIJourneyTest {

  @Value("${application.staging.password}")
  private String stagingPassword;

  @Value("${application.staging.url}")
  private String stagingUrl;

  @Value("${application.staging.user}")
  private String stagingUser;

  @Test
  void docUnitCreationAPITest() {
    WebTestClient.bindToServer()
        .baseUrl(stagingUrl)
        .build()
        .post()
        .uri("/api/v1/docunits")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"documentationCenterAbbreviation\":\"foo\",\"documentType\":\"X\"}")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$['uuid']")
        .exists();
  }
}
