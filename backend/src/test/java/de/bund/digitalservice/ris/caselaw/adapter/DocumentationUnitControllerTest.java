package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSuser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.DocumentationUnitControllerTestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverterException;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import de.bund.digitalservice.ris.caselaw.domain.BulkAssignProcessStepRequest;
import de.bund.digitalservice.ris.caselaw.domain.BulkDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatusRequest;
import de.bund.digitalservice.ris.caselaw.domain.EventRecord;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverReport;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.Image;
import de.bund.digitalservice.ris.caselaw.domain.Kind;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ImageNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.MappingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.core.type.TypeReference;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DocumentationUnitController.class)
@Import({DocumentationUnitControllerTestConfig.class})
class DocumentationUnitControllerTest {
  @Autowired private RisWebTestClient risWebClient;
  @MockitoBean private DocumentationUnitService service;
  @MockitoBean private BulkDocumentationUnitService abstractService;
  @MockitoBean private DocumentationUnitDocxMetadataInitializationService docUnitAttachmentService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private PortalPublicationService portalPublicationService;
  @MockitoBean private UserService userService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean DatabaseApiKeyRepository apiKeyRepository;
  @MockitoBean DatabaseDocumentationOfficeRepository officeRepository;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private OidcUser oidcUser;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;

  @MockitoBean(name = "userHasWriteAccess")
  private Function<UUID, Boolean> userHasWriteAccess;

  @MockitoBean(name = "userHasBulkWriteAccess")
  private Function<List<UUID>, Boolean> userHasBulkWriteAccess;

  @MockitoBean private XmlUtilService xmlUtilService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";
  private final DocumentationOffice docOffice = buildDSDocOffice();
  private final User user = buildDSuser();

