package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthController;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      FlywayConfig.class,
      PostgresConfig.class,
      KeycloakUserService.class,
      SecurityConfig.class,
      TestConfig.class,
      AuthService.class
    },
    controllers = {AuthController.class})
class AuthIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean DocumentUnitService documentUnitService;

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
              assertThat(response.getResponseBody().name()).isEqualTo("testUser");
              assertThat(response.getResponseBody().documentationOffice().label())
                  .isEqualTo("DigitalService");
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
              assertThat(response.getResponseBody().name()).isEqualTo("testUser");
              assertThat(response.getResponseBody().documentationOffice().label())
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
        .isOk()
        .expectBody(User.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().name()).isEqualTo("testUser"));
  }
}
