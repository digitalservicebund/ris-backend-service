package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.Utils.getMockLoginWithDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.UNPUBLISHED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitController.class)
@Import({SecurityConfig.class, AuthService.class})
class DocumentUnitControllerAuthTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean DocxConverterService docxConverterService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String docOffice1Group = "/CC-RIS";
  private final DocumentationOffice docOffice1 =
      DocumentationOffice.builder().label("CC-RIS").abbreviation("XX").build();
  private final String docOffice2Group = "/caselaw/BGH";
  private final DocumentationOffice docOffice2 =
      DocumentationOffice.builder().label("BGH").abbreviation("CO").build();

  @BeforeEach
  void setUp() {
    doReturn(Mono.just(docOffice1))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice1Group);
                }));
    doReturn(Mono.just(docOffice2))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice2Group);
                }));
  }

  // testGetByDocumentNumber() is in DocumentUnitControllerAuthIntegrationTest

  @Test
  void testAttachFileToDocumentUnit() {
    when(service.attachFileToDocumentUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class)))
        .thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .put()
        .uri(uri)
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
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

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testDeleteByUuid() {
    when(service.deleteByUuid(TEST_UUID)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID;

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
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

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .put()
        .uri(uri)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
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
    mockDocumentUnit(docOffice1, "123", PUBLISHED);
    when(docxConverterService.getConvertedObject("123")).thenReturn(Mono.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/docx";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    mockDocumentUnit(docOffice1, "123", UNPUBLISHED);

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
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

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void testGetLastPublishedXml() {
    mockDocumentUnit(docOffice1, null, PUBLISHED);
    when(service.getLastPublishedXmlMail(TEST_UUID)).thenReturn(Mono.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    mockDocumentUnit(docOffice1, null, UNPUBLISHED);

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private DocumentUnit mockDocumentUnit(
      DocumentationOffice docOffice, String s3path, DocumentUnitStatus status) {
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
