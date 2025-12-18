package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FeatureToggleController.class)
@Import({SecurityConfig.class, TestConfig.class})
class FeatureToggleControllerTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @MockitoBean private FeatureToggleService service;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  @Test
  void shouldReturnTrueForEnabledFlag() {
    when(service.isEnabled("enabled_flag")).thenReturn(true);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/feature-toggles/enabled_flag")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Boolean.class)
        .consumeWith(result -> assertThat(result.getResponseBody()).isTrue());

    verify(service, times(1)).isEnabled("enabled_flag");
  }

  @Test
  void shouldReturnFalseForDisabledFlag() {
    when(service.isEnabled("disabled_flag")).thenReturn(false);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/feature-toggles/disabled_flag")
        .bodyValue(true)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Boolean.class)
        .consumeWith(result -> assertThat(result.getResponseBody()).isFalse());

    verify(service, times(1)).isEnabled("disabled_flag");
  }
}
