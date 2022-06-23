package de.bund.digitalservice.ris;

import java.net.MalformedURLException;
import java.net.URL;
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

  @Value("${application.staging.url}")
  private String stagingUrl;

  @Test
  void applicationHealthTest() throws MalformedURLException {
    URL url = new URL(stagingUrl);

    WebTestClient.bindToServer()
        .baseUrl(plainBaseUrl(url))
        .build()
        .get()
        .uri("/actuator/health")
        .headers(headers -> headers.setBasicAuth(userInfo(url)[0], userInfo(url)[1]))
        .exchange()
        .expectStatus()
        .isOk();
  }

  private String plainBaseUrl(URL url) {
    if (url.getPort() > 0) {
      return String.format("%s://%s:%s", url.getProtocol(), url.getHost(), url.getPort());
    }
    return String.format("%s://%s", url.getProtocol(), url.getHost());
  }

  private String[] userInfo(URL url) {
    return url.getUserInfo().split(":");
  }
}
