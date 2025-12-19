package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustryService;
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
@WebMvcTest(controllers = CollectiveAgreementIndustryController.class)
@Import({SecurityConfig.class, TestConfig.class})
class CollectiveAgreementIndustryControllerTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @MockitoBean private CollectiveAgreementIndustryService service;

  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  @Test
  void testGetCollectiveAgreementIndustries() {
    when(service.getCollectiveAgreementIndustries(anyString())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/collective-agreement-industries")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCollectiveAgreementIndustries(null);
  }

  @Test
  void testGetCollectiveAgreementIndustriesWithQuery() {
    when(service.getCollectiveAgreementIndustries(anyString())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/collective-agreement-industries?q=test")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCollectiveAgreementIndustries("test");
  }
}
