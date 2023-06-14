package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.Utils.getMockLoginWithDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
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
      AuthService.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberService.class,
      DatabaseDocumentUnitStatusService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      SecurityConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitControllerAuthIntegrationTest {
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
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitStatusRepository statusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  private final String docOffice1Group = "/CC-RIS";
  private final String docOffice2Group = "/caselaw/BGH";
  private DocumentationOfficeDTO docOffice1DTO;
  private DocumentationOfficeDTO docOffice2DTO;

  @BeforeEach
  void setUp() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    docOffice1DTO = documentationOfficeRepository.findByLabel("CC-RIS").block();
    docOffice2DTO = documentationOfficeRepository.findByLabel("BGH").block();
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
    statusRepository.deleteAll().block();
  }

  @Test
  void testGetAll_correctFilteringDependingOnUser() {
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

  @Test
  void testGetByDocumentNumber() {
    DocumentUnitDTO docUnit1 = createNewDocumentUnitDTO(docOffice1DTO.getId());
    saveToStatusRepository(docUnit1, docUnit1.getCreationtimestamp(), UNPUBLISHED);

    // Documentation Office 1
    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody())).isEqualTo(docUnit1.getUuid().toString());

    // Documentation Office 2
    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
        .exchange()
        .expectStatus()
        .isForbidden();

    saveToStatusRepository(
        docUnit1, docUnit1.getCreationtimestamp().plus(1, ChronoUnit.DAYS), PUBLISHED);

    result =
        webClient
            .mutateWith(csrf())
            .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody())).hasToString(docUnit1.getUuid().toString());
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

  private String extractUuid(String responseBody) {
    return JsonPath.read(responseBody, "$.uuid");
  }
}
