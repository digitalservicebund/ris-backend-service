package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxMetadataProperty;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

class DocumentationUnitControllerDocxFilesIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private AttachmentRepository attachmentRepository;
  @MockitoSpyBean private DocumentationUnitDocxMetadataInitializationService service;
  @Autowired private DocumentationUnitHistoryLogService historyLogService;
  private final UUID oidcLoggedInUserId = UUID.randomUUID();
  private final DocumentationOffice docOffice = buildDSDocOffice();

  @MockitoBean
  @Qualifier("docxS3Client")
  private S3Client s3Client;

  private DocumentationOfficeDTO dsDocOffice = null;

  @BeforeEach
  void setUp() {
    dsDocOffice = documentationOfficeRepository.findByAbbreviation("DS");
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    attachmentRepository.deleteAll();
    databaseCourtRepository.deleteAll();
  }

  @Nested
  class AttachOriginalFileToDocumentationUnit {
    @Test
    void testAttachOriginalFileToDocumentationUnit_shouldSucceed() throws IOException {
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachment);
      mockS3Client(attachment);

      risWebTestClient
          .withDefaultLogin(oidcLoggedInUserId)
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      var savedAttachment =
          attachmentRepository.findAllByDocumentationUnitId(dto.getId()).getFirst();
      assertThat(savedAttachment.getUploadTimestamp()).isInstanceOf(Instant.class);
      assertThat(savedAttachment.getId()).isInstanceOf(UUID.class);

      User user = User.builder().documentationOffice(docOffice).build();
      var logs = historyLogService.getHistoryLogs(dto.getId(), user);
      assertThat(logs).hasSize(2);
      assertThat(logs)
          .map(HistoryLog::eventType)
          .containsExactly(HistoryLogEventType.UPDATE, HistoryLogEventType.FILES);
      assertThat(logs).map(HistoryLog::createdBy).containsExactly("testUser", "testUser");
      assertThat(logs.get(1).description()).isEqualTo("Originaldokument hinzugefügt");
    }

    @Test
    void testAttachOriginalFileToDocumentationUnit_shouldSetManagementData() throws IOException {
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachment);
      mockS3Client(attachment);
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      var docUnit =
          risWebTestClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + dto.getDocumentNumber())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(Decision.class)
              .returnResult()
              .getResponseBody();

      ManagementData managementData = docUnit.managementData();
      assertThat(managementData.lastUpdatedByName()).isEqualTo("testUser");
      assertThat(managementData.lastUpdatedByDocOffice()).isEqualTo("DS");
      assertThat(managementData.lastUpdatedAtDateTime())
          .isBetween(Instant.now().minusSeconds(10), Instant.now());
    }

    @Test
    void testAttachOriginalFileToDocumentationUnit_shouldSetManagementDataForOtherDocOffice()
        throws IOException {
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachment);
      mockS3Client(attachment);

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      var docUnit =
          risWebTestClient
              .withLogin("/BGH")
              .get()
              .uri("/api/v1/caselaw/documentunits/" + dto.getDocumentNumber())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(Decision.class)
              .returnResult()
              .getResponseBody();

      ManagementData managementData = docUnit.managementData();
      assertThat(managementData.lastUpdatedByName()).isNull();
      assertThat(managementData.lastUpdatedByDocOffice()).isEqualTo("DS");
      assertThat(managementData.lastUpdatedAtDateTime())
          .isBetween(Instant.now().minusSeconds(10), Instant.now());
    }

    @Test
    void testAttachOriginalFileToDocumentationUnit_withMultipleDocx_shouldAddSequentially()
        throws IOException {
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachment);
      mockS3Client(attachment);

      DocumentationUnitDTO documentationUnitDto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isOk();

      assertThat(attachmentRepository.findAllByDocumentationUnitId(documentationUnitDto.getId()))
          .hasSize(3);
    }

    @Test
    void testAttachOriginalFileToDocumentationUnit_withInvalidUuid() {
      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/abc/original-file")
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void
        testAttachOriginalFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndSetItInUnitIfNotSet()
            throws IOException {
      var attachmentWithEcli =
          Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment_ecli.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachmentWithEcli);
      mockS3Client(attachmentWithEcli);

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment_with_ecli.docx")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(Docx2Html.class)
          .consumeWith(
              response -> {
                assertThat(response.getResponseBody()).isNotNull();
                assertThat(response.getResponseBody().ecliList())
                    .containsExactly("ECLI:DE:BGH:2023:210423UVZR86.22.0");
              });

      DecisionDTO savedDTO = (DecisionDTO) repository.findById(dto.getId()).get();
      assertThat(savedDTO.getEcli()).isEqualTo("ECLI:DE:BGH:2023:210423UVZR86.22.0");
    }

    @Test
    void
        testAttachOriginalFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndNotChangeTheECLIInUnitIfECLIIsSet()
            throws IOException {
      var attachmentWithEcli =
          Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment_ecli.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachmentWithEcli);
      mockS3Client(attachmentWithEcli);
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(
              repository,
              DecisionDTO.builder()
                  .documentNumber("1234567890123")
                  .ecli("oldEcli")
                  .documentationOffice(dsDocOffice));

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment_with_ecli.docx")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(Docx2Html.class)
          .consumeWith(
              response -> {
                assertThat(response.getResponseBody()).isNotNull();
                assertThat(response.getResponseBody().ecliList())
                    .containsExactly("ECLI:DE:BGH:2023:210423UVZR86.22.0");
              });

      DecisionDTO savedDTO = (DecisionDTO) repository.findById(dto.getId()).get();
      assertThat(savedDTO.getEcli()).isEqualTo("oldEcli");
    }

    @Test
    // Needed to lazy-load procedure
    @Transactional
    void
        testAttachOriginalFileToDocumentationUnit_withMetadataProperties_shouldExtractCoreDataAndShouldNotOverrideFields()
            throws IOException {
      TestTransaction.end();
      var attachmentWithMetadata =
          Files.readAllBytes(Paths.get("src/test/resources/fixtures/with_metadata.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachmentWithMetadata);
      mockS3Client(attachmentWithMetadata);
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(
              repository,
              DecisionDTO.builder()
                  .documentNumber("1234567890123")
                  .legalEffect(LegalEffectDTO.JA) // file has "Nein"
                  .judicialBody("1. Senat") // file has "2. Senat"
                  .court(null) // file has BFH
                  .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS")));

      databaseCourtRepository.save(
          CourtDTO.builder().type("BFH").isForeignCourt(true).isSuperiorCourt(false).build());

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment_with_metadata.docx")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(Docx2Html.class)
          .consumeWith(
              response -> {
                assertThat(response.getResponseBody()).isNotNull();
                assertThat(response.getResponseBody().properties())
                    .containsAllEntriesOf(
                        Map.of(
                            DocxMetadataProperty.APPRAISAL_BODY,
                            "2. Senat",
                            DocxMetadataProperty.FILE_NUMBER,
                            "II B 29/24",
                            DocxMetadataProperty.LEGAL_EFFECT,
                            "Nein",
                            DocxMetadataProperty.COURT_TYPE,
                            "BFH"));
              });

      TestTransaction.start();
      DecisionDTO savedDTO = (DecisionDTO) repository.findById(dto.getId()).get();
      assertThat(savedDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.JA); // kept old value
      assertThat(savedDTO.getJudicialBody()).isEqualTo("1. Senat"); // kept old value
      assertThat(savedDTO.getCourt().getType())
          .isEqualTo("BFH"); // added court based on docx properties
      TestTransaction.end();
    }

    @Test
    void testAttachOriginalFileToDocumentationUnit_withExternalUser_shouldBeForbidden()
        throws IOException {
      var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "attachment.docx",
              String.valueOf(MediaType.APPLICATION_OCTET_STREAM),
              attachment);
      mockS3Client(attachment);
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      risWebTestClient
          .withExternalLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/original-file")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .addFile(mockFile)
          .addHeader("X-Filename", "attachment.docx")
          .exchange()
          .expectStatus()
          .isForbidden();

      assertThat(attachmentRepository.findAllByDocumentationUnitId(dto.getId())).isEmpty();
    }
  }

  @Nested
  class RemoveAttachmentFromDocumentationUnit {
    @Test
    void
        testRemoveFileFromDocumentationUnit_withOtherAttachmentType_shouldReturnLastUpdatedByWithName() {
      // Arrange
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenReturn(DeleteObjectResponse.builder().build());

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      var attachment =
          attachmentRepository.save(
              AttachmentDTO.builder()
                  .s3ObjectPath("fooPath")
                  .documentationUnit(dto)
                  .uploadTimestamp(Instant.now())
                  .filename("fooFile.docx")
                  .format("docx")
                  .attachmentType(AttachmentType.OTHER.name())
                  .build());

      assertThat(attachmentRepository.findAll()).hasSize(1);

      // Act
      risWebTestClient
          .withDefaultLogin(oidcLoggedInUserId)
          .delete()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/" + attachment.getId())
          .exchange()
          .expectStatus()
          .isNoContent();

      var docUnit =
          risWebTestClient
              .withDefaultLogin(oidcLoggedInUserId)
              .get()
              .uri("/api/v1/caselaw/documentunits/" + dto.getDocumentNumber())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(Decision.class)
              .returnResult()
              .getResponseBody();

      // Assert
      ManagementData managementData = docUnit.managementData();
      assertThat(managementData.lastUpdatedByName()).isEqualTo("testUser");
      assertThat(managementData.lastUpdatedByDocOffice()).isEqualTo("DS");
      assertThat(managementData.lastUpdatedAtDateTime())
          .isBetween(Instant.now().minusSeconds(10), Instant.now());

      User user = User.builder().documentationOffice(docOffice).build();
      var logs = historyLogService.getHistoryLogs(dto.getId(), user);
      assertThat(logs).hasSize(1);
      assertThat(logs.getFirst().eventType()).isEqualTo(HistoryLogEventType.FILES);
      assertThat(logs.getFirst().createdBy()).isEqualTo("testUser");
      assertThat(logs.getFirst().description()).isEqualTo("Anhang \"fooFile.docx\" gelöscht");
    }

    @Test
    void testRemoveFileFromDocumentationUnit_shouldReturnLastUpdatedByWithoutName() {
      // Arrange
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenReturn(DeleteObjectResponse.builder().build());

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      var attachment =
          attachmentRepository.save(
              AttachmentDTO.builder()
                  .s3ObjectPath("fooPath")
                  .documentationUnit(dto)
                  .uploadTimestamp(Instant.now())
                  .filename("fooFile")
                  .format("docx")
                  .attachmentType(AttachmentType.OTHER.name())
                  .build());

      assertThat(attachmentRepository.findAll()).hasSize(1);

      // Act
      risWebTestClient
          .withDefaultLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/" + attachment.getId())
          .exchange()
          .expectStatus()
          .isNoContent();

      var docUnit =
          risWebTestClient
              .withLogin("/BGH")
              .get()
              .uri("/api/v1/caselaw/documentunits/" + dto.getDocumentNumber())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(Decision.class)
              .returnResult()
              .getResponseBody();

      // Assert
      ManagementData managementData = docUnit.managementData();
      assertThat(managementData.lastUpdatedByName()).isNull();
      assertThat(managementData.lastUpdatedByDocOffice()).isEqualTo("DS");
      assertThat(managementData.lastUpdatedAtDateTime())
          .isBetween(Instant.now().minusSeconds(10), Instant.now());
    }

    @Test
    void testRemoveFileFromDocumentationUnit_withInvalidUuid_shouldFail() {
      var fileId = UUID.randomUUID();
      risWebTestClient
          .withDefaultLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/abc/file/" + fileId)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void testRemoveFileFromDocumentationUnit_withExternalUser_shouldBeForbidden() {
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenReturn(DeleteObjectResponse.builder().build());

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      var attachment =
          attachmentRepository.save(
              AttachmentDTO.builder()
                  .s3ObjectPath("fooPath")
                  .documentationUnit(dto)
                  .uploadTimestamp(Instant.now())
                  .filename("fooFile")
                  .format("docx")
                  .attachmentType(AttachmentType.OTHER.name())
                  .build());

      assertThat(attachmentRepository.findAll()).hasSize(1);
      risWebTestClient
          .withExternalLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/" + attachment.getId())
          .exchange()
          .expectStatus()
          .isForbidden();
    }

    @Test
    void testRemoveDocumentationUnit_shouldRemoveAttachments() {
      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

      attachmentRepository.save(
          AttachmentDTO.builder()
              .s3ObjectPath("fooPath")
              .documentationUnit(dto)
              .uploadTimestamp(Instant.now())
              .filename("fooFile")
              .format("docx")
              .attachmentType(AttachmentType.OTHER.name())
              .build());

      assertThat(attachmentRepository.findAll()).hasSize(1);
      risWebTestClient
          .withDefaultLogin()
          .delete()
          .uri("/api/v1/caselaw/documentunits/" + dto.getId())
          .exchange()
          .expectStatus()
          .isOk();
      assertThat(attachmentRepository.findAll()).isEmpty();
    }
  }

  private void mockS3Client(byte[] file) {
    when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-123").build());
    when(s3Client.createMultipartUpload(any(Consumer.class)))
        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-123").build());
    when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
        .thenReturn(UploadPartResponse.builder().eTag("etag").build());
    when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
        .thenReturn(CompleteMultipartUploadResponse.builder().build());
    when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
        .thenReturn(UploadPartResponse.builder().eTag("etag").build());
    when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
        .thenReturn(CompleteMultipartUploadResponse.builder().build());

    var getObjectResponse =
        GetObjectResponse.builder()
            .contentType("application/octet-stream")
            .contentLength((long) file.length)
            .build();

    ResponseBytes<GetObjectResponse> responseBytes =
        ResponseBytes.fromByteArray(getObjectResponse, file);

    when(s3Client.getObject(
            (GetObjectRequest) any(), (ResponseTransformer<GetObjectResponse, Object>) any()))
        .thenReturn(responseBytes);
  }
}
