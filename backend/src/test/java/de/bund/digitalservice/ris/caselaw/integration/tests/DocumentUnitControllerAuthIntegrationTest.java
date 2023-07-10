package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
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
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
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
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
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

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitStatusRepository statusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @Autowired private DatabasePublicationReportRepository databasePublishReportRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

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
    databasePublishReportRepository.deleteAll().block();
  }

  @Test
  void testGetAll_correctFilteringDependingOnUser() {
    DocumentUnitDTO office1Unpublished = createNewDocumentUnitDTO(docOffice1DTO.getId());
    saveToStatusRepository(
        office1Unpublished,
        office1Unpublished.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());

    DocumentUnitDTO office2Published = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(
        office2Published,
        office2Published.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());
    saveToStatusRepository(
        office2Published, Instant.now(), DocumentUnitStatus.builder().status(PUBLISHED).build());

    DocumentUnitDTO office2Publishing = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(
        office2Publishing,
        office2Publishing.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());
    saveToStatusRepository(
        office2Publishing, Instant.now(), DocumentUnitStatus.builder().status(PUBLISHING).build());

    DocumentUnitDTO office2Unpublished = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(
        office2Unpublished,
        office2Unpublished.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());

    DocumentUnitDTO office2LaterPublished = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(
        office2LaterPublished,
        office2LaterPublished.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());
    saveToStatusRepository(
        office2LaterPublished,
        Instant.now(),
        DocumentUnitStatus.builder().status(PUBLISHED).build());

    DocumentUnitDTO office2NoStatus = createNewDocumentUnitDTO(docOffice2DTO.getId());

    DocumentUnitDTO withoutOffice = createNewDocumentUnitDTO(null);

    DocumentUnitDTO office2LaterUnpublished = createNewDocumentUnitDTO(docOffice2DTO.getId());
    saveToStatusRepository(
        office2LaterUnpublished,
        office2LaterUnpublished.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());
    saveToStatusRepository(
        office2LaterUnpublished,
        Instant.now().plus(1, ChronoUnit.DAYS),
        DocumentUnitStatus.builder().status(PUBLISHED).build());
    saveToStatusRepository(
        office2LaterUnpublished,
        Instant.now().plus(2, ChronoUnit.DAYS),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());

    // Documentation Office 1
    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(docOffice1Group)
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractStatusByUuid(result.getResponseBody(), office1Unpublished.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2Published.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2Publishing.getUuid()))
        .isEqualTo(PUBLISHING.toString());
    assertThat(extractDocUnitsByUuid(result.getResponseBody(), office2Unpublished.getUuid()))
        .isEmpty();
    assertThat(extractStatusByUuid(result.getResponseBody(), office2LaterPublished.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2NoStatus.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), withoutOffice.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractDocUnitsByUuid(result.getResponseBody(), office2LaterUnpublished.getUuid()))
        .isEmpty();

    // Documentation Office 2
    result =
        risWebTestClient
            .withLogin(docOffice2Group)
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractDocUnitsByUuid(result.getResponseBody(), office1Unpublished.getUuid()))
        .isEmpty();
    assertThat(extractStatusByUuid(result.getResponseBody(), office2Published.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2Publishing.getUuid()))
        .isEqualTo(PUBLISHING.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2Unpublished.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2LaterPublished.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2NoStatus.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), withoutOffice.getUuid()))
        .isEqualTo(PUBLISHED.toString());
    assertThat(extractStatusByUuid(result.getResponseBody(), office2LaterUnpublished.getUuid()))
        .isEqualTo(UNPUBLISHED.toString());
  }

  @Test
  void testUnpublishedDocumentUnitIsForbiddenFOrOtherOffice() {
    DocumentUnitDTO docUnit1 = createNewDocumentUnitDTO(docOffice1DTO.getId());
    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp(),
        DocumentUnitStatus.builder().status(UNPUBLISHED).build());

    // Documentation Office 1
    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(docOffice1Group)
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody())).isEqualTo(docUnit1.getUuid().toString());

    // Documentation Office 2
    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
        .exchange()
        .expectStatus()
        .isForbidden();

    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp().plus(1, ChronoUnit.DAYS),
        DocumentUnitStatus.builder().status(PUBLISHING).build());

    result =
        risWebTestClient
            .withLogin(docOffice2Group)
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody())).hasToString(docUnit1.getUuid().toString());

    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp().plus(2, ChronoUnit.DAYS),
        DocumentUnitStatus.builder().status(PUBLISHED).build());

    result =
        risWebTestClient
            .withLogin(docOffice2Group)
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
                .status(status.status())
                .withError(status.withError())
                .createdAt(createdAt)
                .id(UUID.randomUUID())
                .newEntry(true)
                .build())
        .block();
  }

  private String extractStatusByUuid(String responseBody, UUID uuid) {
    List<String> docUnitStatusResults =
        JsonPath.read(
            responseBody, String.format("$.content[?(@.uuid=='%s')].status.status", uuid));
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
