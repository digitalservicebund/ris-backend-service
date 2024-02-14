package de.bund.digitalservice.ris.caselaw.adapter.authorization;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.setUpDocumentationOfficeMocks;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitController.class)
@Import({
  SecurityConfig.class,
  AuthService.class,
  TestConfig.class,
  DocumentNumberPatternProperties.class
})
class DocumentUnitControllerAuthTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String docOffice1Group = "/CC-RIS";
  private final String docOffice2Group = "/caselaw/BGH";
  private final DocumentationOffice docOffice1 = buildDocOffice("CC-RIS");
  private final DocumentationOffice docOffice2 = buildDocOffice("BGH");

  @BeforeEach
  void setUp() {
    setUpDocumentationOfficeMocks(
        userService, docOffice1, docOffice1Group, docOffice2, docOffice2Group);
  }

  @Test
  void testGetByDocumentNumber_nonExistentDocumentNumber_shouldYield403Too() {
    // testGetByDocumentNumber() is also in DocumentUnitControllerAuthIntegrationTest
    when(service.getByDocumentNumber(any(String.class))).thenReturn(Mono.empty());

    risWebTestClient
        .withLogin(docOffice1Group)
        .get()
        .uri("/api/v1/caselaw/documentunits/abc123")
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri("/api/v1/caselaw/documentunits/abc123")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testAttachFileToDocumentUnit() {
    when(service.attachFileToDocumentUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class)))
        .thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file";

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    when(service.removeFileFromDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice2, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file";

    risWebTestClient
        .withLogin(docOffice1Group)
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient.withLogin(docOffice2Group).delete().uri(uri).exchange().expectStatus().isOk();
  }

  @Test
  void testDeleteByUuid() {
    when(service.deleteByUuid(TEST_UUID)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID;

    risWebTestClient.withLogin(docOffice1Group).delete().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUpdateByUuid() {
    DocumentUnit docUnit = mockDocumentUnit(docOffice2, null, null);
    when(service.updateDocumentUnit(docUnit)).thenReturn(Mono.empty());
    when(service.getByUuid(TEST_UUID)).thenReturn(Mono.just(docUnit));

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID;

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isOk();

    UUID nonExistentUuid = UUID.fromString("12345678-1111-2222-3333-787878787878");
    when(service.getByUuid(nonExistentUuid)).thenReturn(Mono.empty());
    uri = "/api/v1/caselaw/documentunits/" + nonExistentUuid;

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testHtml() {
    mockDocumentUnit(docOffice1, "123", Status.builder().publicationStatus(PUBLISHED).build());
    when(docxConverterService.getConvertedObject("123")).thenReturn(Mono.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/docx";

    risWebTestClient.withLogin(docOffice1Group).get().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient.withLogin(docOffice2Group).get().uri(uri).exchange().expectStatus().isOk();

    mockDocumentUnit(docOffice1, "123", Status.builder().publicationStatus(UNPUBLISHED).build());

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testPublishDocumentUnitAsEmail() {
    mockDocumentUnit(docOffice2, null, null);
    when(userService.getEmail(any(OidcUser.class))).thenReturn("abc");
    when(service.publishAsEmail(TEST_UUID, "abc")).thenReturn(Mono.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish";

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient.withLogin(docOffice2Group).put().uri(uri).exchange().expectStatus().isOk();
  }

  @Test
  void testGetPublishedMails() {
    mockDocumentUnit(docOffice1, null, Status.builder().publicationStatus(PUBLISHED).build());
    when(service.getPublicationHistory(TEST_UUID)).thenReturn(Flux.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish";

    risWebTestClient.withLogin(docOffice1Group).get().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient.withLogin(docOffice2Group).get().uri(uri).exchange().expectStatus().isOk();

    mockDocumentUnit(docOffice1, null, Status.builder().publicationStatus(UNPUBLISHED).build());

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private DocumentUnit mockDocumentUnit(
      DocumentationOffice docOffice, String s3path, Status status) {
    DocumentUnit docUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .status(status)
            .s3path(s3path)
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();
    when(service.getByUuid(TEST_UUID)).thenReturn(Mono.just(docUnit));
    return docUnit;
  }
}
