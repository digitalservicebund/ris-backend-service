package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.LookupTableController;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresCitationStyleRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      LookupTableService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      PostgresDocumentTypeRepositoryImpl.class,
      PostgresCitationStyleRepositoryImpl.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {LookupTableController.class})
class LookupTableIntegrationTest {
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
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseCitationStyleRepository databaseCitationStyleRepository;

  @MockBean private FieldOfLawRepository fieldOfLawRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean UserService userService;
  @MockBean private DocumentUnitService documentUnitService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  @AfterEach
  void cleanUp() {
    databaseCitationStyleRepository.deleteAll().block();
    databaseDocumentTypeRepository.deleteAll();
  }

  @Test
  void testGetAllCitationStyles() {
    UUID TEST_UUID = UUID.randomUUID();
    UUID TEST_UUID2 = UUID.randomUUID();
    CitationStyleDTO citationStyleDTO =
        CitationStyleDTO.builder()
            .uuid(TEST_UUID)
            .jurisId(1L)
            .changeIndicator('N')
            .version("1.0")
            .documentType('R')
            .citationDocumentType('R')
            .jurisShortcut("Änderung")
            .label("Änderung")
            .newEntry(true)
            .build();
    databaseCitationStyleRepository.save(citationStyleDTO).block();
    citationStyleDTO =
        CitationStyleDTO.builder()
            .uuid(TEST_UUID2)
            .jurisId(2L)
            .changeIndicator('N')
            .version("1.0")
            .documentType('R')
            .citationDocumentType('R')
            .jurisShortcut("Vergleich")
            .label("Vergleich")
            .newEntry(true)
            .build();
    databaseCitationStyleRepository.save(citationStyleDTO).block();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/lookuptable/zitart")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CitationStyle[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("Änderung");
              assertThat(response.getResponseBody()[1].label()).isEqualTo("Vergleich");
            });
  }

  @Test
  void testGetCitationStylesBySearchString() {
    UUID TEST_UUID = UUID.randomUUID();
    UUID TEST_UUID2 = UUID.randomUUID();
    CitationStyleDTO citationStyleDTO =
        CitationStyleDTO.builder()
            .uuid(TEST_UUID)
            .jurisId(3L)
            .changeIndicator('N')
            .version("1.0")
            .documentType('R')
            .citationDocumentType('R')
            .jurisShortcut("Änderung")
            .label("Änderung")
            .newEntry(true)
            .build();
    databaseCitationStyleRepository.save(citationStyleDTO).block();
    citationStyleDTO =
        CitationStyleDTO.builder()
            .uuid(TEST_UUID2)
            .jurisId(4L)
            .changeIndicator('N')
            .version("1.0")
            .documentType('R')
            .citationDocumentType('R')
            .jurisShortcut("Vergleich")
            .label("Vergleich")
            .newEntry(true)
            .build();
    databaseCitationStyleRepository.save(citationStyleDTO).block();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/lookuptable/zitart?q=Änd")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Court[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("Änderung");
            });
  }
}
