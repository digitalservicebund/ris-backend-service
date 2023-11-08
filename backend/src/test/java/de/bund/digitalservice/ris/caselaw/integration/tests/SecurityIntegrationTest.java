package de.bund.digitalservice.ris.caselaw.integration.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com"
    })
@Tag("integration")
class SecurityIntegrationTest {

  @Container
  static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14")
          .withInitScript("db/create_migration_scheme_and_extensions.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired WebTestClient webTestClient;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Test
  void shouldHaveEnabledCSPHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals(
            "Content-Security-Policy",
            "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-eval'; connect-src 'self' *.sentry.io data:");
  }

  @Test
  void shouldHaveEnabledXFrameOptionsHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("X-Frame-Options", "SAMEORIGIN");
  }

  @Test
  void shouldHaveEnabledXContentTypeOptionsHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("X-Content-Type-Options", "nosniff");
  }

  @Test
  void shouldHaveEnabledReferrerPolicyHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  @Test
  void shouldHaveEnabledPermissionsPolicyHeader() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals(
            "Permissions-Policy",
            "accelerometer=(), ambient-light-sensor=(), autoplay=(), battery=(), camera=(), cross-origin-isolated=(), "
                + "display-capture=(), document-domain=(), encrypted-media=(), execution-while-not-rendered=(), "
                + "execution-while-out-of-viewport=(), fullscreen=(), geolocation=(), gyroscope=(), keyboard-map=(), "
                + "magnetometer=(), microphone=(), midi=(), navigation-override=(), payment=(), picture-in-picture=(), "
                + "publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=(), "
                + "clipboard-read=(self), clipboard-write=(self), gamepad=(), speaker-selection=(), conversion-measurement=(), "
                + "focus-without-user-activation=(self), hid=(), idle-detection=(), interest-cohort=(), serial=(), sync-script=(), "
                + "trust-token-redemption=(), window-placement=(), vertical-scroll=(self)");
  }
}
