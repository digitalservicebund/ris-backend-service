package de.bund.digitalservice.ris.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.service.DocUnitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocUnitController.class)
@WithMockUser
@Tag("test")
class DocUnitControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocUnitService service;

  @Captor
  private ArgumentCaptor<Mono<FilePart>> captor;
  @Test
  public void testUploadFile() {
    var bodyBuilder = new MultipartBodyBuilder();
    bodyBuilder
        .part("fileToUpload", new byte[] {})
        .header("Content-Disposition", "form-data; name=fileToUpload; filename=test.docx");

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/docunit/upload")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).generateNewDocUnit(captor.capture());
    Assertions.assertEquals("test.docx", captor.getValue().block().filename());
  }

  @Test
  public void testGetAll() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunit/getAll")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getAll();
  }
}
