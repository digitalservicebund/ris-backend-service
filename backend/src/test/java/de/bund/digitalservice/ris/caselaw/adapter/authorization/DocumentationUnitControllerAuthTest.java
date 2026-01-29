package de.bund.digitalservice.ris.caselaw.adapter.authorization;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildCCRisDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.setUpDocumentationOfficeMocks;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.DocumentationUnitControllerTestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.BulkDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DocumentationUnitController.class)
@Import({DocumentationUnitControllerTestConfig.class})
class DocumentationUnitControllerAuthTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @MockitoBean private DocumentationUnitService service;
  @MockitoBean private BulkDocumentationUnitService bulkDocumentationUnitService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  @MockitoBean private HandoverService handoverService;
  @MockitoBean private KeycloakUserService userService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PortalPublicationService portalPublicationService;
  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean DatabaseApiKeyRepository apiKeyRepository;
  @MockitoBean DatabaseDocumentationOfficeRepository officeRepository;
  @MockitoBean PatchMapperService patchMapperService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private AttachmentRepository attachmentRepository;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final UUID TEST_FILE_UUID =
      UUID.fromString("99999999-4444-4444-4444-121212121212");
  private final String docOffice1Group = "/CC-RIS";
  private final String docOffice2Group = "/BGH";
  private final DocumentationOffice docOffice1 = buildCCRisDocOffice();
  private final DocumentationOffice docOffice2 = buildBGHDocOffice();

  @BeforeEach
  void setUp() {
    setUpDocumentationOfficeMocks(
        userService, docOffice1, docOffice1Group, docOffice2, docOffice2Group);
  }

  @Test
  void testGetByDocumentNumber_nonExistentDocumentNumber_shouldYield403Too()
      throws DocumentationUnitNotExistsException {

    // testGetByDocumentNumber() is also in DocumentationUnitControllerAuthIntegrationTest
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
  void testAttachFileToDocumentationUnit() throws DocumentationUnitNotExistsException {
    when(attachmentService.attachFileToDocumentationUnit(
            eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(Attachment.builder().s3path("fooPath").build());
    when(converterService.getConvertedObject(anyString())).thenReturn(Docx2Html.EMPTY);
    mockDocumentationUnit(docOffice1, null, null);

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
  void testRemoveFileFromDocumentationUnit() throws DocumentationUnitNotExistsException {
    mockDocumentationUnit(docOffice2, null, null);
    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file/" + TEST_FILE_UUID;

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
    mockDocumentationUnit(docOffice1, null, null);

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
    Decision docUnit = mockDocumentationUnit(docOffice2, null, null);
    when(service.updateDocumentationUnit(docUnit)).thenReturn(null);
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
  void testGetHtml() throws DocumentationUnitNotExistsException {
    var attachmentId = UUID.randomUUID();
    mockDocumentationUnit(docOffice1, "123", Status.builder().publicationStatus(PUBLISHED).build());
    when(converterService.getConvertedObject("123")).thenReturn(null);
    when(attachmentRepository.findById(attachmentId))
        .thenReturn(Optional.of(AttachmentDTO.builder().s3ObjectPath("123").build()));

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/file/" + attachmentId + "/html";

    risWebTestClient.withLogin(docOffice1Group).get().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient.withLogin(docOffice2Group).get().uri(uri).exchange().expectStatus().isOk();

    mockDocumentationUnit(
        docOffice1, "123", Status.builder().publicationStatus(UNPUBLISHED).build());

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testHandoverDocumentationUnitAsEmail() throws DocumentationUnitNotExistsException {
    mockDocumentationUnit(docOffice2, null, null);
    when(userService.getEmail(any(OidcUser.class))).thenReturn("abc");
    when(handoverService.handoverDocumentationUnitAsMail(TEST_UUID, "abc", null)).thenReturn(null);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/handover";

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient
        .withLogin(docOffice2Group)
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @ParameterizedTest
  @EnumSource(
      value = PublicationStatus.class,
      names = {"UNPUBLISHED", "PUBLISHED"})
  void testGetEvents(PublicationStatus status) throws DocumentationUnitNotExistsException {
    mockDocumentationUnit(docOffice1, null, Status.builder().publicationStatus(status).build());
    when(handoverService.getEventLog(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT))
        .thenReturn(List.of());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/handover";

    risWebTestClient.withLogin(docOffice1Group).get().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    mockDocumentationUnit(
        docOffice1, null, Status.builder().publicationStatus(UNPUBLISHED).build());

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private Decision mockDocumentationUnit(
      DocumentationOffice docOffice, String s3path, Status status)
      throws DocumentationUnitNotExistsException {

    Decision docUnit =
        Decision.builder()
            .uuid(TEST_UUID)
            .status(status == null ? Status.builder().publicationStatus(PUBLISHED).build() : status)
            .attachments(
                Collections.singletonList(
                    Attachment.builder().id(TEST_FILE_UUID).s3path(s3path).build()))
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();
    when(service.getByUuid(TEST_UUID)).thenReturn(docUnit);
    return docUnit;
  }
}
