package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.JURIS_PUBLISHED;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitControllerAuthIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("db/create_extension.sql");

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

  static Stream<Arguments> getUnauthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "BGH", null),
        Arguments.of("CC-RIS", "BGH", List.of(UNPUBLISHED)),
        Arguments.of("BGH", "CC-RIS", null),
        Arguments.of("BGH", "CC-RIS", List.of(UNPUBLISHED)));
  }

  static Stream<Arguments> getAuthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "BGH", List.of(PUBLISHED)),
        Arguments.of("CC-RIS", "BGH", List.of(PUBLISHING)),
        Arguments.of("CC-RIS", "BGH", List.of(JURIS_PUBLISHED)),
        Arguments.of("BGH", "BGH", List.of(UNPUBLISHED)),
        Arguments.of("BGH", "BGH", List.of(PUBLISHED)),
        Arguments.of("BGH", "BGH", List.of(UNPUBLISHED, PUBLISHED)),
        Arguments.of("BGH", "BGH", List.of(PUBLISHING)),
        Arguments.of("BGH", "BGH", List.of(UNPUBLISHED, PUBLISHING)),
        Arguments.of("BGH", "BGH", List.of(UNPUBLISHED, PUBLISHED, UNPUBLISHED)));
  }

  private static final Map<String, String> officeGroupMap =
      new HashMap<>() {
        {
          put("CC-RIS", "/CC-RIS");
          put("BGH", "/caselaw/BGH");
        }
      };

  private static final Map<String, UUID> officeIdMap = new HashMap<>();

  @BeforeEach
  void setUp() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    UUID docOffice1Id = documentationOfficeRepository.findByLabel("CC-RIS").getId();
    UUID docOffice2Id = documentationOfficeRepository.findByLabel("BGH").getId();

    officeIdMap.put("CC-RIS", docOffice1Id);
    officeIdMap.put("BGH", docOffice2Id);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
    statusRepository.deleteAll().block();
    databasePublishReportRepository.deleteAll().block();
  }

  @ParameterizedTest
  @MethodSource("getAuthorizedCases")
  void testGetAll_shouldBeAccessible(
      String docUnitOffice, String userDocOffice, List<PublicationStatus> publicationStatus) {

    String userOfficeId = officeGroupMap.get(userDocOffice);
    UUID docUnitOfficeId = officeIdMap.get(docUnitOffice);

    DocumentUnitDTO docUnit = createNewDocumentUnitDTO(UUID.randomUUID(), docUnitOfficeId);
    for (int i = 0; i < publicationStatus.size(); i++) {
      saveToStatusRepository(
          docUnit,
          docUnit.getCreationtimestamp().plusSeconds(60 + i),
          DocumentUnitStatus.builder().publicationStatus(publicationStatus.get(i)).build());
    }

    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(userOfficeId)
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractStatusByUuid(result.getResponseBody(), docUnit.getUuid()))
        .isEqualTo(getResultStatus(publicationStatus).toString());

    EntityExchangeResult<String> resultForSingleAccess =
        risWebTestClient
            .withLogin(userOfficeId)
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
  }

  @ParameterizedTest
  @MethodSource("getUnauthorizedCases")
  void testGetAll_shouldNotBeAccessible(
      String docUnitOffice, String userDocOffice, List<PublicationStatus> publicationStatus) {

    String userOfficeId = officeGroupMap.get(userDocOffice);
    UUID docUnitOfficeId = officeIdMap.get(docUnitOffice);

    DocumentUnitDTO docUnit = createNewDocumentUnitDTO(UUID.randomUUID(), docUnitOfficeId);
    if (publicationStatus != null) {
      for (int i = 0; i < publicationStatus.size(); i++) {
        saveToStatusRepository(
            docUnit,
            docUnit.getCreationtimestamp().plusSeconds(60 + i),
            DocumentUnitStatus.builder().publicationStatus(publicationStatus.get(i)).build());
      }
    }

    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(userOfficeId)
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractDocUnitsByUuid(result.getResponseBody(), docUnit.getUuid())).isEmpty();

    risWebTestClient
        .withLogin(userOfficeId)
        .get()
        .uri("/api/v1/caselaw/documentunits/" + docUnit.getDocumentnumber())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUnpublishedDocumentUnitIsForbiddenFOrOtherOffice() {
    DocumentUnitDTO docUnit1 =
        createNewDocumentUnitDTO(UUID.randomUUID(), officeIdMap.get("CC-RIS"));
    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp(),
        DocumentUnitStatus.builder().publicationStatus(UNPUBLISHED).build());

    // Documentation Office 1
    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(officeGroupMap.get("CC-RIS"))
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
        .withLogin(officeGroupMap.get("BGH"))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
        .exchange()
        .expectStatus()
        .isForbidden();

    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp().plus(1, ChronoUnit.DAYS),
        DocumentUnitStatus.builder().publicationStatus(PUBLISHING).build());

    result =
        risWebTestClient
            .withLogin(officeGroupMap.get("BGH"))
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
        DocumentUnitStatus.builder().publicationStatus(PUBLISHED).build());

    result =
        risWebTestClient
            .withLogin(officeGroupMap.get("BGH"))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + docUnit1.getDocumentnumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody())).hasToString(docUnit1.getUuid().toString());
  }

  private PublicationStatus getResultStatus(List<PublicationStatus> publicationStatus) {
    if (publicationStatus.isEmpty()) {
      return null;
    }

    PublicationStatus lastStatus = publicationStatus.get(publicationStatus.size() - 1);
    if (lastStatus == PublicationStatus.JURIS_PUBLISHED) {
      return PUBLISHED;
    }

    return lastStatus;
  }

  private DocumentUnitDTO createNewDocumentUnitDTO(
      UUID documentationUnitUuid, UUID documentationOfficeId) {
    String documentNumber =
        new Random().ints(13, 0, 10).mapToObj(Integer::toString).collect(Collectors.joining());
    return repository
        .save(
            DocumentUnitDTO.builder()
                .uuid(documentationUnitUuid)
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
                .publicationStatus(status.publicationStatus())
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
            responseBody,
            String.format("$.content[?(@.uuid=='%s')].status.publicationStatus", uuid));
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
