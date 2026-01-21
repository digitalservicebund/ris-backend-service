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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

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

  @Test
  void testAttachDocxToDocumentationUnit() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3ClientToReturnFile(attachment);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withDefaultLogin(oidcLoggedInUserId)
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
        .exchange()
        .expectStatus()
        .isOk();

    var savedAttachment = attachmentRepository.findAllByDocumentationUnitId(dto.getId()).get(0);
    assertThat(savedAttachment.getUploadTimestamp()).isInstanceOf(Instant.class);
    assertThat(savedAttachment.getId()).isInstanceOf(UUID.class);

    User user = User.builder().documentationOffice(docOffice).build();
    var logs = historyLogService.getHistoryLogs(dto.getId(), user);
    assertThat(logs).hasSize(2);
    assertThat(logs)
        .map(HistoryLog::eventType)
        .containsExactly(HistoryLogEventType.UPDATE, HistoryLogEventType.FILES);
    assertThat(logs).map(HistoryLog::createdBy).containsExactly("testUser", "testUser");
    assertThat(logs.get(1).description()).isEqualTo("Word-Dokument hinzugefügt");
  }

  @Test
  void testAttachDocxToDocumentationUnit_shouldSetManagementData() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3ClientToReturnFile(attachment);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
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
  void testAttachDocxToDocumentationUnit_shouldSetManagementDataForOtherDocOffice()
      throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3ClientToReturnFile(attachment);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
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
  void testAttachMultipleDocxToDocumentationUnitSequentially() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3ClientToReturnFile(attachment);

    DocumentationUnitDTO documentationUnitDto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
        .exchange()
        .expectStatus()
        .isOk();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
        .exchange()
        .expectStatus()
        .isOk();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(attachmentRepository.findAllByDocumentationUnitId(documentationUnitDto.getId()))
        .hasSize(3);
  }

  @Test
  void testAttachFileToDocumentationUnit_withInvalidUuid() {
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndSetItInUnitIfNotSet()
      throws IOException {
    var attachmentWithEcli =
        Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment_ecli.docx"));
    mockS3ClientToReturnFile(attachmentWithEcli);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachmentWithEcli)
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
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndNotChangeTheECLIInUnitIfECLIIsSet()
          throws IOException {
    var attachmentWithEcli =
        Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment_ecli.docx"));
    mockS3ClientToReturnFile(attachmentWithEcli);

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
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachmentWithEcli)
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
      testAttachFileToDocumentationUnit_withMetadataProperties_shouldExtractCoreDataAndShouldNotOverrideFields()
          throws IOException {
    TestTransaction.end();
    var attachmentWithMetadata =
        Files.readAllBytes(Paths.get("src/test/resources/fixtures/with_metadata.docx"));
    mockS3ClientToReturnFile(attachmentWithMetadata);

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
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachmentWithMetadata)
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
  void testRemoveFileFromDocumentationUnit_shouldReturnLastUpdatedByWithName() {
    // Arrange
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(DeleteObjectResponse.builder().build());

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

    // Act
    risWebTestClient
        .withDefaultLogin(oidcLoggedInUserId)
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/fooPath")
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
    assertThat(logs.getFirst().description()).isEqualTo("Word-Dokument gelöscht");
  }

  @Test
  void testRemoveFileFromDocumentationUnit_shouldReturnLastUpdatedByWithoutName() {
    // Arrange
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(DeleteObjectResponse.builder().build());

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

    // Act
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/fooPath")
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
  void testAttachFileToDocumentationUnit_withExternalUser_shouldBeForbidden() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3ClientToReturnFile(attachment);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, dsDocOffice, "1234567890123");

    risWebTestClient
        .withExternalLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .bodyAsByteArray(attachment)
        .exchange()
        .expectStatus()
        .isForbidden();

    assertThat(attachmentRepository.findAllByDocumentationUnitId(dto.getId())).isEmpty();
  }

  @Test
  void testRemoveFileFromDocumentationUnit_withInvalidUuid_shouldFail() {
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/abc/file/fooPath")
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
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/fooPath")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private void mockS3ClientToReturnFile(byte[] file) {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    when(s3Client.getObject(
            any(GetObjectRequest.class),
            Mockito
                .<ResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>any()))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), file));
  }
}
