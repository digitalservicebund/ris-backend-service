package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
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
@WebMvcTest(controllers = CourtController.class)
@Import({SecurityConfig.class, TestConfig.class, DocumentNumberPatternConfig.class})
class CourtControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockitoBean private CourtService service;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean private UserService userService;

  @Test
  void testGetCourtsWithoutSize() {
    when(service.getCourts(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCourts(null, 200);
  }

  @Test
  void testGetCourtsWithSize() {
    when(service.getCourts(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts?sz=100")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCourts(null, 100);
  }

  @Test
  void testGetCourtsWithQuery() {
    when(service.getCourts(anyString(), anyInt())).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts?q=test")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCourts("test", 200);
  }
}
