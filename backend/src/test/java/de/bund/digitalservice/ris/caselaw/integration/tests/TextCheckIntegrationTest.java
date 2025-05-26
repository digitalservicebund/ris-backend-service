package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.TextCheckController;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseIgnoredTextCheckWordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresIgnoredTextCheckWordRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRequest;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentationUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresIgnoredTextCheckWordRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      TextCheckService.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class
    },
    controllers = {TextCheckController.class})
@Sql(scripts = {"classpath:text_check_init.sql"})
@Sql(
    scripts = {"classpath:text_check_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class TextCheckIntegrationTest {
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
  @Autowired private DatabaseIgnoredTextCheckWordRepository repository;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockitoBean private S3AsyncClient s3AsyncClient;
  @MockitoBean private MailService mailService;
  @MockitoBean private DocxConverterService docxConverterService;
  @MockitoBean private UserService userService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private StagingPortalPublicationService stagingPortalPublicationService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private FmxService fmxService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  DocumentationUnitDTO documentationUnitDTO;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890";

  @BeforeEach
  void setUp() {
    DocumentationOfficeDTO documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    when(userService.getDocumentationOffice(any())).thenReturn(docOffice);

    documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOffice,
            DEFAULT_DOCUMENT_NUMBER + Math.random() * 1000);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testAddAndRemoveLocalIgnore() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("abc"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(IgnoredTextCheckWord.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().id()).isNotNull();
              assertThat(response.getResponseBody().word()).isEqualTo("abc");
              assertThat(response.getResponseBody().type())
                  .isEqualTo(IgnoredTextCheckType.DOCUMENTATION_UNIT);
            });

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word/abc")
        .exchange()
        .expectStatus()
        .isOk();
  }

  // 'xyz' is added as a globally ignored word via SQL script
  // this test verifies that it can be added locally at doc unit level, too
  @Test
  void testAddLocalIgnore_canAddGloballyIgnoredWordsLocally() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("xyz"))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void testAddAndRemoveGlobalIgnore() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/api/v1/caselaw/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("def"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(IgnoredTextCheckWord.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().id()).isNotNull();
              assertThat(response.getResponseBody().word()).isEqualTo("def");
              assertThat(response.getResponseBody().type()).isEqualTo(IgnoredTextCheckType.GLOBAL);
            });

    // assert the global word has been saved and the documentation office is set correctly
    assertThat(
            repository
                .findByDocumentationUnitIdOrByGlobalWords(null, List.of("def"))
                .getFirst()
                .getDocumentationOffice()
                .getId())
        .isEqualTo(docOffice.id());

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/text-check/ignored-word/def")
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(repository.findByDocumentationUnitIdOrByGlobalWords(null, List.of("def"))).isEmpty();
  }

  @Test
  void testAddGlobalIgnore_shouldNotBeAbleToAddTwice() {
    for (int i = 0; i < 2; i++) {
      risWebTestClient
          .withDefaultLogin()
          .post()
          .uri("/api/v1/caselaw/text-check/ignored-word")
          .bodyValue(new IgnoredTextCheckWordRequest("hij"))
          .exchange()
          .expectStatus()
          .isOk();
    }

    assertThat(repository.findByDocumentationUnitIdOrByGlobalWords(null, List.of("hij")))
        .hasSize(1);
  }

  @Test
  void testGlobalJdvIgnore_cantBeDeleted() {
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/text-check/ignored-word/uvw")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
