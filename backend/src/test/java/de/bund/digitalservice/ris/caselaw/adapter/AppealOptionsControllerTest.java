package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AppealOptionsController.class)
@Import({SecurityConfig.class, TestConfig.class, DocumentNumberPatternConfig.class})
class AppealOptionsControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @MockitoBean private AppealOptionsService appealOptionsService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  @Test
  void testGetAppellantOptions() {
    var appellants =
        List.of(
            Appellant.builder().value("Kläger").build(),
            Appellant.builder().value("Beklagter").build());
    when(appealOptionsService.getAppellantOptions()).thenReturn(appellants);

    var result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/appealoptions/appellants")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<Appellant>>() {})
            .returnResult();

    assertThat(result.getResponseBody()).isEqualTo(appellants);
  }

  @Test
  void testGetAppealStatusOptions() {
    var statuses =
        List.of(
            AppealStatus.builder().value("zulässig").build(),
            AppealStatus.builder().value("unbegründet").build());
    when(appealOptionsService.getAppealStatusOptions()).thenReturn(statuses);

    var result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/appealoptions/statuses")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<AppealStatus>>() {})
            .returnResult();

    assertThat(result.getResponseBody()).isEqualTo(statuses);
  }
}