  @BeforeEach
  void setup() throws DocumentationUnitNotExistsException {
    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser currentUser) -> {
                  List<String> groups = currentUser.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));

    doReturn(true).when(userService).isInternal(any());

    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Decision.builder()
                .coreData(CoreData.builder().documentationOffice(docOffice).build())
                .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                .build());
  }

  @Nested
  class GenerateNewDocumentationUnit {
    @Test
    void
        testGenerateNewDocumentationUnit_withInternalUserAndNoKind_shouldSuccessfullyCreateDecision() {
      // userService.getDocumentationOffice is mocked in @BeforeEach
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);
      when(service.generateNewDocumentationUnit(user, Optional.empty(), Kind.DECISION))
          .thenReturn(
              Decision.builder()
                  .coreData(CoreData.builder().documentationOffice(docOffice).build())
                  .build());

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new")
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody(Decision.class);

      verify(service, times(1)).generateNewDocumentationUnit(user, Optional.empty(), Kind.DECISION);
      verify(userService, times(1)).getUser(any(OidcUser.class));
    }

    @Test
    void
        testGenerateNewDocumentationUnit_withInternalUserAndKindPendingProceeding_shouldSuccessfullyCreatePendingProceeding() {
      // Arrange
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);
      when(service.generateNewDocumentationUnit(user, Optional.empty(), Kind.PENDING_PROCEEDING))
          .thenReturn(
              PendingProceeding.builder()
                  .coreData(CoreData.builder().documentationOffice(docOffice).build())
                  .build());

      // Act
      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new?kind=PENDING_PROCEEDING")
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody(PendingProceeding.class);

      // Assert
      verify(service, times(1))
          .generateNewDocumentationUnit(user, Optional.empty(), Kind.PENDING_PROCEEDING);
      verify(userService, times(1)).getUser(any(OidcUser.class));
    }

    @Test
    void testGenerateNewDocumentationUnit_withExternalUserAndNoKind_shouldBeForbidden() {
      // Arrange
      when(userService.isInternal(any(OidcUser.class))).thenReturn(false);

      // Act
      risWebClient
          .withExternalLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new")
          .exchange()
          .expectStatus()
          .isForbidden();

      // Assert
      verify(service, never()).generateNewDocumentationUnit(any(), any(), any());
      verify(userService, times(0)).getDocumentationOffice(any(OidcUser.class));
    }

    @Test
    void
        testGenerateNewDocumentationUnit_withExternalUserAndKindPendingProceeding_shouldBeForbidden() {
      // Arrange
      when(userService.isInternal(any(OidcUser.class))).thenReturn(false);

      // Act
      risWebClient
          .withExternalLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new?kind=PENDING_PROCEEDING")
          .exchange()
          .expectStatus()
          .isForbidden();

      // Assert
      verify(service, never()).generateNewDocumentationUnit(any(), any(), any());
      verify(userService, times(0)).getDocumentationOffice(any(OidcUser.class));
    }

    @Test
    void testGenerateNewDocumentationUnit_withUnknownKind_shouldBeBadRequest() {
      // Arrange
      when(userService.isInternal(any(OidcUser.class))).thenReturn(false);

      // Act
      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new?kind=UNKNOWN")
          .exchange()
          .expectStatus()
          .isBadRequest();

      // Assert
      verify(service, never()).generateNewDocumentationUnit(any(), any(), any());
      verify(userService, times(0)).getDocumentationOffice(any(OidcUser.class));
    }

    @Test
    void testGenerateNewDocumentationUnit_withUnsupportedKind_shouldBeForbidden() {
      // Arrange
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);
      when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
      when(service.generateNewDocumentationUnit(user, Optional.empty(), Kind.UNSUPPORTED))
          .thenThrow(DocumentationUnitException.class);

      // Act
      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/new?kind=UNSUPPORTED")
          .exchange()
          .expectStatus()
          .is5xxServerError();

      // Assert
      verify(service, times(1))
          .generateNewDocumentationUnit(user, Optional.empty(), Kind.UNSUPPORTED);
    }
  }

  @Nested
  class AttachFileToDocumentationUnit {
    @Test
    void testAttachUnsupportedFile_shouldDeleteOnFail()
        throws IOException, DocumentationUnitNotExistsException {
      when(userHasWriteAccess.apply(any())).thenReturn(true);
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      when(converterService.getConvertedObject(any())).thenThrow(DocxConverterException.class);

      when(attachmentService.attachFileToDocumentationUnit(
              eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class), any()))
          .thenReturn(Attachment.builder().s3path("fooPath").build());

      when(service.getByUuid(TEST_UUID))
          .thenReturn(
              Decision.builder()
                  .coreData(CoreData.builder().documentationOffice(docOffice).build())
                  .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                  .build());

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
          .contentType(
              MediaType.parseMediaType(
                  "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
          .bodyAsByteArray(attachment)
          .exchange()
          .expectStatus()
          .is4xxClientError();

      verify(attachmentService).deleteByS3Path(any(), any(), any());
    }

    @Test
    void testAttachFile_shouldInitializeCoreDataAndCheckDuplicates()
        throws IOException, DocumentationUnitNotExistsException {
      when(userHasWriteAccess.apply(any())).thenReturn(true);
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));

      when(attachmentService.attachFileToDocumentationUnit(
              eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class), any()))
          .thenReturn(Attachment.builder().s3path("fooPath").build());

      Decision docUnit =
          Decision.builder()
              .documentNumber("myDocNumber1")
              .coreData(CoreData.builder().documentationOffice(docOffice).build())
              .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
              .build();
      when(service.getByUuid(TEST_UUID)).thenReturn(docUnit);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
          .contentType(
              MediaType.parseMediaType(
                  "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
          .bodyAsByteArray(attachment)
          .exchange()
          .expectStatus()
          .isOk();

      verify(duplicateCheckService, times(1)).checkDuplicates(docUnit.documentNumber());
      verify(docUnitAttachmentService, times(1)).initializeCoreData(eq(docUnit), any(), any());

      verify(attachmentService).attachFileToDocumentationUnit(eq(TEST_UUID), any(), any(), any());
    }
  }

  @Nested
  class GetByDocumentNumber {
    @Test
    void testGetByDocumentnumber() throws DocumentationUnitNotExistsException {
      when(service.getByDocumentNumber("ABCD202200001"))
          .thenReturn(
              Decision.builder()
                  .coreData(CoreData.builder().documentationOffice(docOffice).build())
                  .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                  .build());

      risWebClient
          .withDefaultLogin()
          .get()
          .uri("/api/v1/caselaw/documentunits/ABCD202200001")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(Decision.class);

      // once by the AuthService and once by the controller asking the service
      verify(service, times(1)).getByDocumentNumber("ABCD202200001");
      verify(duplicateCheckService, times(1)).checkDuplicates("ABCD202200001");
    }

    @Test
    void testGetByDocumentNumber_withPendingProceeding()
        throws DocumentationUnitNotExistsException {
      when(service.getByDocumentNumber("ABCD202200001"))
          .thenReturn(
              PendingProceeding.builder()
                  .coreData(CoreData.builder().documentationOffice(docOffice).build())
                  .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                  .build());

      risWebClient
          .withDefaultLogin()
          .get()
          .uri("/api/v1/caselaw/documentunits/ABCD202200001")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(PendingProceeding.class);

      // once by the AuthService and once by the controller asking the service
      verify(service, times(1)).getByDocumentNumber("ABCD202200001");
    }

    @Test
    void testGetByDocumentNumber_withInvalidDocumentNumber()
        throws DocumentationUnitNotExistsException {

      when(service.getByDocumentNumber("abc")).thenReturn(null);

      risWebClient
          .withDefaultLogin()
          .get()
          .uri("/api/v1/caselaw/documentunits/abc")
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }
  }

  @Nested
  class DeleteByUuid {
    @Test
    void testDeleteByUuid_withInternalUser_shouldSucceed()
        throws DocumentationUnitNotExistsException {
      when(service.deleteByUuid(TEST_UUID)).thenReturn(null);
      when(userHasWriteAccess.apply(any())).thenReturn(true);

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
    void testDeleteByUuid_withExternalUser_shouldBeForbidden()
        throws DocumentationUnitNotExistsException {
      // userService.getDocumentationOffice is mocked in @BeforeEach
      when(userService.isInternal(any(OidcUser.class))).thenReturn(false);
      when(service.deleteByUuid(TEST_UUID)).thenReturn(null);

      risWebClient
          .withExternalLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
          .exchange()
          .expectStatus()
          .isForbidden();

      verify(service, times(0)).deleteByUuid(TEST_UUID);
    }

    @Test
    void testDeleteByUuid_withInvalidUuid() throws DocumentationUnitNotExistsException {
      when(service.getByDocumentNumber("abc")).thenReturn(null);

      risWebClient
          .withDefaultLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/abc")
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }
  }

  @Nested
  class UpdateByUuid {
    @Test
    void testUpdateByUuid() throws DocumentationUnitNotExistsException {
      DecisionDTO documentationUnitDTO =
          DecisionDTO.builder()
              .id(TEST_UUID)
              .documentNumber("ABCD202200001")
              .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("DS").build())
              .build();
      Decision decision = DecisionTransformer.transformToDomain(documentationUnitDTO);

      when(service.updateDocumentationUnit(decision)).thenReturn(null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(decision)
          .exchange()
          .expectStatus()
          .isOk();

      verify(service).updateDocumentationUnit(decision);
    }

    @Test
    void testUpdateByUuid_withInvalidUuid() {
      Decision decisionDTO = Decision.builder().uuid(TEST_UUID).build();

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/abc")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(decisionDTO)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }
  }

  @Nested
  class PartialUpdateByUuid {
    @Test
    @Disabled("fix and enable")
    void testPatchUpdateByUuid() throws DocumentationUnitNotExistsException {
      DecisionDTO documentationUnitDTO =
          DecisionDTO.builder()
              .id(TEST_UUID)
              .documentNumber("ABCD202200001")
              .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("DS").build())
              .build();
      Decision decision = DecisionTransformer.transformToDomain(documentationUnitDTO);

      when(service.updateDocumentationUnit(decision)).thenReturn(decision);
      when(service.getByUuid(TEST_UUID)).thenReturn(decision);

      JsonNode valueToReplace = new TextNode("newValue");
      JsonPatchOperation replaceOp =
          new ReplaceOperation("/coreData/appraisalBody", valueToReplace);
      JsonPatch patch = new JsonPatch(List.of(replaceOp));
      RisJsonPatch risJsonPatch = new RisJsonPatch(0L, patch, Collections.emptyList());

      risWebClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(risJsonPatch)
          .exchange()
          .expectStatus()
          .isOk();
    }
  }

  @Nested
  class HandoverDocumentationUnitAsMail {
    @Test
    void testHandoverAsEmail() throws DocumentationUnitNotExistsException {
      when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
      when(handoverService.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
          .thenReturn(
              HandoverMail.builder()
                  .entityId(TEST_UUID)
                  .receiverAddress("receiver address")
                  .mailSubject("mailSubject")
                  .attachments(
                      Collections.singletonList(
                          MailAttachment.builder().fileContent("xml").fileName("test.xml").build()))
                  .success(true)
                  .statusMessages(List.of("status-messages"))
                  .handoverDate(Instant.parse("2020-01-01T01:01:01.00Z"))
                  .build());

      HandoverMail responseBody =
          risWebClient
              .withDefaultLogin()
              .put()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/handover")
              .exchange()
              .expectHeader()
              .contentType(MediaType.APPLICATION_JSON)
              .expectStatus()
              .isOk()
              .expectBody(HandoverMail.class)
              .returnResult()
              .getResponseBody();

      assertThat(responseBody)
          .isEqualTo(
              HandoverMail.builder()
                  .entityId(TEST_UUID)
                  .receiverAddress("receiver address")
                  .mailSubject("mailSubject")
                  .attachments(
                      Collections.singletonList(
                          MailAttachment.builder().fileContent("xml").fileName("test.xml").build()))
                  .success(true)
                  .statusMessages(List.of("status-messages"))
                  .handoverDate(Instant.parse("2020-01-01T01:01:01Z"))
                  .build());

      verify(handoverService).handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null);
    }

    @Test
    void testHandoverAsEmail_withServiceThrowsException()
        throws DocumentationUnitNotExistsException {
      when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
      when(handoverService.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
          .thenThrow(DocumentationUnitNotExistsException.class);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/handover")
          .exchange()
          .expectStatus()
          .is5xxServerError();

      verify(handoverService).handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null);
    }

    @Test
    void testGetLastHandoverXmlMail() {

      when(handoverService.getEventLog(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT))
          .thenReturn(
              List.of(
                  HandoverReport.builder()
                      .content("<html>2021 Report</html>")
                      .receivedDate(Instant.parse("2021-01-01T01:01:01.00Z"))
                      .build(),
                  HandoverMail.builder()
                      .entityId(TEST_UUID)
                      .receiverAddress("receiver address")
                      .mailSubject("mailSubject")
                      .attachments(
                          Collections.singletonList(
                              MailAttachment.builder()
                                  .fileContent("xml")
                                  .fileName("test.xml")
                                  .build()))
                      .success(true)
                      .statusMessages(List.of("status-messages"))
                      .handoverDate(Instant.parse("2020-01-01T01:01:01.00Z"))
                      .build(),
                  HandoverReport.builder()
                      .content("<html>2019 Report</html>")
                      .receivedDate(Instant.parse("2019-01-01T01:01:01.00Z"))
                      .build()));

      List<EventRecord> responseBody =
          risWebClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/handover")
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new TypeReference<List<EventRecord>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(responseBody)
          .containsExactly(
              HandoverReport.builder()
                  .content("<html>2021 Report</html>")
                  .receivedDate(Instant.parse("2021-01-01T01:01:01Z"))
                  .build(),
              HandoverMail.builder()
                  .entityId(TEST_UUID)
                  .receiverAddress("receiver address")
                  .mailSubject("mailSubject")
                  .attachments(
                      Collections.singletonList(
                          MailAttachment.builder().fileContent("xml").fileName("test.xml").build()))
                  .success(true)
                  .statusMessages(List.of("status-messages"))
                  .handoverDate(Instant.parse("2020-01-01T01:01:01Z"))
                  .build(),
              HandoverReport.builder()
                  .content("<html>2019 Report</html>")
                  .receivedDate(Instant.parse("2019-01-01T01:01:01Z"))
                  .build());

      verify(handoverService).getEventLog(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
    }
  }

  @Test
  void testGetXmlPreview() throws DocumentationUnitNotExistsException {
    when(userService.getEmail(any(OidcUser.class))).thenReturn(ISSUER_ADDRESS);
    when(handoverService.createPreviewXml(TEST_UUID, true))
        .thenReturn(
            new XmlTransformationResult(
                "xml",
                true,
                List.of("status-messages"),
                "test.xml",
                Instant.parse("2020-01-01T01:01:01.00Z")));

    XmlTransformationResult responseBody =
        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-xml")
            .exchange()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectStatus()
            .isOk()
            .expectBody(XmlTransformationResult.class)
            .returnResult()
            .getResponseBody();
    assertThat(responseBody)
        .isEqualTo(
            XmlTransformationResult.builder()
                .xml("xml")
                .success(true)
                .statusMessages(List.of("status-messages"))
                .creationDate(Instant.parse("2020-01-01T01:01:01Z"))
                .fileName("test.xml")
                .build());

    verify(handoverService).createPreviewXml(TEST_UUID, true);
  }

  @Test
  void testSearchByLinkedDocumentationUnit() {
    RelatedDocumentationUnit linkedDocumentationUnit = RelatedDocumentationUnit.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(service.searchLinkableDocumentationUnits(
            linkedDocumentationUnit, docOffice, Optional.of("KORE0000000000"), false, pageRequest))
        .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/search-linkable-documentation-units?pg=0&sz=10&documentNumber=KORE0000000000")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(linkedDocumentationUnit)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .searchLinkableDocumentationUnits(
            linkedDocumentationUnit, docOffice, Optional.of("KORE0000000000"), false, pageRequest);
  }

  @Test
  void testSearchByDocumentationUnitListEntry() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(service.searchByDocumentationUnitSearchInput(
            pageRequest,
            oidcUser,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
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
            eq(pageRequest),
            any(OidcUser.class),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()),
            eq(Optional.empty()));
  }

  @Test
  void testGetHtml() throws DocumentationUnitNotExistsException {
    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Decision.builder()
                .attachments(Collections.singletonList(Attachment.builder().s3path("123").build()))
                .coreData(CoreData.builder().documentationOffice(docOffice).build())
                .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                .build());
    when(converterService.getConvertedObject("123")).thenReturn(null);

    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file?s3Path=123&format=")
        .exchange()
        .expectStatus()
        .isOk();

    // once by the AuthService and once by the controller asking the service
    verify(service, times(2)).getByUuid(TEST_UUID);
    verify(converterService).getConvertedObject("", "123", TEST_UUID);
  }

  @Nested
  class testOtherFileEndpoint {
    @Test
    void givenMissingMultipart_whenUploadingOtherFile_thenFail() {
      // given
      // when
      when(userHasWriteAccess.apply(any())).thenReturn(true);
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);

      // then
      risWebClient
          .withLogin("DS", "Internal")
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/other-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addHeader("X-Filename", "some-file.docx")
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void givenMissingHeaderParam_whenUploadingOtherFile_thenFail() {
      // given
      byte[] fileContent = "test content".getBytes();

      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "some-file.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              fileContent);

      // when
      when(userHasWriteAccess.apply(any())).thenReturn(true);
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);

      // then
      risWebClient
          .withLogin("DS", "Internal")
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/other-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void givenValidFile_whenUploadingOtherFile_thenVerifyCreated() {
      // given
      byte[] fileContent = "test content".getBytes();

      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "some-file.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              fileContent);

      // when
      when(userHasWriteAccess.apply(any())).thenReturn(true);
      when(userService.getUser(any(OidcUser.class))).thenReturn(user);

      // then
      risWebClient
          .withLogin("DS", "Internal")
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/other-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "some-file.docx")
          .exchange()
          .expectStatus()
          .isCreated();

      verify(attachmentService)
          .streamFileToDocumentationUnit(
              eq(TEST_UUID),
              any(InputStream.class),
              eq("some-file.docx"),
              eq(user),
              eq(AttachmentType.OTHER));
    }
  }

  //  @Nested
  //  class incorrectParamsTests {
  //    @Test
  //    void givenNoFileName_whenDownloading_then() {
  //      risWebClient
  //          .withDefaultLogin()
  //          .get()
  //          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
  //          .exchange()
  //          .expectStatus()
  //          .isBadRequest();
  //    }
  //  }

  @Nested
  class TakeoverDocumentUnit {
    @Test
    void testTakeoverDocumentationUnit_withSameDocOfficeAsDocUnit_shouldSucceed()
        throws DocumentationUnitNotExistsException {
      DocumentationOffice documentationOffice =
          DocumentationOffice.builder()
              .id(UUID.fromString("6be0bb1a-c196-484a-addf-822f2ab557f7"))
              .abbreviation("DS")
              .build();
      String documentNumber = "ABCD202200001";

      Decision decision =
          Decision.builder()
              .documentNumber(documentNumber)
              .status(
                  Status.builder()
                      .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                      .build())
              .coreData(CoreData.builder().documentationOffice(documentationOffice).build())
              .build();

      when(service.getByDocumentNumber(documentNumber)).thenReturn(decision);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/ABCD202200001/takeover")
          .exchange()
          .expectStatus()
          .isOk();
      verify(service, times(1)).takeOverDocumentationUnit(any(), any());
    }

    @Test
    void testTakeoverDocumentationUnit_withExternalUser_shouldBeForbidden()
        throws DocumentationUnitNotExistsException {
      when(userService.isInternal(any(OidcUser.class))).thenReturn(false);

      risWebClient
          .withExternalLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/ABCD202200001/takeover")
          .exchange()
          .expectStatus()
          .isForbidden();

      verify(service, times(0)).takeOverDocumentationUnit("ABCD202200001", null);
    }

    @Test
    void testTakeoverDocumentationUnit_withOtherDocOfficeAsDocUnit_shouldBeForbidden()
        throws DocumentationUnitNotExistsException {
      Mockito.reset(userService);
      DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
      when(userService.getDocumentationOffice(any())).thenReturn(office);
      String documentNumber = "ABCD202200001";
      Decision decision =
          Decision.builder()
              .documentNumber(documentNumber)
              .status(
                  Status.builder()
                      .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                      .build())
              .coreData(CoreData.builder().documentationOffice(office).build())
              .build();

      when(service.getByDocumentNumber(documentNumber)).thenReturn(decision);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/ABCD202200001/takeover")
          .exchange()
          .expectStatus()
          .isForbidden();

      verify(service, times(0)).takeOverDocumentationUnit(documentNumber, null);
    }
  }

  @Nested
  class CreateLdmlPreview {
    @Test
    void createLdmlPreview_withoutWriteAccess_shouldReturnIsForbidden()
        throws DocumentationUnitNotExistsException {
      // Arrange
      when(userHasWriteAccess.apply(TEST_UUID)).thenReturn(false);

      // Act + Assert
      risWebClient
          .withDefaultLogin()
          .get()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
          .exchange()
          .expectStatus()
          .isForbidden();

      verify(portalPublicationService, never()).createLdmlPreview(TEST_UUID);
    }

    @Test
    void createLdmlPreview_withMissingDocUnit_shouldReturnIsNotFound()
        throws DocumentationUnitNotExistsException {
      // Arrange
      doThrow(DocumentationUnitNotExistsException.class)
          .when(portalPublicationService)
          .createLdmlPreview(TEST_UUID);

      // Act + Assert
      risWebClient
          .withDefaultLogin()
          .get()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
          .exchange()
          .expectStatus()
          .isNotFound();

      verify(portalPublicationService).createLdmlPreview(TEST_UUID);
    }

    @Test
    void createLdmlPreview_withoutException_shouldBeSuccessful()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var expectedResult = LdmlTransformationResult.builder().ldml("ldml").success(true).build();
      when(portalPublicationService.createLdmlPreview(TEST_UUID)).thenReturn(expectedResult);

      // Act
      var result =
          risWebClient
              .withExternalLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(LdmlTransformationResult.class)
              .returnResult();

      // Assert
      verify(portalPublicationService).createLdmlPreview(TEST_UUID);
      assertThat(result.getResponseBody()).isEqualTo(expectedResult);
    }

    @Test
    void createLdmlPreview_withLdmlTransformationException_shouldReturnUnprocessableEntity()
        throws DocumentationUnitNotExistsException {
      // Arrange
      doThrow(new LdmlTransformationException("Could not parse transformed LDML as string.", null))
          .when(portalPublicationService)
          .createLdmlPreview(TEST_UUID);

      // Act
      var result =
          risWebClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
              .exchange()
              .expectStatus()
              .isUnprocessableEntity()
              .expectBody(LdmlTransformationResult.class)
              .returnResult();

      // Assert
      verify(portalPublicationService).createLdmlPreview(TEST_UUID);
      assertThat(result.getResponseBody())
          .isEqualTo(
              LdmlTransformationResult.builder()
                  .success(false)
                  .statusMessages(List.of("Could not parse transformed LDML as string."))
                  .build());
    }

    @Test
    void createLdmlPreview_withMappingException_shouldReturnUnprocessableEntity()
        throws DocumentationUnitNotExistsException {
      // Arrange
      doThrow(
              new MappingException(
                  "The element type ... must be terminated by the matchen end-tag ..."))
          .when(portalPublicationService)
          .createLdmlPreview(TEST_UUID);

      // Act
      var result =
          risWebClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
              .exchange()
              .expectStatus()
              .isUnprocessableEntity()
              .expectBody(LdmlTransformationResult.class)
              .returnResult();

      // Assert
      verify(portalPublicationService).createLdmlPreview(TEST_UUID);
      assertThat(result.getResponseBody())
          .isEqualTo(
              LdmlTransformationResult.builder()
                  .success(false)
                  .statusMessages(
                      List.of("The element type ... must be terminated by the matchen end-tag ..."))
                  .build());
    }

    @Test
    void createLdmlPreview_withRuntimeException_shouldReturnInternalServerError()
        throws DocumentationUnitNotExistsException {
      // Arrange
      doThrow(new RuntimeException("Any other exception..."))
          .when(portalPublicationService)
          .createLdmlPreview(TEST_UUID);

      // Act
      var result =
          risWebClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/preview-ldml")
              .exchange()
              .expectStatus()
              .is5xxServerError()
              .expectBody(LdmlTransformationResult.class)
              .returnResult();

      // Assert
      verify(portalPublicationService).createLdmlPreview(TEST_UUID);
      assertThat(result.getResponseBody())
          .isEqualTo(
              LdmlTransformationResult.builder()
                  .success(false)
                  .statusMessages(List.of("Any other exception..."))
                  .build());
    }
  }

  @Nested
  class PublishDocumentationUnit {
    @Test
    void testPublish_withServiceThrowsDocumentationUnitNotExistsException()
        throws DocumentationUnitNotExistsException {

      doThrow(DocumentationUnitNotExistsException.class)
          .when(portalPublicationService)
          .publishDocumentationUnitWithChangelog(TEST_UUID, null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
          .exchange()
          .expectStatus()
          .isNotFound();

      verify(portalPublicationService).publishDocumentationUnitWithChangelog(TEST_UUID, null);
    }

    @Test
    void testPublish_withServiceThrowsLdmlTransformationException()
        throws DocumentationUnitNotExistsException {

      doThrow(LdmlTransformationException.class)
          .when(portalPublicationService)
          .publishDocumentationUnitWithChangelog(TEST_UUID, null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
          .exchange()
          .expectStatus()
          .isBadRequest();

      verify(portalPublicationService).publishDocumentationUnitWithChangelog(TEST_UUID, null);
    }

    @Test
    void testPublish_withServiceThrowsPublishException()
        throws DocumentationUnitNotExistsException {

      doThrow(PublishException.class)
          .when(portalPublicationService)
          .publishDocumentationUnitWithChangelog(TEST_UUID, null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/publish")
          .exchange()
          .expectStatus()
          .is5xxServerError();

      verify(portalPublicationService).publishDocumentationUnitWithChangelog(TEST_UUID, null);
    }

    @Test
    void testWithdraw_withServiceThrowsDocumentationUnitNotExistsException()
        throws DocumentationUnitNotExistsException {

      doThrow(DocumentationUnitNotExistsException.class)
          .when(portalPublicationService)
          .withdrawDocumentationUnitWithChangelog(TEST_UUID, null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/withdraw")
          .exchange()
          .expectStatus()
          .isNotFound();

      verify(portalPublicationService).withdrawDocumentationUnitWithChangelog(TEST_UUID, null);
    }

    @Test
    void testWithdraw_withServiceThrowsChangelogException()
        throws DocumentationUnitNotExistsException {

      doThrow(ChangelogException.class)
          .when(portalPublicationService)
          .withdrawDocumentationUnitWithChangelog(TEST_UUID, null);

      risWebClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/withdraw")
          .exchange()
          .expectStatus()
          .is5xxServerError();

      verify(portalPublicationService).withdrawDocumentationUnitWithChangelog(TEST_UUID, null);
    }

    @Nested
    class UpdateDuplicateStatus {
      @Test
      void testUpdateDuplicateStatus_withValidStatus() throws DocumentationUnitNotExistsException {
        var docNumberOrigin = "documentNumber";
        var docNumberDuplicate = "duplicateNumb";
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        DocumentationOffice documentationOffice =
            DocumentationOffice.builder()
                .id(UUID.fromString("6be0bb1a-c196-484a-addf-822f2ab557f7"))
                .abbreviation("DS")
                .build();

        Decision decision =
            Decision.builder()
                .documentNumber(docNumberOrigin)
                .coreData(CoreData.builder().documentationOffice(documentationOffice).build())
                .build();

        when(service.getByDocumentNumber(docNumberOrigin)).thenReturn(decision);

        DuplicateRelationStatusRequest body =
            DuplicateRelationStatusRequest.builder()
                .status(DuplicateRelationStatus.IGNORED)
                .build();
        risWebClient
            .withDefaultLogin()
            .put()
            .uri(
                "/api/v1/caselaw/documentunits/"
                    + docNumberOrigin
                    + "/duplicate-status/"
                    + docNumberDuplicate)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk();
      }

      @Test
      void testUpdateDuplicateStatus_withInValidStatus()
          throws DocumentationUnitNotExistsException {
        var docNumberOrigin = "documentNumber";
        var docNumberDuplicate = "duplicateNumb";
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        DocumentationOffice documentationOffice =
            DocumentationOffice.builder()
                .id(UUID.fromString("6be0bb1a-c196-484a-addf-822f2ab557f7"))
                .abbreviation("DS")
                .build();

        Decision decision =
            Decision.builder()
                .documentNumber(docNumberOrigin)
                .coreData(CoreData.builder().documentationOffice(documentationOffice).build())
                .build();

        when(service.getByDocumentNumber(docNumberOrigin)).thenReturn(decision);

        String body =
            """
                { "status": "INVALID" }
                """;
        risWebClient
            .withDefaultLogin()
            .put()
            .uri(
                "/api/v1/caselaw/documentunits/"
                    + docNumberOrigin
                    + "/duplicate-status/"
                    + docNumberDuplicate)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyJsonString(body)
            .exchange()
            .expectStatus()
            .isBadRequest();
      }

      @Test
      void testUpdateDuplicateStatus_withNonExistingRelation()
          throws DocumentationUnitNotExistsException {
        var docNumberOrigin = "documentNumber";
        var docNumberDuplicate = "duplicateNumb";
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        DocumentationOffice documentationOffice =
            DocumentationOffice.builder()
                .id(UUID.fromString("6be0bb1a-c196-484a-addf-822f2ab557f7"))
                .abbreviation("DS")
                .build();

        Decision decision =
            Decision.builder()
                .documentNumber(docNumberOrigin)
                .coreData(CoreData.builder().documentationOffice(documentationOffice).build())
                .build();

        when(service.getByDocumentNumber(docNumberOrigin)).thenReturn(decision);

        when(duplicateCheckService.updateDuplicateStatus(any(), any(), any()))
            .thenThrow(EntityNotFoundException.class);

        DuplicateRelationStatusRequest body =
            DuplicateRelationStatusRequest.builder()
                .status(DuplicateRelationStatus.IGNORED)
                .build();

        risWebClient
            .withDefaultLogin()
            .put()
            .uri(
                "/api/v1/caselaw/documentunits/"
                    + docNumberOrigin
                    + "/duplicate-status/"
                    + docNumberDuplicate)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .is4xxClientError();
      }
    }

    @Nested
    class AssignDocumentationOffice {
      @Test
      void test_assignDocumentationOffice_withoutException_shouldSucceed()
          throws DocumentationUnitNotExistsException {
        // Arrange
        var result = "The documentation office 'Test' has been successfully assigned.";
        UUID documentationOfficeId = UUID.randomUUID();
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        when(service.assignDocumentationOffice(any(UUID.class), any(UUID.class), any()))
            .thenReturn(result);
        when(userHasWriteAccess.apply(any())).thenReturn(true);

        // Act
        risWebClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/assign/" + documentationOfficeId)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .consumeWith(
                response -> {
                  // Assert
                  assertThat(response.getResponseBody()).isEqualTo(result);
                });
      }

      @Test
      void
          test_assignDocumentationOffice_withDocumentationUnitNotExistsException_shouldReturnNotFound()
              throws DocumentationUnitNotExistsException {
        // Arrange
        var result = "Documentation unit not found";
        UUID documentationOfficeId = UUID.randomUUID();
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        when(service.assignDocumentationOffice(any(UUID.class), any(UUID.class), any()))
            .thenThrow(DocumentationUnitNotExistsException.class);
        when(userHasWriteAccess.apply(any())).thenReturn(true);

        // Act
        risWebClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/assign/" + documentationOfficeId)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(String.class)
            .consumeWith(
                response -> {
                  // Assert
                  assertThat(response.getResponseBody()).isEqualTo(result);
                });
      }

      @Test
      void
          test_assignDocumentationOffice_withDocumentationOfficeNotExistsException_shouldReturnNotFound()
              throws DocumentationUnitNotExistsException {
        // Arrange
        var result = "Documentation office not found";
        UUID documentationOfficeId = UUID.randomUUID();
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        when(service.assignDocumentationOffice(any(UUID.class), any(UUID.class), any()))
            .thenThrow(DocumentationOfficeNotExistsException.class);
        when(userHasWriteAccess.apply(any())).thenReturn(true);

        // Act
        risWebClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/assign/" + documentationOfficeId)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(String.class)
            .consumeWith(
                response -> {
                  // Assert
                  assertThat(response.getResponseBody()).isEqualTo(result);
                });
      }

      @Test
      void test_assignDocumentationOffice_withDocumentationUnitException_shouldReturnBadRequest()
          throws DocumentationUnitNotExistsException {
        // Arrange
        UUID documentationOfficeId = UUID.randomUUID();
        var result =
            String.format(
                "Error assigning documentation office %s to %s", TEST_UUID, documentationOfficeId);
        when(userService.isInternal(any(OidcUser.class))).thenReturn(true);
        when(service.assignDocumentationOffice(any(UUID.class), any(UUID.class), any()))
            .thenThrow(new DocumentationUnitException(result));
        when(userHasWriteAccess.apply(any())).thenReturn(true);

        // Act
        risWebClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/assign/" + documentationOfficeId)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(String.class)
            .consumeWith(
                response -> {
                  // Assert
                  assertThat(response.getResponseBody()).isEqualTo(result);
                });
      }
    }

    @Nested
    class GetImage {
      @Test
      void testGetImage_shouldReturnCorrectContentType()
          throws DocumentationUnitNotExistsException, ImageNotExistsException {
        when(service.getByDocumentNumber("ABCD202200001"))
            .thenReturn(
                Decision.builder()
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                    .build());
        when(service.getImageBytes("ABCD202200001", "bild.jpg"))
            .thenReturn(
                Image.builder()
                    .name("bild.jpg")
                    .contentType("jpg")
                    .content("imageContent".getBytes())
                    .build());

        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/ABCD202200001/image/bild.jpg")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.parseMediaType("image/jpg"))
            .expectHeader()
            .cacheControl(Duration.ofDays(1))
            .expectBody(byte[].class);

        // once by the AuthService and once by the controller asking the service
        verify(service, times(1)).getImageBytes("ABCD202200001", "bild.jpg");
      }

      @Test
      void testGetImage_shouldReturnNotFoundForMissingImage()
          throws DocumentationUnitNotExistsException, ImageNotExistsException {
        when(service.getByDocumentNumber("ABCD202200001"))
            .thenReturn(
                Decision.builder()
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                    .build());
        when(service.getImageBytes("ABCD202200001", "bild.jpg"))
            .thenThrow(new ImageNotExistsException("Image not found"));

        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/ABCD202200001/image/bild.abc")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .exchange()
            .expectStatus()
            .isNotFound();

        // once by the AuthService and once by the controller asking the service
        verify(service, times(1)).getImageBytes("ABCD202200001", "bild.abc");
      }
    }

    @Nested
    class BulkAssignProcessStep {
      private final UUID documentationUnitId1 = UUID.randomUUID();
      private final UUID documentationUnitId2 = UUID.randomUUID();
      private final List<UUID> documentationUnitIds =
          List.of(documentationUnitId1, documentationUnitId2);

      private final DocumentationUnitProcessStep processStep =
          DocumentationUnitProcessStep.builder()
              .processStep(ProcessStep.builder().uuid(UUID.randomUUID()).name("NewStep").build())
              .user(User.builder().id(UUID.randomUUID()).firstName("Test").lastName("User").build())
              .build();

      @Test
      void testBulkAssignProcessStep_withValidRequest_shouldSucceed() throws Exception {
        doReturn(user).when(userService).getUser(any(OidcUser.class));
        BulkAssignProcessStepRequest requestBody =
            new BulkAssignProcessStepRequest(processStep, documentationUnitIds);

        risWebClient
            .withDefaultLogin()
            .patch()
            .uri("/api/v1/caselaw/documentunits/bulk-assign-process-step")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus()
            .isOk();

        verify(userService, times(1)).getUser(any(OidcUser.class));
        verify(abstractService, times(1))
            .bulkAssignProcessStep(documentationUnitIds, processStep, user);
      }

      @Test
      void testBulkAssignProcessStep_withDocumentationUnitNotExistsException_shouldReturnNotFound()
          throws Exception {
        BulkAssignProcessStepRequest requestBody =
            new BulkAssignProcessStepRequest(processStep, documentationUnitIds);

        doThrow(DocumentationUnitNotExistsException.class)
            .when(abstractService)
            .bulkAssignProcessStep(any(), any(), any());

        risWebClient
            .withDefaultLogin()
            .patch()
            .uri("/api/v1/caselaw/documentunits/bulk-assign-process-step")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus()
            .isNotFound();
      }

      @Test
      void testBulkAssignProcessStep_withBadRequestException_shouldReturnBadRequest()
          throws Exception {
        BulkAssignProcessStepRequest requestBody =
            new BulkAssignProcessStepRequest(processStep, documentationUnitIds);

        doThrow(BadRequestException.class)
            .when(abstractService)
            .bulkAssignProcessStep(any(), any(), any());

        risWebClient
            .withDefaultLogin()
            .patch()
            .uri("/api/v1/caselaw/documentunits/bulk-assign-process-step")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus()
            .isBadRequest();
      }

      @Test
      void testBulkAssignProcessStep_withForbiddenAccess_shouldReturnForbidden() throws Exception {
        BulkAssignProcessStepRequest requestBody =
            new BulkAssignProcessStepRequest(processStep, documentationUnitIds);

        when(userHasBulkWriteAccess.apply(any())).thenReturn(false);

        risWebClient
            .withDefaultLogin()
            .patch()
            .uri("/api/v1/caselaw/documentunits/bulk-assign-process-step")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus()
            .isForbidden();

        verify(abstractService, never()).bulkAssignProcessStep(any(), any(), any());
      }

      @Test
      void testBulkAssignProcessStep_withInvalidBody_shouldReturnBadRequest() throws Exception {
        BulkAssignProcessStepRequest requestBody =
            new BulkAssignProcessStepRequest(processStep, null);

        risWebClient
            .withDefaultLogin()
            .patch()
            .uri("/api/v1/caselaw/documentunits/bulk-assign-process-step")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus()
            .isBadRequest();

        verify(abstractService, never()).bulkAssignProcessStep(any(), any(), any());
      }
    }
  }
}
