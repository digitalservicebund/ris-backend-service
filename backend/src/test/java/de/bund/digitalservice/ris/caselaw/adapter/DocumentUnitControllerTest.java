package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublishState;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitController.class)
@Import({SecurityConfig.class, AuthService.class, TestConfig.class})
class DocumentUnitControllerTest {
  @Autowired private RisWebTestClient risWebClient;
  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  @Captor private ArgumentCaptor<ByteBuffer> captor;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";
  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @BeforeEach
  void setup() {
    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DigitalService");
                }));

    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
  }

  @Test
  void testGenerateNewDocumentUnit() {
    // userService.getDocumentationOffice is mocked in @BeforeEach
    when(service.generateNewDocumentUnit(docOffice))
        .thenReturn(Mono.just(DocumentUnit.builder().build()));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated();

    verify(service, times(1)).generateNewDocumentUnit(docOffice);
    verify(userService, times(1)).getDocumentationOffice(any(OidcUser.class));
  }

  @Test
  void testAttachFileToDocumentUnit() {
    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
    when(service.attachFileToDocumentUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class)))
        .thenReturn(Mono.empty());
    risWebClient
        .withDefaultLogin()
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
    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    when(service.removeFileFromDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocumentUnit(TEST_UUID);
  }

  @Test
  void testRemoveFileFromDocumentUnit_withInvalidUuid() {
    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testGetAll() {
    // userService.getDocumentationOffice is mocked in @BeforeEach
    when(service.getAll(PageRequest.of(0, 10), docOffice)).thenReturn(Mono.empty());
    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getAll(PageRequest.of(0, 10), docOffice);
  }

  @Test
  void testGetByDocumentnumber() {
    when(service.getByDocumentNumber("ABCD202200001"))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/ABCD202200001")
        .exchange()
        .expectStatus()
        .isOk();

    // once by the AuthService and once by the controller asking the service
    verify(service, times(2)).getByDocumentNumber("ABCD202200001");
  }

  @Test
  void testGetByDocumentNumber_withInvalidDocumentNumber() {
    when(service.getByDocumentNumber("abc")).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/abc")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testDeleteByUuid() {
    when(service.deleteByUuid(TEST_UUID)).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).deleteByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withInvalidUuid() {
    when(service.getByDocumentNumber("abc")).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
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
    DocumentUnit documentUnit = DocumentUnitTransformer.transformDTO(documentUnitDTO);

    when(service.updateDocumentUnit(eq(documentUnit), any(DocumentationOffice.class)))
        .thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).updateDocumentUnit(eq(documentUnit), any(DocumentationOffice.class));
  }

  @Test
  void testUpdateByUuid_withInvalidUuid() {
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setUuid(TEST_UUID);
    risWebClient
        .withDefaultLogin()
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
    when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
    when(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS))
        .thenReturn(
            Mono.just(
                XmlPublication.builder()
                    .documentUnitUuid(TEST_UUID)
                    .receiverAddress("receiver address")
                    .mailSubject("mailSubject")
                    .xml("xml")
                    .statusCode("status-code")
                    .statusMessages(List.of("status-messages"))
                    .fileName("test.xml")
                    .publishDate(Instant.parse("2020-01-01T01:01:01.00Z"))
                    .publishState(PublishState.UNKNOWN)
                    .build()));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
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
        .jsonPath("date")
        .isEqualTo("2020-01-01T01:01:01Z");

    verify(service).publishAsEmail(TEST_UUID, ISSUER_ADDRESS);
  }

  @Test
  void testPublishAsEmail_withServiceThrowsException() {
    when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
    when(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS))
        .thenThrow(DocumentUnitPublishException.class);

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(service).publishAsEmail(TEST_UUID, ISSUER_ADDRESS);
  }

  @Test
  void testGetLastPublishedXml() {

    when(service.getPublicationHistory(TEST_UUID))
        .thenReturn(
            Flux.fromIterable(
                List.of(
                    PublicationReport.builder()
                        .content("<html>2021 Report</html>")
                        .receivedDate(Instant.parse("2021-01-01T01:01:01.00Z"))
                        .build(),
                    XmlPublication.builder()
                        .documentUnitUuid(TEST_UUID)
                        .receiverAddress("receiver address")
                        .mailSubject("mailSubject")
                        .xml("xml")
                        .statusCode("status-code")
                        .statusMessages(List.of("status-messages"))
                        .fileName("test.xml")
                        .publishDate(Instant.parse("2020-01-01T01:01:01.00Z"))
                        .publishState(PublishState.SENT)
                        .build(),
                    PublicationReport.builder()
                        .content("<html>2019 Report</html>")
                        .receivedDate(Instant.parse("2019-01-01T01:01:01.00Z"))
                        .build())));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("[0].type")
        .isEqualTo("PUBLICATION_REPORT")
        .jsonPath("[0].content")
        .isEqualTo("<html>2021 Report</html>")
        .jsonPath("[0].date")
        .isEqualTo("2021-01-01T01:01:01Z")
        .jsonPath("[1].receivedDate")
        .doesNotExist()
        .jsonPath("[1].type")
        .isEqualTo("PUBLICATION")
        .jsonPath("[1].documentUnitUuid")
        .isEqualTo(TEST_UUID.toString())
        .jsonPath("[1].receiverAddress")
        .isEqualTo("receiver address")
        .jsonPath("[1].mailSubject")
        .isEqualTo("mailSubject")
        .jsonPath("[1].xml")
        .isEqualTo("xml")
        .jsonPath("[1].statusCode")
        .isEqualTo("status-code")
        .jsonPath("[1].statusMessages")
        .isEqualTo("status-messages")
        .jsonPath("[1].date")
        .isEqualTo("2020-01-01T01:01:01Z")
        .jsonPath("[1].publishDate")
        .doesNotExist()
        .jsonPath("[2].type")
        .isEqualTo("PUBLICATION_REPORT")
        .jsonPath("[2].content")
        .isEqualTo("<html>2019 Report</html>")
        .jsonPath("[2].date")
        .isEqualTo("2019-01-01T01:01:01Z");

    verify(service).getPublicationHistory(TEST_UUID);
  }

  @Test
  void testGetLastPublishedXml_withServiceThrowsException() {
    when(service.getPublicationHistory(TEST_UUID)).thenThrow(DocumentUnitPublishException.class);

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(service).getByUuid(TEST_UUID);
    verify(service).getPublicationHistory(TEST_UUID);
  }

  @Test
  void testSearchByLinkedDocumentationUnit() {
    LinkedDocumentationUnit linkedDocumentationUnit = LinkedDocumentationUnit.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(service.searchByLinkedDocumentationUnit(linkedDocumentationUnit, pageRequest))
        .thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(linkedDocumentationUnit)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).searchByLinkedDocumentationUnit(linkedDocumentationUnit, pageRequest);
  }

  @Test
  void testHtml() {
    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .s3path("123")
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
    when(docxConverterService.getConvertedObject("123")).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/docx")
        .exchange()
        .expectStatus()
        .isOk();

    // once by the AuthService and once by the controller asking the service
    verify(service, times(2)).getByUuid(TEST_UUID);
    verify(docxConverterService).getConvertedObject("123");
  }
}
