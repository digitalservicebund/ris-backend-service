package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.Utils.getMockLogin;
import static de.bund.digitalservice.ris.caselaw.Utils.getMockLoginWithDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
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
  @Autowired private DatabaseDocumentUnitStatusRepository statusRepository;
  @Autowired private DatabaseDocumentUnitMetadataRepository listEntryRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
    statusRepository.deleteAll().block();
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
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
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByLabel("DigitalService").block();

    List<Instant> timestampsExpected = new ArrayList<>();
    for (int i = 0; i < 11; i++) {
      timestampsExpected.add(Instant.now().minus(i, ChronoUnit.DAYS));
    }
    Collections.shuffle(timestampsExpected);

    for (int i = 0; i < 11; i++) {
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
    assertThat(timestampsActual).hasSize(10); // test pagination

    for (int i = 0; i < timestampsActual.size() - 1; i++) {
      Instant tThis = timestampsActual.get(i);
      Instant tNext = timestampsActual.get(i + 1);
      assertThat(tThis).isAfter(tNext);
    }
  }

  @Test
  void testForCorrectFilteringDependingOnUser() {
    String docOffice1Group = "/CC-RIS";
    String docOffice2Group = "/caselaw/BGH";

    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    DocumentationOfficeDTO docOffice1DTO =
        documentationOfficeRepository.findByLabel("CC-RIS").block();
    DocumentationOfficeDTO docOffice2DTO = documentationOfficeRepository.findByLabel("BGH").block();

    DocumentUnitDTO docUnit1 = createNewDocumentUnitDTO(docOffice1DTO.getId());
    saveToStatusRepository(docUnit1, docUnit1.getCreationtimestamp(), UNPUBLISHED);

    DocumentUnitDTO docUnit2 = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(docUnit2, docUnit2.getCreationtimestamp(), UNPUBLISHED);
    saveToStatusRepository(docUnit2, Instant.now(), PUBLISHED);

    DocumentUnitDTO docUnit3 = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(docUnit3, docUnit3.getCreationtimestamp(), UNPUBLISHED);

    DocumentUnitDTO docUnit4 = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(docUnit4, docUnit4.getCreationtimestamp(), UNPUBLISHED);
    saveToStatusRepository(docUnit4, Instant.now(), PUBLISHED);

    DocumentUnitDTO docUnit5 = createNewDocumentUnitDTO(docOffice2DTO.getId());

    DocumentUnitDTO docUnit6 = createNewDocumentUnitDTO(null);

    DocumentUnitDTO docUnit7 = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(docUnit7, docUnit7.getCreationtimestamp(), UNPUBLISHED);
    saveToStatusRepository(docUnit7, Instant.now().plus(1, ChronoUnit.DAYS), PUBLISHED);
    saveToStatusRepository(docUnit7, Instant.now().plus(2, ChronoUnit.DAYS), UNPUBLISHED);

    // Documentation Office 1
    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit1.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit2.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractDocUnitsByUuid(result.getResponseBody(), docUnit3.getUuid())).isEmpty();
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit4.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit5.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit6.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractDocUnitsByUuid(result.getResponseBody(), docUnit7.getUuid())).isEmpty();

    // Documentation Office 2
    result =
        webClient
            .mutateWith(csrf())
            .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractDocUnitsByUuid(result.getResponseBody(), docUnit1.getUuid())).isEmpty();
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit2.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit3.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit4.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit5.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit6.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit7.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
  }

  private DocumentUnitDTO createNewDocumentUnitDTO(UUID documentationOfficeId) {
    String documentNumber =
        new Random().ints(13, 0, 10).mapToObj(Integer::toString).collect(Collectors.joining());
    return repository
        .save(
            DocumentUnitDTO.builder()
                .uuid(UUID.randomUUID())
                .creationtimestamp(Instant.now())
                .documentnumber(documentNumber)
                .dataSource(DataSource.NEURIS)
                .documentationOfficeId(documentationOfficeId)
                .build())
        .block();
  }

  private void saveToStatusRepository(
      DocumentUnitDTO docUnitDTO, Instant createdAt, DocumentUnitStatus status) {
    statusRepository
        .save(
            DocumentUnitStatusDTO.builder()
                .documentUnitId(docUnitDTO.getUuid())
                .status(status)
                .createdAt(createdAt)
                .id(UUID.randomUUID())
                .newEntry(true)
                .build())
        .block();
  }

  private String extractStatusByUuid(String responseBody, UUID uuid) {
    List<String> docUnitStatusResults =
        JsonPath.read(responseBody, String.format("$.content[?(@.uuid=='%s')].status", uuid));
    assertThat(docUnitStatusResults).hasSize(1);
    return docUnitStatusResults.get(0);
  }

  private List<String> extractDocUnitsByUuid(String responseBody, UUID uuid) {
    return JsonPath.read(responseBody, String.format("$.content[?(@.uuid=='%s')]", uuid));
  }
}
