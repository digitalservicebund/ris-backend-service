package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.HandoverMailService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.LdmlExporterService;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalEditionRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.ScheduledPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      HandoverService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      KeycloakUserService.class,
      ScheduledPublicationService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresLegalPeriodicalEditionRepositoryImpl.class,
      PostgresLegalPeriodicalRepositoryImpl.class,
      PostgresHandoverRepositoryImpl.class,
      PostgresHandoverReportRepositoryImpl.class,
      HandoverMailService.class,
      DatabaseDocumentationUnitStatusService.class,
      LegalPeriodicalEditionService.class,
      MockXmlExporter.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentationUnitController.class})
@TestPropertySource(properties = {"mail.exporter.recipientAddress=neuris@example.com"})
class ScheduledPublicationIntegrationTest {
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
  @Autowired private DatabaseDocumentationUnitRepository docUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private HttpMailSender mailSender;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;
  @MockBean private ProcedureService procedureService;
  @MockBean private LdmlExporterService ldmlExporterService;
  @MockBean private UserGroupService userGroupService;

  @MockBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    mockUserGroups(userGroupService);
  }

  @Test
  void testHandover() {
    String identifier = "docnr12345678";

    DocumentationUnitDTO docUnitDueNow =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            docUnitRepository,
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber(identifier)
                .scheduledByEmail("test@example.local")
                .scheduledPublicationDateTime(LocalDateTime.now())
                .decisionDate(LocalDate.now()));

    await()
        .atMost(Duration.ofSeconds(62))
        .untilAsserted(
            () -> {
              var docUnit = docUnitRepository.findById(docUnitDueNow.getId()).get();
              assertThat(docUnit.getScheduledByEmail()).isNull();
              assertThat(docUnit.getScheduledPublicationDateTime()).isNull();
              assertThat(docUnit.getLastPublicationDateTime())
                  .isBetween(
                      LocalDateTime.now().minusSeconds(20), LocalDateTime.now().plusSeconds(20));
            });

    assertThat(docUnitRepository.findAll()).hasSize(1);
  }
}
