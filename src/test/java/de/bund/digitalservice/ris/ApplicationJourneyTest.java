package de.bund.digitalservice.ris;

import java.net.MalformedURLException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@Tag("journey")
@TestPropertySource(locations = "classpath:application.properties")
class ApplicationJourneyTest {

  @Value("${application.staging.password}")
  private String stagingPassword;

  @Value("${application.staging.url}")
  private String stagingUrl;

  @Value("${application.staging.user}")
  private String stagingUser;

  @Test
  void applicationHealthTest() throws MalformedURLException {
    WebTestClient.bindToServer()
        .baseUrl(stagingUrl)
        .build()
        .get()
        .uri("/actuator/health")
        .headers(headers -> headers.setBasicAuth(stagingUser, stagingPassword))
        .exchange()
        .expectStatus()
        .isOk();
  }
}
