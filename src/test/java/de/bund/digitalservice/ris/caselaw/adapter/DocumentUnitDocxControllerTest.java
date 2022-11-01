package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.domain.DocxConverterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitDocxController.class)
@WithMockUser
class DocumentUnitDocxControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocxConverterService service;

  @Test
  void testGet() {
    when(service.getDocxFiles()).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunitdocx")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getDocxFiles();
  }

  @Test
  void testHtml() {
    when(service.getHtml("123")).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunitdocx/123")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getHtml("123");
  }
}
