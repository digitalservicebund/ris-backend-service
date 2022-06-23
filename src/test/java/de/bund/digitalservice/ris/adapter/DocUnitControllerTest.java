package de.bund.digitalservice.ris.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocUnitCreationInfo;
import de.bund.digitalservice.ris.domain.DocUnitService;
import java.nio.ByteBuffer;
import java.util.Objects;
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
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocUnitController.class)
@WithMockUser
class DocUnitControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocUnitService service;

  @Captor private ArgumentCaptor<Flux<ByteBuffer>> fluxCaptor;

  @Test
  void testGenerateNewDocUnit() {
    DocUnitCreationInfo docUnitCreationInfo = DocUnitCreationInfo.EMPTY;
    when(service.generateNewDocUnit(DocUnitCreationInfo.EMPTY))
        .thenReturn(Mono.just(DocUnit.EMPTY));

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/docunits")
        .bodyValue(docUnitCreationInfo)
        .exchange()
        .expectStatus()
        .isCreated();

    verify(service, times(1)).generateNewDocUnit(DocUnitCreationInfo.EMPTY);
  }

  @Test
  void testAttachFileToDocUnit() {
    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/1/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).attachFileToDocUnit(eq("1"), fluxCaptor.capture(), headersCaptor.capture());
    assertEquals(0, Objects.requireNonNull(fluxCaptor.getValue().blockFirst()).array().length);
    assertEquals(0, headersCaptor.getValue().getContentLength());
  }

  @Test
  void testAttachFileToDocUnit_withInvalidId() {
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testRemoveFileFromDocUnit() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/1/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocUnit("1");
  }

  @Test
  void testRemoveFileFromDocUnit_withInvalidId() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testGetAll() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/docunits/").exchange().expectStatus().isOk();

    verify(service).getAll();
  }

  @Test
  void testGetById() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/docunits/1").exchange().expectStatus().isOk();

    verify(service).getById("1");
  }

  @Test
  void testGetById_withInvalidId() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testDeleteById() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/1")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).deleteById("1");
  }

  @Test
  void testDeleteById_withInvalidId() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testUpdateById() {
    DocUnit docUnit = new DocUnit();
    docUnit.setId(1L);
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/1/docx")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isOk();
    verify(service).updateDocUnit(docUnit);
  }

  @Test
  void testUpdateById_withInvalidId() {
    DocUnit docUnit = new DocUnit();
    docUnit.setId(1L);
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/abc/docx")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
