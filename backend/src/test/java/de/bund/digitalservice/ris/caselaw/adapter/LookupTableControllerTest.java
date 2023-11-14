package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = LookupTableController.class)
@Import({SecurityConfig.class, TestConfig.class})
class LookupTableControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private LookupTableService service;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Test
  void testGetDocumentTypes() {
    when(service.getCaselawDocumentTypes(Optional.empty())).thenReturn(Flux.empty());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/lookuptable/documentTypes")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCaselawDocumentTypes(Optional.empty());
  }

  @Test
  void testGetCourts() {
    when(service.getCourts(Optional.empty())).thenReturn(Flux.empty());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/lookuptable/courts")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCourts(Optional.empty());
  }

  @Test
  void testGetCitationStyles() {
    when(service.getCitationStyles(Optional.empty())).thenReturn(Flux.empty());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/lookuptable/zitart")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getCitationStyles(Optional.empty());
  }
}
