package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.DuplicateRelationService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.ProcedureController;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserGroupDTO;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.FmxImportService;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentationUnitStatusService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseProcedureService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      DatabaseDuplicateCheckService.class,
      DuplicateRelationService.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      KeycloakUserService.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class
    },
    controllers = {DocumentationUnitController.class, ProcedureController.class})
@Sql(scripts = {"classpath:courts_init.sql"})
@Sql(
    scripts = {"classpath:courts_cleanup.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DocumentationUnitSearchIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

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
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcedureRepository procedureRepository;
  @Autowired private DatabaseUserGroupRepository userGroupRepository;
  @Autowired private DatabaseDuplicateCheckService duplicateCheckService;

  @MockitoBean S3AsyncClient s3AsyncClient;
  @MockitoBean MailService mailService;
  @MockitoBean DocxConverterService docxConverterService;
  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean AttachmentService attachmentService;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private StagingPortalPublicationService stagingPortalPublicationService;
  @MockitoBean private FmxImportService fmxImportService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private static final CourtDTO courtAgAachen =
      CourtDTO.builder().id(UUID.fromString("46301f85-9bd2-4690-a67f-f9fdfe725de3")).build();
  private static final CourtDTO courtOlgDresden =
      CourtDTO.builder().id(UUID.fromString("12e9f671-6a5c-4ec7-9b57-3fafdefd7a49")).build();
  private DocumentationOfficeDTO docOfficeDTO;
  private DocumentationOfficeDTO bghOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");
    bghOfficeDTO = documentationOfficeRepository.findByAbbreviation("BGH");
    when(featureToggleService.isEnabled("neuris.search-criteria-api")).thenReturn(true);
    when(featureToggleService.isEnabled("neuris.search-fetch-relationships")).thenReturn(true);
    mockUserGroups(userGroupService);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("MIGR202200012")
            .documentationOffice(docOfficeDTO)
            .fileNumbers(List.of(FileNumberDTO.builder().value("AkteM").rank(0L).build())));

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("NEUR202300008")
            .documentationOffice(docOfficeDTO)
            .fileNumbers(List.of(FileNumberDTO.builder().value("AkteY").rank(0L).build())));

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBody)
        .extracting("documentNumber", "fileNumber", "status")
        .containsExactly(
            tuple(
                "NEUR202300008",
                "AkteY",
                Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build()),
            tuple(
                "MIGR202200012",
                "AkteM",
                Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build()));
    assertThat(responseBody.getNumberOfElements()).isEqualTo(2);
  }

  @Test
  void shouldFilterByInboxStatus() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("ABCD202200001")
            .documentationOffice(docOfficeDTO)
            .inboxStatus(null));

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("ABCD202200002")
            .documentationOffice(docOfficeDTO)
            .inboxStatus(InboxStatus.EU));

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("ABCD202200003")
            .documentationOffice(docOfficeDTO)
            .inboxStatus(InboxStatus.EXTERNAL_HANDOVER));

    Slice<DocumentationUnitListItem> responseBodyWithoutFilter =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=5")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBodyWithoutFilter.getNumberOfElements()).isEqualTo(3);
    assertThat(responseBodyWithoutFilter)
        .map(DocumentationUnitListItem::documentNumber)
        .containsExactly("ABCD202200003", "ABCD202200002", "ABCD202200001");

    Slice<DocumentationUnitListItem> responseBodyWithEUFilter =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=5&inboxStatus=EU")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBodyWithEUFilter.getNumberOfElements()).isEqualTo(1);
    assertThat(responseBodyWithEUFilter)
        .map(DocumentationUnitListItem::documentNumber)
        .containsExactly("ABCD202200002");

    Slice<DocumentationUnitListItem> responseBodyWithExternalHandoverFilter =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=5&inboxStatus=EXTERNAL_HANDOVER")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBodyWithExternalHandoverFilter.getNumberOfElements()).isEqualTo(1);
    assertThat(responseBodyWithExternalHandoverFilter)
        .map(DocumentationUnitListItem::documentNumber)
        .containsExactly("ABCD202200003");
  }

  @Test
  void testOrderedByDateDescending() {
    List<LocalDate> dates =
        Arrays.asList(
            LocalDate.of(2022, 1, 23),
            LocalDate.of(2022, 1, 23),
            null,
            LocalDate.of(2023, 3, 15),
            LocalDate.of(2023, 6, 7));

    for (LocalDate date : dates) {
      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              .documentNumber(RandomStringUtils.randomAlphabetic(13).toUpperCase())
              .date(date)
              .documentationOffice(docOfficeDTO));
    }

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("decisionDate")
        .containsExactly(
            LocalDate.of(2023, 6, 7),
            LocalDate.of(2023, 3, 15),
            LocalDate.of(2022, 1, 23),
            LocalDate.of(2022, 1, 23),
            null);
    var docUnitWithSameDates =
        responseBody.getContent().stream()
            .filter(docUnit -> LocalDate.of(2022, 1, 23).equals(docUnit.decisionDate()))
            .toList();
    // For the same date, the document numbers should be in descending order
    assertThat(docUnitWithSameDates.getFirst().documentNumber())
        .isGreaterThan(docUnitWithSameDates.getLast().documentNumber());
  }

  @Test
  void test_searchByScheduledOnly_shouldReturnDescendingOrder() {
    List<LocalDateTime> scheduledPublicationDates =
        Arrays.asList(
            LocalDateTime.of(2022, 1, 23, 10, 5),
            LocalDateTime.of(2024, 1, 24, 10, 5),
            LocalDateTime.of(2022, 7, 24, 10, 5),
            LocalDateTime.of(2022, 1, 24, 10, 5),
            null,
            LocalDateTime.of(2022, 1, 23, 8, 5),
            LocalDateTime.of(2022, 1, 23, 10, 4));

    for (LocalDateTime date : scheduledPublicationDates) {
      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              .documentNumber(RandomStringUtils.randomAlphabetic(13))
              .scheduledPublicationDateTime(date)
              .documentationOffice(docOfficeDTO));
    }

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=0&sz=10&myDocOfficeOnly=true&scheduledOnly=true")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("scheduledPublicationDateTime")
        .containsExactly(
            LocalDateTime.of(2024, 1, 24, 10, 5),
            LocalDateTime.of(2022, 7, 24, 10, 5),
            LocalDateTime.of(2022, 1, 24, 10, 5),
            LocalDateTime.of(2022, 1, 23, 10, 5),
            LocalDateTime.of(2022, 1, 23, 10, 4),
            LocalDateTime.of(2022, 1, 23, 8, 5));
  }

  @Test
  void
      test_searchByScheduledOnlyWithPublicationDate_shouldReturnFilteredResultsInDescendingOrder() {
    List<LocalDateTime> scheduledPublicationDates =
        Arrays.asList(
            LocalDateTime.of(2022, 1, 23, 10, 5),
            LocalDateTime.of(2024, 1, 24, 10, 5),
            LocalDateTime.of(2022, 7, 24, 10, 5),
            LocalDateTime.of(2022, 1, 24, 10, 5),
            null,
            LocalDateTime.of(2022, 1, 23, 8, 5),
            LocalDateTime.of(2022, 1, 23, 10, 4));

    for (LocalDateTime date : scheduledPublicationDates) {
      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              .documentNumber(RandomStringUtils.randomAlphabetic(13))
              .scheduledPublicationDateTime(date)
              .documentationOffice(docOfficeDTO));
    }

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=0&sz=10&myDocOfficeOnly=true&publicationDate=2022-01-23&scheduledOnly=true")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("scheduledPublicationDateTime")
        .containsExactly(
            LocalDateTime.of(2022, 1, 23, 10, 5),
            LocalDateTime.of(2022, 1, 23, 10, 4),
            LocalDateTime.of(2022, 1, 23, 8, 5));
  }

  @Test
  void test_searchByPublicationDate_shouldFilterAndReturnDescendingOrder() {
    List<LocalDateTime> lastPublicationDates =
        Arrays.asList(
            LocalDateTime.of(2022, 1, 23, 10, 5),
            null,
            LocalDateTime.of(2022, 1, 23, 9, 5),
            LocalDateTime.of(2022, 1, 23, 19, 5),
            LocalDateTime.of(2022, 1, 23, 8, 5),
            LocalDateTime.of(2022, 1, 24, 10, 5),
            null);

    List<LocalDateTime> scheduledPublicationDates =
        Arrays.asList(
            null,
            LocalDateTime.of(2022, 1, 23, 5, 3),
            null,
            null,
            LocalDateTime.of(2022, 1, 23, 10, 10),
            null,
            LocalDateTime.of(2022, 1, 23, 12, 10));

    var index = 0;

    for (LocalDateTime date : lastPublicationDates) {
      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              .documentNumber(RandomStringUtils.randomAlphabetic(13))
              .lastPublicationDateTime(date)
              .scheduledPublicationDateTime(scheduledPublicationDates.get(index))
              .documentationOffice(docOfficeDTO));
      index++;
    }

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=0&sz=10&myDocOfficeOnly=true&publicationDate=2022-01-23")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    /*
     * These are unlikely test conditions, because scheduled dates are in the future only and lastPublication dates in the past only.
     *
     * Ordered results visible for user (lastPublicationDate only if scheduledPublicationDate is null):
     * 2022-01-23 12:10
     * 2022-01-23 10:10
     * 2022-01-23 05:03
     * 2022-01-23 19:05
     * 2022-01-23 10:05
     * 2022-01-23 09:05
     */
    assertThat(responseBody)
        .extracting("scheduledPublicationDateTime")
        .containsExactly(
            LocalDateTime.of(2022, 1, 23, 12, 10),
            LocalDateTime.of(2022, 1, 23, 10, 10),
            LocalDateTime.of(2022, 1, 23, 5, 3),
            null,
            null,
            null);
    assertThat(responseBody)
        .extracting("lastPublicationDateTime")
        .containsExactly(
            null,
            LocalDateTime.of(2022, 1, 23, 8, 5),
            null,
            LocalDateTime.of(2022, 1, 23, 19, 5),
            LocalDateTime.of(2022, 1, 23, 10, 5),
            LocalDateTime.of(2022, 1, 23, 9, 5));
  }

  @Test
  void testForCorrectPagination() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "1234567801");

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "1234567802");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<?>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().isFirst()).isTrue();
              assertThat(response.getResponseBody().isLast()).isFalse();
              assertThat(response.getResponseBody().getNumberOfElements()).isEqualTo(1);
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=1&sz=1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<?>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().isFirst()).isFalse();
              assertThat(response.getResponseBody().isLast()).isTrue();
              assertThat(response.getResponseBody().getNumberOfElements()).isEqualTo(1);
            });
  }

  @Test
  void testForCompleteResultListWhenSearchingForFileNumber() {
    for (int i = 0; i < 10; i++) {
      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              // index 0-4 get a "AB" docNumber
              .documentNumber((i <= 4 ? "AB" : "GE") + "123456780" + i)
              .documentationOffice(docOfficeDTO)
              .fileNumbers(
                  // even indices get a fileNumber
                  i % 2 == 0
                      ? List.of(FileNumberDTO.builder().value("AB 34/" + i).rank(0L).build())
                      : List.of())
              // odd indices a deviating fileNumber
              .deviatingFileNumbers(
                  i % 2 == 1
                      ? List.of(
                          DeviatingFileNumberDTO.builder().value("AbC 34/" + i).rank(0L).build(),
                          DeviatingFileNumberDTO.builder().value("NoMatch" + i).rank(1L).build())
                      : List.of()));
    }

    // Doc unit with different file numbers will not match the search criteria
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("NE1234567800")
            .documentationOffice(docOfficeDTO)
            .fileNumbers(List.of(FileNumberDTO.builder().value("BB 34/").rank(0L).build()))
            .deviatingFileNumbers(
                List.of(DeviatingFileNumberDTO.builder().value("BBC 34/").rank(0L).build())));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=4&fileNumber=AB")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              Slice<DocumentationUnitListItem> responseBodyFirstPage = response.getResponseBody();
              assertThat(responseBodyFirstPage.getNumberOfElements()).isEqualTo(4);
              assertThat(responseBodyFirstPage.isFirst()).isTrue();
              assertThat(responseBodyFirstPage.isLast()).isFalse();
              assertThat(responseBodyFirstPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("GE1234567809", "GE1234567808", "GE1234567807", "GE1234567806");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=1&sz=4&fileNumber=  AB   ")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              // expect second page...
              Slice<DocumentationUnitListItem> responseBodySecondPage = response.getResponseBody();
              assertThat(responseBodySecondPage.getNumberOfElements()).isEqualTo(4);
              assertThat(responseBodySecondPage.isFirst()).isFalse();
              assertThat(responseBodySecondPage.isLast()).isFalse();
              assertThat(responseBodySecondPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("GE1234567805", "AB1234567804", "AB1234567803", "AB1234567802");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=2&sz=4&fileNumber=AB")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              // expect third page...
              Slice<DocumentationUnitListItem> responseBodyThirdPage = response.getResponseBody();
              assertThat(responseBodyThirdPage.getNumberOfElements()).isEqualTo(2);
              assertThat(responseBodyThirdPage.isFirst()).isFalse();
              assertThat(responseBodyThirdPage.isLast()).isTrue();
              assertThat(responseBodyThirdPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567801", "AB1234567800");
            });
  }

  @Test
  void testTrim() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567802");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumber=   AB  ")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567802"));
  }

  @Test
  void testSearch_withInternalUser_shouldReturnEditableAndDeletableDocumentationUnit() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567802");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumber=AB1234567802")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567802");
              assertThat(response.getResponseBody().getContent()).hasSize(1);
              assertThat(response.getResponseBody().getContent().getFirst().isEditable()).isTrue();
              assertThat(response.getResponseBody().getContent().getFirst().isDeletable()).isTrue();
            });
  }

  @Test
  void shouldFilterOutUnpublishedDocUnitsOfOtherDocOffices() {
    // Published doc unit of BGH office should be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, bghOfficeDTO, "AB1234567800");
    // Published doc unit of own office should be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567801");
    // Published doc unit of own office should be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567802").documentationOffice(docOfficeDTO),
        PublicationStatus.UNPUBLISHED);
    // Publishing doc unit of BGH office should be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567803").documentationOffice(bghOfficeDTO),
        PublicationStatus.PUBLISHING);
    // Unpublished doc unit of BGH office should not be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("XX1234567801").documentationOffice(bghOfficeDTO),
        PublicationStatus.UNPUBLISHED);

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly(
                        "AB1234567803", "AB1234567802", "AB1234567801", "AB1234567800"));
  }

  @Test
  void shouldFilterByPublicationStatus() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567800");
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567801").documentationOffice(docOfficeDTO),
        PublicationStatus.UNPUBLISHED);
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567802").documentationOffice(docOfficeDTO),
        PublicationStatus.PUBLISHING);
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567803").documentationOffice(bghOfficeDTO),
        PublicationStatus.UNPUBLISHED);

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&publicationStatus=UNPUBLISHED")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567801"));
  }

  @Test
  void shouldFilterByErrorStatus() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567800");
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567801").documentationOffice(docOfficeDTO),
        PublicationStatus.UNPUBLISHED,
        true);
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567802").documentationOffice(docOfficeDTO),
        PublicationStatus.PUBLISHING,
        true);
    // Error filtering only works for own doc office -> published bgh decision with error will not
    // be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder().documentNumber("AB1234567803").documentationOffice(bghOfficeDTO),
        PublicationStatus.PUBLISHED,
        true);

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&withError=true")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567802", "AB1234567801"));
  }

  @Test
  void shouldFilterByDuplicateWarning() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567800");
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .ecli("same")
            .documentNumber("AB1234567801")
            .documentationOffice(docOfficeDTO));
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .ecli("same")
            .documentNumber("AB1234567802")
            .documentationOffice(docOfficeDTO));
    // Duplicate filtering only works for own doc office -> published bgh decision with duplicate
    // will not be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .ecli("same")
            .documentNumber("AB1234567803")
            .documentationOffice(bghOfficeDTO));

    duplicateCheckService.checkDuplicates("AB1234567801");

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&withDuplicateWarning=true")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567802", "AB1234567801"));
  }

  @Test
  void shouldFilterForOwnDocOffice() {
    // Published doc unit of BGH office should not be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, bghOfficeDTO, "AB1234567800");
    // Published doc unit of own office should be returned
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567801");

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&myDocOfficeOnly=true")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567801"));
  }

  @Test
  void shouldFilterForCourtType() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .court(courtAgAachen)
            .documentNumber("AB1234567800")
            .documentationOffice(docOfficeDTO));
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .court(courtOlgDresden)
            .documentNumber("AB1234567801")
            .documentationOffice(docOfficeDTO));

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&courtType=AG")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567800"));
  }

  @Test
  void shouldFilterForCourtLocation() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .court(courtAgAachen)
            .documentNumber("AB1234567800")
            .documentationOffice(docOfficeDTO));
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .court(courtOlgDresden)
            .documentNumber("AB1234567801")
            .documentationOffice(docOfficeDTO));

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&courtLocation=Dresden")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().getContent())
                    .extracting("documentNumber")
                    .containsExactly("AB1234567801"));
  }

  @Test
  void
      testSearch_withExternalUnassignedUser_shouldReturnNotEditableAndNotDeletableDocumentationUnit() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "AB1234567802");
    // Second doc unit won't match
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository, docOfficeDTO, "XX1234567802");

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumber=AB1234567802")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567802");
              assertThat(response.getResponseBody().getContent().getFirst().isEditable()).isFalse();
              assertThat(response.getResponseBody().getContent().getFirst().isDeletable())
                  .isFalse();
            });
  }

  @Test
  @Sql(
      scripts = {
        "classpath:doc_office_init.sql",
        "classpath:user_group_init.sql",
        "classpath:procedures_init.sql",
      })
  void testSearch_withExternalAssignedUser_shouldReturnEditableAndNotDeletableDocumentationUnit() {
    DecisionDTO documentationUnitDTO =
        (DecisionDTO) repository.findByDocumentNumber("docNumber00002").get();
    Optional<ProcedureDTO> procedureDTO =
        procedureRepository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);
    Optional<UserGroupDTO> userGroupDTO =
        userGroupRepository.findById(UUID.fromString("2b733549-d2cc-40f0-b7f3-9bfa9f3c1b89"));
    documentationUnitDTO.setProcedureHistory(List.of(procedureDTO.get()));
    documentationUnitDTO.setProcedure(procedureDTO.get());
    repository.save(documentationUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/procedure/"
                + procedureDTO.get().getId()
                + "/assign/"
                + userGroupDTO.get().getId())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumber="
                + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().getContent())
                  .extracting("documentNumber")
                  .containsExactly(documentationUnitDTO.getDocumentNumber());
              assertThat(response.getResponseBody().getContent()).hasSize(1);
              assertThat(response.getResponseBody().getContent().getFirst().isEditable()).isTrue();
              assertThat(response.getResponseBody().getContent().getFirst().isDeletable())
                  .isFalse();
            });
  }
}
