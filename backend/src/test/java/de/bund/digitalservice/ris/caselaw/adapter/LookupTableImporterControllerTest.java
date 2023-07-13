package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = LookupTableImporterController.class)
@Import({SecurityConfig.class, TestConfig.class})
class LookupTableImporterControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private LookupTableImporterService service;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private JdbcTemplate jdbcTemplate;

  @Test
  void testImportDocumentTypeLookupTable() {
    when(service.importDocumentTypeLookupTable(any(ByteBuffer.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/doktyp")
        .bodyValue(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).importDocumentTypeLookupTable(any(ByteBuffer.class));
  }

  @Test
  void testImportCourtLookupTable() {
    when(service.importCourtLookupTable(any(ByteBuffer.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/gerichtdata")
        .bodyValue(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).importCourtLookupTable(any(ByteBuffer.class));
  }

  @Test
  void testImportStateLookupTable() {
    when(service.importStateLookupTable(any(ByteBuffer.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/buland")
        .bodyValue(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).importStateLookupTable(any(ByteBuffer.class));
  }

  @Test
  void testImportCitationStyleLookupTable() {
    when(service.importCitationStyleLookupTable(any(ByteBuffer.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/zitart")
        .bodyValue(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).importCitationStyleLookupTable(any(ByteBuffer.class));
  }

  @Test
  void testImportFieldOfLawLookupTable() {
    when(service.importFieldOfLawLookupTable(any(ByteBuffer.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/fieldOfLaw")
        .bodyValue(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).importFieldOfLawLookupTable(any(ByteBuffer.class));
  }
}
