package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
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
import org.junit.jupiter.api.Disabled;
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
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentUnitStatusService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitControllerAuthIntegrationTest {
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
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseStatusRepository statusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean AttachmentService attachmentService;
  @MockBean private FeatureToggleService featureService;

  static Stream<Arguments> getUnauthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "BGH", List.of(UNPUBLISHED)),
        Arguments.of("BGH", "CC-RIS", List.of(UNPUBLISHED)));
  }

  static Stream<Arguments> getAuthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "BGH", List.of(PUBLISHED)),
        Arguments.of("CC-RIS", "BGH", List.of(PUBLISHING)),
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

  private static final Map<String, DocumentationOfficeDTO> officeMap = new HashMap<>();

  @BeforeEach
  void setUp() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    DocumentationOfficeDTO ccRisOffice = documentationOfficeRepository.findByAbbreviation("CC-RIS");
    DocumentationOfficeDTO bghOffice = documentationOfficeRepository.findByAbbreviation("BGH");

    officeMap.put("CC-RIS", ccRisOffice);
    officeMap.put("BGH", bghOffice);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    //    databasePublishReportRepository.deleteAll().block();
  }

  @ParameterizedTest
  @Disabled("waiting for Datenschemamigration to be finished")
  @MethodSource("getAuthorizedCases")
  void testGetAll_shouldBeAccessible(
      String docUnitOfficeAbbreviation,
      String userDocOfficeAbbreviation,
      List<PublicationStatus> publicationStatus) {

    String userOfficeId = officeGroupMap.get(userDocOfficeAbbreviation);
    DocumentationOfficeDTO docUnitOffice = officeMap.get(docUnitOfficeAbbreviation);

    DocumentationUnitDTO documentationUnitDTO =
        createNewDocumentUnitDTO(UUID.randomUUID(), docUnitOffice);
    for (int i = 0; i < publicationStatus.size(); i++) {
      saveToStatusRepository(
          documentationUnitDTO,
          Instant.now().plusSeconds(60 + i),
          Status.builder().publicationStatus(publicationStatus.get(i)).build());
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

    assertThat(extractStatusByUuid(result.getResponseBody(), documentationUnitDTO.getId()))
        .isEqualTo(getResultStatus(publicationStatus).toString());

    EntityExchangeResult<String> resultForSingleAccess =
        risWebTestClient
            .withLogin(userOfficeId)
            .get()
            .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
  }

  @ParameterizedTest
  @Disabled("waiting for Datenschemamigration to be finished")
  @MethodSource("getUnauthorizedCases")
  void testGetAll_shouldNotBeAccessible(
      String docUnitOfficeAbbreviation,
      String userDocOfficeAbbreviation,
      List<PublicationStatus> publicationStatus) {

    String userOfficeId = officeGroupMap.get(userDocOfficeAbbreviation);
    DocumentationOfficeDTO docUnitOfficeId = officeMap.get(docUnitOfficeAbbreviation);

    DocumentationUnitDTO documentationUnitDTO =
        createNewDocumentUnitDTO(UUID.randomUUID(), docUnitOfficeId);
    for (int i = 0; i < publicationStatus.size(); i++) {
      saveToStatusRepository(
          documentationUnitDTO,
          Instant.now().plusSeconds(60 + i),
          Status.builder().publicationStatus(publicationStatus.get(i)).build());
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

    assertThat(extractDocUnitsByUuid(result.getResponseBody(), documentationUnitDTO.getId()))
        .isEmpty();

    risWebTestClient
        .withLogin(userOfficeId)
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUnpublishedDocumentUnitIsForbiddenFOrOtherOffice() {
    DocumentationUnitDTO documentationUnitDTO =
        createNewDocumentUnitDTO(UUID.randomUUID(), officeMap.get("CC-RIS"));
    saveToStatusRepository(
        documentationUnitDTO,
        Instant.now(),
        Status.builder().publicationStatus(UNPUBLISHED).build());

    // Documentation Office 1
    EntityExchangeResult<String> result =
        risWebTestClient
            .withLogin(officeGroupMap.get("CC-RIS"))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody()))
        .isEqualTo(documentationUnitDTO.getId().toString());

    // Documentation Office 2
    risWebTestClient
        .withLogin(officeGroupMap.get("BGH"))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isForbidden();

    saveToStatusRepository(
        documentationUnitDTO,
        Instant.now().plus(1, ChronoUnit.DAYS),
        Status.builder().publicationStatus(PUBLISHING).build());

    result =
        risWebTestClient
            .withLogin(officeGroupMap.get("BGH"))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody()))
        .hasToString(documentationUnitDTO.getId().toString());

    saveToStatusRepository(
        documentationUnitDTO,
        Instant.now().plus(2, ChronoUnit.DAYS),
        Status.builder().publicationStatus(PUBLISHED).build());

    result =
        risWebTestClient
            .withLogin(officeGroupMap.get("BGH"))
            .get()
            .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(extractUuid(result.getResponseBody()))
        .hasToString(documentationUnitDTO.getId().toString());
  }

  private PublicationStatus getResultStatus(List<PublicationStatus> publicationStatus) {
    if (publicationStatus.isEmpty()) {
      return null;
    }

    return publicationStatus.get(publicationStatus.size() - 1);
  }

  private DocumentationUnitDTO createNewDocumentUnitDTO(
      UUID documentationUnitUuid, DocumentationOfficeDTO documentationOffice) {
    String documentNumber =
        new Random().ints(13, 0, 10).mapToObj(Integer::toString).collect(Collectors.joining());
    return repository.save(
        DocumentationUnitDTO.builder()
            .id(documentationUnitUuid)
            .documentNumber(documentNumber)
            .documentationOffice(documentationOffice)
            .build());
  }

  private void saveToStatusRepository(
      DocumentationUnitDTO documentationUnitDTO, Instant createdAt, Status status) {
    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(documentationUnitDTO)
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .createdAt(createdAt)
            .build());
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
