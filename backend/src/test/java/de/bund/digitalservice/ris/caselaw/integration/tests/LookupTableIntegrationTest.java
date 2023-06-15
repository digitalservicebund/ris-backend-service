package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.LookupTableController;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
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
      PostgresCourtRepositoryImpl.class
    },
    controllers = {LookupTableController.class})
class LookupTableIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private WebTestClient webClient;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseCitationStyleRepository databaseCitationStyleRepository;
  @MockBean private CitationStyleRepository citationStyleRepository;
  @MockBean private FieldOfLawRepository fieldOfLawRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    databaseCourtRepository.deleteAll().block();
    databaseDocumentTypeRepository.deleteAll().block();
  }

  @Test
  void testGetAllCourts() {
    CourtDTO courtDTO = CourtDTO.builder().courttype("AB").courtlocation("Berlin").build();
    databaseCourtRepository.save(courtDTO).block();
    courtDTO =
        CourtDTO.builder()
            .courttype("BGH")
            .courtlocation("Karlsruhe")
            .superiorcourt("ja")
            .foreigncountry("nein")
            .build();
    databaseCourtRepository.save(courtDTO).block();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/lookuptable/courts")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Court[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("AB Berlin");
              assertThat(response.getResponseBody()[1].label()).isEqualTo("BGH");
            });
  }

  @Test
  void testGetCourtsBySearchString() {
    String[][] courtData = {
      {"Kammer für Baulandsachen", "Ulm"},
      {"Gericht", "Potsdam"}, // not expected to be in results
      {"Landsitzungskammer", "Hamburg"},
      {"Verwaltungsgericht der Landeskirche", "Frankfurt"},
      {"England", "Court"},
      {"Landgericht", "Amberg"},
      {"Jugendgericht des Haupt-Landes", "München"},
    };

    for (String[] court : courtData) {
      databaseCourtRepository
          .save(CourtDTO.builder().courttype(court[0]).courtlocation(court[1]).build())
          .block();
    }

    // expected order: alphabetically within 3 priority classes:
    List<String> expectedOrder =
        Arrays.asList(
            "Landgericht Amberg", // [1]
            "Landsitzungskammer Hamburg", // [1]
            "Jugendgericht des Haupt-Landes München", // [2]
            "Verwaltungsgericht der Landeskirche Frankfurt", // [2]
            "England Court", // [3]
            "Kammer für Baulandsachen Ulm" // [3]
            );

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/lookuptable/courts?q=land")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Court[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(6);
              for (int i = 0; i < expectedOrder.size(); i++) {
                assertThat(response.getResponseBody()[i].label()).isEqualTo(expectedOrder.get(i));
              }
            });
  }
}
