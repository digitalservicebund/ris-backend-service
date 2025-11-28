package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCodeService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.ArrayList;
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
@WebMvcTest(controllers = CurrencyCodeController.class)
@Import({SecurityConfig.class, TestConfig.class})
class CurrencyCodeControllerTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @MockitoBean private CurrencyCodeService service;

  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  @Test
  void testGetCurrencyCodesWithoutSize() {
    when(service.getCurrencyCodes(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/currencycodes")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCurrencyCodes(null, 200);
  }

  @Test
  void testGetCurrencyCodesWithSize() {
    when(service.getCurrencyCodes(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/currencycodes?sz=100")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCurrencyCodes(null, 100);
  }

  @Test
  void testGetCurrencyCodesWithQuery() {
    when(service.getCurrencyCodes(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/currencycodes?q=test")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCurrencyCodes("test", 200);
  }
}
