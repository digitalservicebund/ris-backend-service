package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitCreationInfo;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.PublishState;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
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
@WebFluxTest(controllers = DocumentUnitController.class)
@WithMockUser
class DocumentUnitControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocumentUnitService service;

  @Captor private ArgumentCaptor<ByteBuffer> captor;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String RECEIVER_ADDRESS = "test@exporter.neuris";

  @Test
  void testGenerateNewDocumentUnit() {
    DocumentUnitCreationInfo documentUnitCreationInfo = DocumentUnitCreationInfo.EMPTY;
    when(service.generateNewDocumentUnit(DocumentUnitCreationInfo.EMPTY))
        .thenReturn(Mono.just(DocumentUnitBuilder.newInstance().build()));

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/caselaw/documentunits")
        .bodyValue(documentUnitCreationInfo)
        .exchange()
        .expectStatus()
        .isCreated();

    verify(service, times(1)).generateNewDocumentUnit(DocumentUnitCreationInfo.EMPTY);
  }

  @Test
  void testAttachFileToDocumentUnit() {
    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
    when(service.attachFileToDocumentUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class)))
        .thenReturn(Mono.empty());
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .attachFileToDocumentUnit(eq(TEST_UUID), captor.capture(), headersCaptor.capture());
    assertEquals(0, Objects.requireNonNull(captor.getValue()).array().length);
    assertEquals(0, headersCaptor.getValue().getContentLength());
  }

  @Test
  void testAttachFileToDocumentUnit_withInvalidUuid() {
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    when(service.removeFileFromDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocumentUnit(TEST_UUID);
  }

  @Test
  void testRemoveFileFromDocumentUnit_withInvalidUuid() {
    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testGetAll() {
    when(service.getAll()).thenReturn(Flux.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getAll();
  }

  @Test
  void testGetByDocumentnumber() {
    when(service.getByDocumentNumber("ABCD202200001")).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/ABCD202200001")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getByDocumentNumber("ABCD202200001");
  }

  @Test
  void testGetByUuid_withInvalidUuid() {
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testDeleteByUuid() {
    when(service.deleteByUuid(TEST_UUID)).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
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
        .uri("/api/v1/caselaw/documentunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testUpdateByUuid() {
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setDocumentnumber("ABCD202200001");
    documentUnitDTO.setUuid(TEST_UUID);
    DocumentUnit documentUnit =
        DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build();
    when(service.updateDocumentUnit(documentUnit)).thenReturn(Mono.empty());
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk();
    verify(service).updateDocumentUnit(documentUnit);
  }

  @Test
  void testUpdateByUuid_withInvalidUuid() {
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setUuid(TEST_UUID);
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/abc")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(documentUnitDTO)
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
                        TEST_UUID,
                        "receiver address",
                        "mailSubject",
                        "xml",
                        "status-code",
                        List.of("status-messages"),
                        "test.xml",
                        Instant.parse("2020-01-01T01:01:01.00Z"),
                        PublishState.UNKNOWN))));

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .bodyValue(RECEIVER_ADDRESS)
        .exchange()
        .expectHeader()
        .valueEquals("Content-Type", "application/json")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("documentUnitUuid")
        .isEqualTo(TEST_UUID.toString())
        .jsonPath("receiverAddress")
        .isEqualTo("receiver address")
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
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
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
                        TEST_UUID,
                        "receiver address",
                        "mailSubject",
                        "xml",
                        "status-code",
                        List.of("status-messages"),
                        "test.xml",
                        Instant.parse("2020-01-01T01:01:01.00Z"),
                        PublishState.SENT))));

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("documentUnitUuid")
        .isEqualTo(TEST_UUID.toString())
        .jsonPath("receiverAddress")
        .isEqualTo("receiver address")
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
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(service).getLastPublishedXmlMail(TEST_UUID);
  }
}
