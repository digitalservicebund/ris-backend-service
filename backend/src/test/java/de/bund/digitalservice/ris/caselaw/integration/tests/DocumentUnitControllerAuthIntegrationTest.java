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
import java.util.List;
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

  private static final String docOffice1 = "CC-RIS";
  private static final String docOffice1Group = "/" + docOffice1;

  private static final String docOffice2 = "BGH";
  private static final String docOffice2Group = "/caselaw/" + docOffice2;
  private UUID docOffice1Id;
  private UUID docOffice2Id;

  @BeforeEach
  void setUp() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    docOffice1Id = documentationOfficeRepository.findByLabel(docOffice1).getId();
    docOffice2Id = documentationOfficeRepository.findByLabel(docOffice2).getId();
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

    UUID docUnitOfficeId = null;
    if (docUnitOffice.equals(userDocOffice)) {
      docUnitOfficeId = docOffice1Id;
    } else if (docUnitOffice.equals(docOffice2)) {
      docUnitOfficeId = docOffice2Id;
    }

    String userOfficeId = null;
    if (userDocOffice.equals(userDocOffice)) {
      userOfficeId = docOffice1Group;
    } else if (userDocOffice.equals(docOffice2)) {
      userOfficeId = docOffice2Group;
    }

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
  }

  private PublicationStatus getResultStatus(List<PublicationStatus> publicationStatus) {
    if (publicationStatus.isEmpty()) {
      return null;
    }

    PublicationStatus lastStatus = publicationStatus.get(publicationStatus.size() - 1);
    if (lastStatus == PublicationStatus.TEST_DOC_UNIT
        || lastStatus == PublicationStatus.JURIS_PUBLISHED) {
      return PUBLISHED;
    }

    return lastStatus;
  }

  @ParameterizedTest
  @MethodSource("getUnauthorizedCases")
  void testGetAll_shouldNotBeAccessible(
      String docUnitOffice, String userDocOffice, List<PublicationStatus> publicationStatus) {

    UUID docUnitOfficeId = null;
    if (docUnitOffice.equals(userDocOffice)) {
      docUnitOfficeId = docOffice1Id;
    } else if (docUnitOffice.equals(docOffice2)) {
      docUnitOfficeId = docOffice2Id;
    }

    String userOfficeId = null;
    if (userDocOffice.equals(userDocOffice)) {
      userOfficeId = docOffice1Group;
    } else if (userDocOffice.equals(docOffice2)) {
      userOfficeId = docOffice2Group;
    }

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

    assertThat(extractDocUnitsByUuid(result.getResponseBody(), docUnit.getUuid())).isEmpty();
  }

  static Stream<Arguments> getUnauthorizedCases() {
    return Stream.of(
        Arguments.of("CC_RIS", "NEURIS", List.of(UNPUBLISHED)),
        Arguments.of("NEURIS", "CC_RIS", List.of(UNPUBLISHED)));
  }

  static Stream<Arguments> getAuthorizedCases() {
    return Stream.of(
        Arguments.of("CC_RIS", "NEURIS", List.of(PUBLISHED)),
        Arguments.of("CC_RIS", "NEURIS", List.of(PUBLISHING)),
        Arguments.of("NEURIS", "NEURIS", List.of(UNPUBLISHED)),
        Arguments.of("NEURIS", "NEURIS", List.of(PUBLISHED)),
        Arguments.of("NEURIS", "NEURIS", List.of(UNPUBLISHED, PUBLISHED)),
        Arguments.of("NEURIS", "NEURIS", List.of(PUBLISHING)),
        Arguments.of("NEURIS", "NEURIS", List.of(UNPUBLISHED, PUBLISHING)),
        Arguments.of("NEURIS", "NEURIS", List.of(UNPUBLISHED, PUBLISHED, UNPUBLISHED)),
        Arguments.of("NEURIS", "NEURIS", List.of(PublicationStatus.TEST_DOC_UNIT)),
        Arguments.of("NEURIS", "NEURIS", List.of(PublicationStatus.JURIS_PUBLISHED)));
  }

  @Test
  void testUnpublishedDocumentUnitIsForbiddenFOrOtherOffice() {
    DocumentUnitDTO docUnit1 = createNewDocumentUnitDTO(UUID.randomUUID(), docOffice1Id);
    saveToStatusRepository(
        docUnit1,
        docUnit1.getCreationtimestamp(),
        DocumentUnitStatus.builder().publicationStatus(UNPUBLISHED).build());

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
        DocumentUnitStatus.builder().publicationStatus(PUBLISHING).build());

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
        DocumentUnitStatus.builder().publicationStatus(PUBLISHED).build());

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
