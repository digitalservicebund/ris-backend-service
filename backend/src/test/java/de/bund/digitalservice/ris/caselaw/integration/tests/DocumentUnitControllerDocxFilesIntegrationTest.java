package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.S3AttachmentService;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverter;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      S3AttachmentService.class,
      DocxConverterService.class,
      DocxConverter.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitControllerDocxFilesIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository courtRepository;
  @Autowired private AttachmentService attachmentService;
  @Autowired private AttachmentRepository attachmentRepository;
  @Autowired private DocxConverterService docxConverterService;

  @SpyBean private DocumentUnitService service;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private UserService userService;
  @MockBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private DocumentBuilderFactory documentBuilderFactory;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @BeforeEach
  void setUp() {
    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()).getId();

    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    attachmentRepository.deleteAll();
  }

  @Test
  void testAttachDocxToDocumentationUnit() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3AsyncClientToReturnFile(attachment);

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

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

    var savedAttachment = attachmentRepository.findAllByDocumentationUnitId(dto.getId()).get(0);
    assertThat(savedAttachment.getUploadTimestamp()).isInstanceOf(Instant.class);
    assertThat(savedAttachment.getId()).isInstanceOf(UUID.class);
  }

  @Test
  void testAttachMultipleDocxToDocumentationUnitSequentially() throws IOException {
    var attachment = Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment.docx"));
    mockS3AsyncClientToReturnFile(attachment);

    DocumentationUnitDTO documentationUnitDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

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
    mockS3AsyncClientToReturnFile(attachmentWithEcli);

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

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

    DocumentationUnitDTO savedDTO = repository.findById(dto.getId()).get();
    assertThat(savedDTO.getEcli()).isEqualTo("ECLI:DE:BGH:2023:210423UVZR86.22.0");
  }

  @Test
  void
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndNotChangeTheECLIInUnitIfECLIIsSet()
          throws IOException {
    var attachmentWithEcli =
        Files.readAllBytes(Paths.get("src/test/resources/fixtures/attachment_ecli.docx"));
    mockS3AsyncClientToReturnFile(attachmentWithEcli);

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .ecli("oldEcli")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

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

    DocumentationUnitDTO savedDTO = repository.findById(dto.getId()).get();
    assertThat(savedDTO.getEcli()).isEqualTo("oldEcli");
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(new CompletableFuture<>());

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    attachmentRepository.save(
        AttachmentDTO.builder()
            .s3ObjectPath("fooPath")
            .documentationUnit(dto)
            .uploadTimestamp(Instant.now())
            .filename("fooFile")
            .format("docx")
            .build());

    assertThat(attachmentRepository.findAll()).hasSize(1);

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file/fooPath")
        .exchange()
        .expectStatus()
        .isNoContent();

    assertThat(attachmentRepository.findAll()).isEmpty();
  }

  @Test
  void testRemoveFileFromDocumentUnit_withInvalidUuid() {
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  private void mockS3AsyncClientToReturnFile(byte[] file) throws IOException {
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    when(s3AsyncClient.getObject(
            any(GetObjectRequest.class),
            Mockito
                .<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>
                    any()))
        .thenReturn(
            CompletableFuture.completedFuture(
                ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), file)));
  }
}
