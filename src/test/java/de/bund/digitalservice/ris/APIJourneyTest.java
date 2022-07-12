package de.bund.digitalservice.ris;

import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

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
        .headers(headers -> headers.setBasicAuth(stagingUser, stagingPassword))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"documentationCenterAbbreviation\":\"foo\",\"documentType\":\"X\"}")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$['uuid']")
        .exists();
  }

  @Test
  void docUnitFileUploadAPITest() {
    // Create documentation unit
    DocUnitResponse response =
        WebTestClient.bindToServer()
            .baseUrl(stagingUrl)
            .build()
            .post()
            .uri("/api/v1/docunits")
            .headers(headers -> headers.setBasicAuth(stagingUser, stagingPassword))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"documentationCenterAbbreviation\":\"foo\",\"documentType\":\"X\"}")
            .exchange()
            .returnResult(DocUnitResponse.class)
            .getResponseBody()
            .blockFirst();

    // Attach docx file
    WebTestClient.bindToServer()
        .baseUrl(stagingUrl)
        .build()
        .put()
        .uri("/api/v1/docunits/{uuid}/file", response.uuid())
        .headers(headers -> headers.setBasicAuth(stagingUser, stagingPassword))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            BodyInserters.fromResource(
                new PathResource(
                    Paths.get("src", "test", "resources", "fixtures", "attachment.docx"))))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  static record DocUnitResponse(UUID uuid) {}
}
