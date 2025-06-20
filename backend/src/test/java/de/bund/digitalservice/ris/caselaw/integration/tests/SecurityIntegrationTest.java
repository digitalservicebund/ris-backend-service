package de.bund.digitalservice.ris.caselaw.integration.tests;

import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SecurityIntegrationTest extends BaseIntegrationTest {

  @Autowired RisWebTestClient webTestClient;

  @Test
  void shouldHaveEnabledCSPHeader() {
    webTestClient
        .withDefaultLogin()
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
        .withDefaultLogin()
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("X-Frame-Options", "SAMEORIGIN");
  }

  @Test
  void shouldHaveEnabledXContentTypeOptionsHeader() {
    webTestClient
        .withDefaultLogin()
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("X-Content-Type-Options", "nosniff");
  }

  @Test
  void shouldHaveEnabledReferrerPolicyHeader() {
    webTestClient
        .withDefaultLogin()
        .get()
        .uri("/")
        .exchange()
        .expectHeader()
        .valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  @Test
  void shouldHaveEnabledPermissionsPolicyHeader() {
    webTestClient
        .withDefaultLogin()
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
