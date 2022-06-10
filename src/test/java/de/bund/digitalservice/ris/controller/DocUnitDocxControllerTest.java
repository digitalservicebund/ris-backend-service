package de.bund.digitalservice.ris.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.service.DocxConverterService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocUnitDocxController.class)
@WithMockUser
@Tag("test")
class DocUnitDocxControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocxConverterService service;

  @Test
  void testGet() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/docunitdocx").exchange().expectStatus().isOk();

    verify(service).getDocxFiles();
  }

  @Test
  void testHtml() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunitdocx/123")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getHtml("123");
  }
}
