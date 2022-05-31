package de.bund.digitalservice.ris.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.service.DocUnitService;
import java.nio.ByteBuffer;
import java.util.Objects;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocUnitController.class)
@WithMockUser
@Tag("test")
class DocUnitControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocUnitService service;

  @Captor private ArgumentCaptor<Flux<ByteBuffer>> fluxCaptor;

  @Test
  void testUploadFile() {
    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/docunits/")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).generateNewDocUnit(fluxCaptor.capture(), headersCaptor.capture());
    assertEquals(0, Objects.requireNonNull(fluxCaptor.getValue().blockFirst()).array().length);
    assertEquals(0, headersCaptor.getValue().getContentLength());
  }

  @Test
  void testGetAll() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/docunits/").exchange().expectStatus().isOk();

    verify(service).getAll();
  }

  @Test
  void testGetById() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/docunits/1").exchange().expectStatus().isOk();

    verify(service).getById(1);
  }
}
