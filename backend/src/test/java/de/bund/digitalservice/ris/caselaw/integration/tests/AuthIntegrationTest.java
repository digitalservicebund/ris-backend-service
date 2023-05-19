package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import de.bund.digitalservice.ris.caselaw.adapter.AuthController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      FlywayConfig.class,
      PostgresConfig.class,
      KeycloakUserService.class,
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

  @Autowired private WebTestClient webClient;

  private OidcLoginMutator getMockLogin() {
    return mockOidcLogin()
        .idToken(
            token ->
                token.claims(
                    claims -> {
                      claims.put("groups", Collections.singletonList("/DigitalService"));
                      claims.put("name", "testUser");
                    }));
  }

  @Test
  void testGetUser() {
    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLogin())
        .get()
        .uri("/api/v1/auth/me")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(User.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().name()).isEqualTo("testUser");
              assertThat(response.getResponseBody().documentationOfficeLabel())
                  .isEqualTo("DigitalService");
            });
  }
}
