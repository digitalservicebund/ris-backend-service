package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.RisSearchWebClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = RisSearchController.class)
@Import({SecurityConfig.class, AuthService.class, TestConfig.class})
class RisSearchControllerTest {

  @Autowired private RisWebTestClient risWebClient;
  @MockBean private DocumentUnitService documentUnitService;
  @MockBean private KeycloakUserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private RisSearchWebClientService webClientService;
  @MockBean DatabaseApiKeyRepository apiKeyRepository;
  @MockBean DatabaseDocumentationOfficeRepository officeRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @Test
  void endpointToPassOnStatusAndContentWithoutChangingIt_shouldBeOk() {
    ResponseEntity<String> mockResponse = ResponseEntity.ok("some content");

    when(userService.getDocumentationOffice(any())).thenReturn(Mono.just(docOffice));

    when(webClientService.callEndpoint(anyString(), anyInt(), anyInt(), anyString(), any()))
        .thenReturn(Mono.just(mockResponse));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/search?query=test123&sz=100&pg=0&sort=default")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("some content");
  }

  @Test
  void endpointToPassOnStatusAndContentWithoutChangingIt_shouldBeBad() {
    when(userService.getDocumentationOffice(any())).thenReturn(Mono.just(docOffice));

    ResponseEntity<String> mockResponse = ResponseEntity.badRequest().body("some content");
    when(webClientService.callEndpoint(anyString(), anyInt(), anyInt(), anyString(), any()))
        .thenReturn(Mono.just(mockResponse));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/search?query=test123&sz=100&pg=0&sort=default")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .isEqualTo("some content");
  }
}
