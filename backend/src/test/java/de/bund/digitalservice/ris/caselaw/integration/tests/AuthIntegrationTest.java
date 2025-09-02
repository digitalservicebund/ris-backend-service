package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @Test
  void testGetUser() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/auth/me")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(User.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().name()).isEqualTo("test User");
              assertThat(response.getResponseBody().documentationOffice().abbreviation())
                  .isEqualTo("DS");
            });
  }

  @Test
  void testGetUserForOtherOffice() {
    risWebTestClient
        .withLogin("/CC-RIS")
        .get()
        .uri("/api/v1/auth/me")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(User.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().name()).isEqualTo("test User");
              assertThat(response.getResponseBody().documentationOffice().abbreviation())
                  .isEqualTo("CC-RIS");
            });
  }

  @Test
  void testGetUserNameWithoutKnownGroup() {
    risWebTestClient
        .withLogin("foo")
        .get()
        .uri("/api/v1/auth/me")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
