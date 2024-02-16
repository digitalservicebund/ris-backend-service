package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.CourtController;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      CourtService.class,
      PostgresJPAConfig.class,
      PostgresCourtRepositoryImpl.class,
      PostgresDocumentTypeRepositoryImpl.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
    },
    controllers = {CourtController.class})
class CourtIntegrationTest {
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
  @Autowired private DatabaseCourtRepository databaseCourtRepository;

  @MockBean UserService userService;
  @MockBean private DocumentUnitService documentUnitService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  @AfterEach
  void cleanUp() {
    databaseCourtRepository.deleteAll();
  }

  @Test
  void testGetAllCourts() {
    CourtDTO courtDTO =
        CourtDTO.builder()
            .jurisId(0)
            .type("AB")
            .location("Berlin")
            .isSuperiorCourt(false)
            .isForeignCourt(false)
            .additionalInformation("- aufgehoben: 1975-02-01 -")
            .build();
    databaseCourtRepository.save(courtDTO);
    courtDTO =
        CourtDTO.builder()
            .jurisId(1)
            .type("BGH")
            .location("Karlsruhe")
            .isSuperiorCourt(true)
            .isForeignCourt(false)
            .build();
    databaseCourtRepository.save(courtDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Court[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("AB Berlin");
              assertThat(response.getResponseBody()[0].revoked())
                  .isEqualTo("aufgehoben seit: 1975");

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

    for (int i = 0; i < courtData.length; i++) {
      String[] court = courtData[i];
      databaseCourtRepository.save(
          CourtDTO.builder()
              .jurisId(i)
              .type(court[0])
              .location(court[1])
              .isSuperiorCourt(false)
              .isForeignCourt(false)
              .build());
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

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/courts?q=land")
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
