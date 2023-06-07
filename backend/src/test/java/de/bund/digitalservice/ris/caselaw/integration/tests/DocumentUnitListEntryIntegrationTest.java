package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.Utils.getMockLogin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      KeycloakUserService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class
    },
    controllers = {DocumentUnitController.class})
public class DocumentUnitListEntryIntegrationTest {
  @Container
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;

  @Autowired private WebTestClient webClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitMetadataRepository listEntryRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByLabel("BGH").block();

    DocumentUnitDTO neurisDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .dataSource(DataSource.NEURIS)
                    .documentationOfficeId(documentationOfficeDTO.getId())
                    .build())
            .block();
    DocumentUnitDTO migrationDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("MIGRATION")
                    .dataSource(DataSource.MIGRATION)
                    .build())
            .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(neurisDto.getId())
                .fileNumber("AkteX")
                .isDeviating(false)
                .build())
        .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(migrationDto.getId())
                .fileNumber("AkteM")
                .isDeviating(false)
                .build())
        .block();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLogin())
        .get()
        .uri("/api/v1/caselaw/documentunits?pg=0&sz=3")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content[0].documentNumber")
        .isEqualTo("1234567890123")
        .jsonPath("$.content[0].uuid")
        .isEqualTo(neurisDto.getUuid().toString())
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteX")
        .jsonPath("$.content[0].documentationOffice.label")
        .isEqualTo("BGH")
        .jsonPath("$.content[0].status")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.totalElements")
        .isEqualTo(1);
  }

  @Test
  void testForCorrectOrdering() {
    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByLabel("DigitalService").block();

    List<Instant> timestampsExpected = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      timestampsExpected.add(Instant.now().minus(i, ChronoUnit.DAYS));
    }
    Collections.shuffle(timestampsExpected);

    for (int i = 0; i < 10; i++) {
      repository
          .save(
              DocumentUnitDTO.builder()
                  .uuid(UUID.randomUUID())
                  .creationtimestamp(timestampsExpected.get(i))
                  .documentnumber("123456789012" + i)
                  .dataSource(DataSource.NEURIS)
                  .documentationOfficeId(documentationOfficeDTO.getId())
                  .build())
          .block();
    }

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .mutateWith(getMockLogin())
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> timestampActualStrings =
        JsonPath.read(result.getResponseBody(), "$.content[*].creationTimestamp");
    List<Instant> timestampsActual = timestampActualStrings.stream().map(Instant::parse).toList();
    assertThat(timestampsActual).hasSameSizeAs(timestampsExpected);

    for (int i = 0; i < timestampsActual.size() - 1; i++) {
      Instant tThis = timestampsActual.get(i);
      Instant tNext = timestampsActual.get(i + 1);
      assertThat(tThis).isAfter(tNext);
    }
  }
}
