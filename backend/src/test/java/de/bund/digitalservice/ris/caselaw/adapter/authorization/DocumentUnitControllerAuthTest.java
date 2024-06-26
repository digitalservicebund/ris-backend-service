package de.bund.digitalservice.ris.caselaw.adapter.authorization;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.setUpDocumentationOfficeMocks;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.DocumentUnitControllerTestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DocumentUnitController.class)
@Import({DocumentUnitControllerTestConfig.class})
class DocumentUnitControllerAuthTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private AttachmentService attachmentService;
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean DatabaseApiKeyRepository apiKeyRepository;
  @MockBean DatabaseDocumentationOfficeRepository officeRepository;

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
    when(service.getByDocumentNumber(any(String.class))).thenReturn(null);

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
    when(attachmentService.attachFileToDocumentationUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class)))
        .thenReturn(Attachment.builder().s3path("fooPath").build());
    when(docxConverterService.getConvertedObject(anyString())).thenReturn(Docx2Html.EMPTY);
    mockDocumentUnit(docOffice1, null, null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file";

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyValue(new byte[] {})
        .exchange()
        .expectStatus()
        .isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyValue(new byte[] {})
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    mockDocumentUnit(docOffice2, null, null);
    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file/fooPath";

    risWebTestClient
        .withLogin(docOffice1Group)
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void testDeleteByUuid() throws DocumentationUnitNotExistsException {
    when(service.deleteByUuid(TEST_UUID)).thenReturn(null);
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
  void testUpdateByUuid() throws DocumentationUnitNotExistsException {
    DocumentUnit docUnit = mockDocumentUnit(docOffice2, null, null);
    when(service.updateDocumentUnit(docUnit)).thenReturn(null);
    when(service.getByUuid(TEST_UUID)).thenReturn(docUnit);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID;

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isOk();

    UUID nonExistentUuid = UUID.fromString("12345678-1111-2222-3333-787878787878");
    when(service.getByUuid(nonExistentUuid)).thenReturn(null);
    uri = "/api/v1/caselaw/documentunits/" + nonExistentUuid;

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(docUnit)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testGetHtml() {
    mockDocumentUnit(docOffice1, "123", Status.builder().publicationStatus(PUBLISHED).build());
    when(docxConverterService.getConvertedObject("123")).thenReturn(null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/docx/123";

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
  void testPublishDocumentUnitAsEmail() throws DocumentationUnitNotExistsException {
    mockDocumentUnit(docOffice2, null, null);
    when(userService.getEmail(any(OidcUser.class))).thenReturn("abc");
    when(service.publishAsEmail(TEST_UUID, "abc")).thenReturn(null);

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
    when(service.getPublicationHistory(TEST_UUID)).thenReturn(List.of());

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
            .attachments(Collections.singletonList(Attachment.builder().s3path(s3path).build()))
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();
    when(service.getByUuid(TEST_UUID)).thenReturn(docUnit);
    return docUnit;
  }
}
