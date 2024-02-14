package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CourtController.class)
@Import({SecurityConfig.class, TestConfig.class, DocumentNumberPatternProperties.class})
class CourtControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private CourtService service;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Test
  void testGetCourts() {
    when(service.getCourts(null)).thenReturn(new ArrayList<>());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCourts(null);
  }
}
