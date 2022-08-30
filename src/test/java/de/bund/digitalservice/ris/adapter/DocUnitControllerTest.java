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
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
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

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String RECEIVER_ADDRESS = "test@exporter.neuris";

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
        .uri("/api/v1/docunits/" + TEST_UUID + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .attachFileToDocUnit(eq(TEST_UUID), fluxCaptor.capture(), headersCaptor.capture());
    assertEquals(0, Objects.requireNonNull(fluxCaptor.getValue().blockFirst()).array().length);
    assertEquals(0, headersCaptor.getValue().getContentLength());
  }

  @Test
  void testAttachFileToDocUnit_withInvalidUuid() {
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
        .uri("/api/v1/docunits/" + TEST_UUID + "/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocUnit(TEST_UUID);
  }

  @Test
  void testRemoveFileFromDocUnit_withInvalidUuid() {
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
  void testGetByDocumentnumber() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunits/ABCD2022000001")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getByDocumentnumber("ABCD2022000001");
  }

  @Test
  void testGetByUuid_withInvalidUuid() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testDeleteByUuid() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/" + TEST_UUID)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).deleteByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withInvalidUuid() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/docunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testUpdateByUuid() {
    DocUnit docUnit = new DocUnit();
    docUnit.setUuid(TEST_UUID);
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/" + TEST_UUID + "/docx")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isOk();
    verify(service).updateDocUnit(docUnit);
  }

  @Test
  void testUpdateByUuid_withInvalidUuid() {
    DocUnit docUnit = new DocUnit();
    docUnit.setUuid(TEST_UUID);
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

  @Test
  void testPublishAsEmail() {
    when(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS))
        .thenReturn(
            Mono.just(
                new XmlMailResponse(
                    TEST_UUID,
                    new XmlMail(
                        1L,
                        123L,
                        "mailSubject",
                        "xml",
                        "status-code",
                        "status-messages",
                        "test.xml",
                        Instant.parse("2020-01-01T01:01:01.00Z")))));

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/" + TEST_UUID + "/publish")
        .bodyValue(RECEIVER_ADDRESS)
        .exchange()
        .expectHeader()
        .valueEquals("Content-Type", "application/json")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("documentUnitUuid")
        .isEqualTo(TEST_UUID.toString())
        .jsonPath("mailSubject")
        .isEqualTo("mailSubject")
        .jsonPath("xml")
        .isEqualTo("xml")
        .jsonPath("statusCode")
        .isEqualTo("status-code")
        .jsonPath("statusMessages")
        .isEqualTo("status-messages")
        .jsonPath("publishDate")
        .isEqualTo("2020-01-01T01:01:01Z");

    verify(service).publishAsEmail(TEST_UUID, RECEIVER_ADDRESS);
  }

  @Test
  void testPublishAsEmail_withServiceThrowsException() {
    when(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS))
        .thenThrow(DocumentUnitPublishException.class);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/docunits/" + TEST_UUID + "/publish")
        .bodyValue(RECEIVER_ADDRESS)
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(service).publishAsEmail(TEST_UUID, RECEIVER_ADDRESS);
  }

  @Test
  void testGetLastPublishedXml() {
    when(service.getLastPublishedXmlMail(TEST_UUID))
        .thenReturn(
            Mono.just(
                new XmlMailResponse(
                    TEST_UUID,
                    new XmlMail(
                        1L,
                        123L,
                        "mailSubject",
                        "xml",
                        "status-code",
                        "status-messages",
                        "test.xml",
                        Instant.parse("2020-01-01T01:01:01.00Z")))));

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("documentUnitUuid")
        .isEqualTo(TEST_UUID.toString())
        .jsonPath("mailSubject")
        .isEqualTo("mailSubject")
        .jsonPath("xml")
        .isEqualTo("xml")
        .jsonPath("statusCode")
        .isEqualTo("status-code")
        .jsonPath("statusMessages")
        .isEqualTo("status-messages")
        .jsonPath("publishDate")
        .isEqualTo("2020-01-01T01:01:01Z");

    verify(service).getLastPublishedXmlMail(TEST_UUID);
  }

  @Test
  void testGetLastPublishedXml_withServiceThrowsException() {
    when(service.getLastPublishedXmlMail(TEST_UUID)).thenThrow(DocumentUnitPublishException.class);

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/docunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(service).getLastPublishedXmlMail(TEST_UUID);
  }
}
