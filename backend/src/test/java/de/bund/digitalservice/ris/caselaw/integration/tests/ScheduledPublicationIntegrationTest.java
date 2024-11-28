package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.LegalPeriodicalEditionController;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.ScheduledPublicationService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      DocumentationUnitRepository.class,
      HandoverService.class,
      DatabaseDocumentationUnitStatusService.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentationUnitController.class, LegalPeriodicalEditionController.class})
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
  @Autowired private ScheduledPublicationService scheduledPublicationService;

  @MockBean private HandoverService handoverService;
  @MockBean private HttpMailSender mailSender;

  @Test
  void testHandover(HandoverEntityType entityType) {
    String identifier = "docnr12345678";

    DocumentationUnitDTO docUnitDueNow =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            docUnitRepository,
            DocumentationUnitDTO.builder()
                .documentationOffice(null)
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
