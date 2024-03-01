package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

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
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitControllerDocxFileIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14")
          .withInitScript("db/create_migration_scheme_and_extensions.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @SpyBean private DocumentUnitService service;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;

  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository courtRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @Captor private ArgumentCaptor<ByteBuffer> captor;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @BeforeEach
  void setUp() {
    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()).getId();

    doReturn(Mono.just(docOffice))
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
  }

  @Test
  void testAttachFileToDocumentUnit() {
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
    doReturn(Mono.empty())
        .when(service)
        .attachFileToDocumentUnit(eq(dto.getId()), any(ByteBuffer.class), any(HttpHeaders.class));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .attachFileToDocumentUnit(eq(dto.getId()), captor.capture(), headersCaptor.capture());
    assertThat(captor.getValue().array()).isEmpty();
    assertThat(headersCaptor.getValue().getContentLength()).isZero();
  }

  @Test
  void testAttachFileToDocumentUnit_withInvalidUuid() {
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndSetItInUnitIfNotSet() {
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    Docx2Html docx2Html = new Docx2Html("html", List.of("ecli"));

    doReturn(Mono.just(DocumentUnit.builder().s3path("filename").build()))
        .when(service)
        .attachFileToDocumentUnit(eq(dto.getId()), any(ByteBuffer.class), any(HttpHeaders.class));
    when(docxConverterService.getConvertedObject("filename")).thenReturn(Mono.just(docx2Html));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Docx2Html.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().ecliList()).containsExactly("ecli");
            });

    verify(service).updateECLI(dto.getId(), docx2Html);

    DocumentationUnitDTO savedDTO = repository.findById(dto.getId()).get();
    assertThat(savedDTO.getEcli()).isEqualTo("ecli");
  }

  @Test
  void
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndNotChangeTheECLIInUnitIfECLIIsSet() {
    Docx2Html docx2Html = new Docx2Html("html", List.of("oldEcli"));
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .ecli("oldEcli")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    doReturn(Mono.just(DocumentUnit.builder().s3path("filename").build()))
        .when(service)
        .attachFileToDocumentUnit(eq(dto.getId()), any(ByteBuffer.class), any(HttpHeaders.class));
    when(docxConverterService.getConvertedObject("filename")).thenReturn(Mono.just(docx2Html));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Docx2Html.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().ecliList()).containsExactly("oldEcli");
            });

    verify(service).updateECLI(dto.getId(), docx2Html);

    DocumentationUnitDTO savedDTO = repository.findById(dto.getId()).get();
    assertThat(savedDTO.getEcli()).isEqualTo("oldEcli");
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    when(service.removeFileFromDocumentUnit(dto.getId())).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocumentUnit(dto.getId());
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
}
