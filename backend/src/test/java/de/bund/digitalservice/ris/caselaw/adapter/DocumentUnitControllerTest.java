package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.OriginalFileDocument;
import de.bund.digitalservice.ris.caselaw.domain.OriginalFileDocumentService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitController.class)
@Import({
  SecurityConfig.class,
  AuthService.class,
  TestConfig.class,
  DocumentNumberPatternConfig.class
})
class DocumentUnitControllerTest {
  @Autowired private RisWebTestClient risWebClient;
  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private OriginalFileDocumentService originalFileDocumentService;
  @MockBean DatabaseApiKeyRepository apiKeyRepository;
  @MockBean DatabaseDocumentationOfficeRepository officeRepository;

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
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
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
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder()
            .id(TEST_UUID)
            .documentNumber("ABCD202200001")
            .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("DS").build())
            .build();
    DocumentUnit documentUnit = DocumentationUnitTransformer.transformToDomain(documentUnitDTO);

    when(service.updateDocumentUnit(documentUnit)).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
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
    DocumentUnit documentUnitDTO = DocumentUnit.builder().uuid(TEST_UUID).build();

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
  void testGetPublicationXmlPreview() {
    when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
    when(service.previewPublication(TEST_UUID))
        .thenReturn(
            Mono.just(
                new XmlResultObject(
                    "xml",
                    "200",
                    List.of("status-messages"),
                    "test.xml",
                    Instant.parse("2020-01-01T01:01:01.00Z"))));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-publication-xml")
        .exchange()
        .expectHeader()
        .valueEquals("Content-Type", "application/json")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("xml")
        .isEqualTo("xml")
        .jsonPath("statusCode")
        .isEqualTo("200")
        .jsonPath("statusMessages")
        .isEqualTo("status-messages")
        .jsonPath("publishDate")
        .isEqualTo("2020-01-01T01:01:01Z");

    verify(service).previewPublication(TEST_UUID);
  }

  @Test
  void testSearchByLinkedDocumentationUnit() {
    RelatedDocumentationUnit linkedDocumentationUnit = RelatedDocumentationUnit.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(service.searchLinkableDocumentationUnits(
            linkedDocumentationUnit, docOffice, "KORE0000000000", pageRequest))
        .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/KORE0000000000/search-linkable-documentation-units?pg=0&sz=10")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(linkedDocumentationUnit)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .searchLinkableDocumentationUnits(
            linkedDocumentationUnit, docOffice, "KORE0000000000", pageRequest);
  }

  @Test
  void testSearchByDocumentUnitListEntry() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(service.searchByDocumentationUnitSearchInput(
            pageRequest,
            docOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()))
        .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .searchByDocumentationUnitSearchInput(
            pageRequest,
            docOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
  }

  @Test
  void testGetHtml() {
    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .originalFiles(
                        Collections.singletonList(
                            OriginalFileDocument.builder().s3path("123").build()))
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
