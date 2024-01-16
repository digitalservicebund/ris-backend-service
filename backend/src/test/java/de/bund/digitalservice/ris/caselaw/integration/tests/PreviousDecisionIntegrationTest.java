package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class PreviousDecisionIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14")
          .withInitScript("db/create_migration_scheme_and_extensions.sql");

  private DocumentCategoryDTO category;
  private CourtDTO testCourt;

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
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseCourtRepository courtRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabasePublicationReportRepository databasePublishReportRepository;

  @MockBean UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;

  private final AtomicInteger courtJurisId = new AtomicInteger(100);

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    doReturn(Mono.just(docOffice)).when(userService).getDocumentationOffice(any(OidcUser.class));

    category =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("R").build());

    testCourt =
        courtRepository.save(
            CourtDTO.builder()
                .type("Court1")
                .location("Berlin")
                .jurisId(courtJurisId.getAndIncrement())
                .isForeignCourt(false)
                .isSuperiorCourt(false)
                .id(UUID.randomUUID())
                .build());
  }

  @AfterEach
  void cleanUp() {
    // has to be cleaned first to avoid foreign key constraint violation in the following deletions
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
    databasePublishReportRepository.deleteAll();
    courtRepository.deleteAll();
    databaseDocumentCategoryRepository.delete(category);
  }

  @Test
  void
      testGetDocumentationUnit_withPreviousDecision_shouldReturnDocumentationUnitWithListOfExistingPreviousDecsion() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("documntnumber")
            .previousDecisions(
                List.of(PreviousDecisionDTO.builder().fileNumber("test").rank(1).build()))
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documntnumber")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody.previousDecisions())
                  .extracting("fileNumber")
                  .containsExactly("test");
            });
  }

  @Test
  void testAddPreviousDecisionToEmptyPreviousDecisionList_shouldContainTheNewEntry() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("documntnumber")
            .build();
    repository.save(parentDocumentUnitDTO);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(List.of(PreviousDecision.builder().fileNumber("test").build()))
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentDocumentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().previousDecisions())
                  .extracting("fileNumber")
                  .containsExactly("test");
            });
  }

  @Test
  void testLinkExistingPreviousDecision() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("1234567890123")
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO);

    DocumentationUnitDTO childDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentNumber("abcdefghjikl")
            .decisionDate(LocalDate.parse("2021-01-01"))
            .documentationOffice(documentationOfficeDTO)
            .build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO);
    final UUID childDocumentationUnitUuid = childDocumentUnitDTO.getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(
                List.of(
                    PreviousDecision.builder()
                        .uuid(childDocumentationUnitUuid)
                        .documentNumber(childDocumentUnitDTO.getDocumentNumber())
                        .build()))
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentDocumentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().previousDecisions())
                  .extracting("documentNumber")
                  .containsExactly("abcdefghjikl");
            });
  }

  @Test
  void testRemovePreviousDecision_withEmptyList_shouldRemoveAllPreviousDecisions() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("1234567890123")
            .previousDecisions(
                List.of(PreviousDecisionDTO.builder().fileNumber("test").rank(1).build()))
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(Collections.emptyList())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentDocumentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().previousDecisions()).isEmpty();
            });
  }

  @Test
  void testLinkTheSameDocumentUnitsTwice() {
    DocumentationUnitDTO childDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentNumber("xxx")
            .documentationOffice(documentationOfficeDTO)
            .build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO);
    final UUID childDocumentationUnitUuid = childDocumentUnitDTO.getId();

    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("1234567890123")
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(
                List.of(
                    PreviousDecision.builder()
                        .uuid(childDocumentationUnitUuid)
                        .documentNumber("xxx")
                        .build(),
                    PreviousDecision.builder()
                        .uuid(childDocumentationUnitUuid)
                        .documentNumber("xxx")
                        .build()))
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentDocumentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().previousDecisions()).hasSize(2);
              assertThat(response.getResponseBody().previousDecisions().get(0))
                  .extracting("documentNumber")
                  .isEqualTo("xxx");
            });
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_noSearchCriteria_shouldMatchAll() {
    prepareDocumentUnitMetadataDTOs();
    simulateAPICall(PreviousDecision.builder().build())
        .jsonPath("$.content")
        .isNotEmpty()
        .jsonPath("$.content.length()")
        .isEqualTo(3);
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyDate_shouldMatchOne() {
    LocalDate date1 = prepareDocumentUnitMetadataDTOs();
    simulateAPICall(PreviousDecision.builder().decisionDate(date1).build())
        .jsonPath("$.content")
        .isNotEmpty()
        .jsonPath("$.content.length()")
        .isEqualTo(1)
        .jsonPath("$.content[0].decisionDate")
        .isEqualTo(date1.toString());
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyCourt_shouldMatchThree() {
    prepareDocumentUnitMetadataDTOs();
    simulateAPICall(
            PreviousDecision.builder().court(Court.builder().type("Court1").build()).build())
        .jsonPath("$.content")
        .isNotEmpty()
        .jsonPath("$.content.length()")
        .isEqualTo(3)
        .jsonPath("$.content[0].court.type")
        .isEqualTo("Court1");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyFileNumber_shouldMatchTwo() {
    prepareDocumentUnitMetadataDTOs();
    simulateAPICall(PreviousDecision.builder().fileNumber("AkteX").build())
        .jsonPath("$.content")
        .isNotEmpty()
        .jsonPath("$.content.length()")
        .isEqualTo(2)
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteX");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyFileNumber_shouldNotMatchDocNumber() {
    prepareDocumentUnitMetadataDTOs();
    simulateAPICall(PreviousDecision.builder().fileNumber("XX").build())
        .jsonPath("$.content")
        .isEmpty();
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyDocumentType_shouldMatchOne() {
    prepareDocumentUnitMetadataDTOs();
    DocumentType documentType =
        DocumentTypeTransformer.transformToDomain(
            databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory("GH", category));
    simulateAPICall(PreviousDecision.builder().documentType(documentType).build())
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content.length()")
        .isEqualTo(1)
        .jsonPath("$.content[0].documentType.jurisShortcut")
        .isEqualTo("GH");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_nullDocumentType_shouldAll() {
    prepareDocumentUnitMetadataDTOs();
    simulateAPICall(PreviousDecision.builder().documentType(null).build())
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content.length()")
        .isEqualTo(3);
  }

  @Test
  void
      testSearchForDocumentUnitsByPreviousDecisionInput_threeMatchingOneDoesNot_shouldMatchNothing() {
    LocalDate date1 = prepareDocumentUnitMetadataDTOs();
    simulateAPICall(
            PreviousDecision.builder()
                .decisionDate(date1)
                .court(Court.builder().type("Court1").build())
                .fileNumber("AkteX")
                .documentType(
                    DocumentType.builder().uuid(UUID.randomUUID()).jurisShortcut("XY").build())
                .build())
        .jsonPath("$.content.length()")
        .isEqualTo(0);
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_shouldOnlyFindPublishedOrMine() {
    LocalDate date = LocalDate.parse("2023-02-02");

    var du1 =
        createDocumentUnit(
            date,
            List.of("AkteZ"),
            "EF",
            "DS",
            Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build());
    var du2 =
        createDocumentUnit(
            date,
            List.of("AkteZ"),
            "EF",
            "DS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    var du4 =
        createDocumentUnit(
            date,
            List.of("AkteZ"),
            "EF",
            "CC-RIS",
            Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build());

    var du5 =
        createDocumentUnit(
            date,
            List.of("AkteZ"),
            "EF",
            "CC-RIS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    simulateAPICall(PreviousDecision.builder().fileNumber("AkteZ").build())
        .jsonPath("$.content.length()")
        .isEqualTo(3)
        .jsonPath("$.content[?(@.uuid=='" + du1.getId() + "')]")
        .isArray()
        .jsonPath("$.content[?(@.uuid=='" + du2.getId() + "')]")
        .isArray()
        .jsonPath("$.content[?(@.uuid=='" + du4.getId() + "')]")
        .isEmpty()
        .jsonPath("$.content[?(@.uuid=='" + du5.getId() + "')]")
        .isArray();
  }

  private LocalDate prepareDocumentUnitMetadataDTOs() {
    LocalDate date1 = LocalDate.parse("2023-01-02");
    DocumentationUnitMetadataDTO documentUnit1 =
        createDocumentUnit(
            date1,
            List.of("AkteX", "AkteY"),
            "CD",
            "DS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    LocalDate date2 = LocalDate.parse("2023-02-03");
    DocumentationUnitMetadataDTO documentUnit2 =
        createDocumentUnit(
            date2,
            null,
            "EF",
            "DS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    LocalDate date3 = LocalDate.parse("2023-03-04");
    DocumentationUnitMetadataDTO documentUnit3 =
        createDocumentUnit(
            date3,
            List.of("AkteX"),
            "GH",
            "DS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
    return date1;
  }

  private WebTestClient.BodyContentSpec simulateAPICall(
      PreviousDecision PreviousDecisionSearchInput) {
    return risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/KORE000000000/search-linkable-documentation-units?pg=0&sz=30")
        .bodyValue(PreviousDecisionSearchInput)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody();
  }

  private DocumentationUnitDTO createDocumentUnit(
      LocalDate decisionDate,
      List<String> fileNumbers,
      String documentTypeJurisShortcut,
      String documentOfficeLabel,
      Status status) {

    DocumentTypeDTO documentTypeDTO = null;
    if (documentTypeJurisShortcut != null) {

      var documentType =
          databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
              documentTypeJurisShortcut, category);

      if (documentType == null) {
        documentTypeDTO =
            DocumentTypeDTO.builder()
                .category(category)
                .label("ABC123")
                .multiple(true)
                .abbreviation(documentTypeJurisShortcut)
                .build();
        documentTypeDTO = databaseDocumentTypeRepository.saveAndFlush(documentTypeDTO);
      }
    }

    DocumentationOfficeDTO documentOffice =
        documentationOfficeRepository.findByAbbreviation(documentOfficeLabel);
    assertThat(documentOffice).isNotNull();

    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentationOffice(documentOffice)
            .documentNumber("XX" + RandomStringUtils.randomAlphanumeric(11))
            .court(testCourt)
            .decisionDate(decisionDate)
            .documentType(documentTypeDTO)
            .documentationOffice(documentOffice)
            .build();
    documentationUnitDTO = repository.save(documentationUnitDTO);

    UUID docUnitId = documentationUnitDTO.getId();

    documentationUnitDTO = repository.findById(docUnitId).get();

    documentationUnitDTO =
        documentationUnitDTO.toBuilder()
            .status(
                status == null
                    ? null
                    : List.of(
                        StatusDTO.builder()
                            .id(UUID.randomUUID())
                            .publicationStatus(status.publicationStatus())
                            .withError(status.withError())
                            .documentationUnitDTO(documentationUnitDTO)
                            .createdAt(Instant.now())
                            .build()))
            .build();

    if (fileNumbers != null) {
      documentationUnitDTO.setFileNumbers(
          fileNumbers.stream()
              .map(
                  fn ->
                      FileNumberDTO.builder()
                          .value(fn)
                          .rank(1L)
                          .documentationUnit(DocumentationUnitDTO.builder().id(docUnitId).build())
                          .build())
              .toList());
    }

    documentationUnitDTO = repository.save(documentationUnitDTO);

    return documentationUnitDTO;
  }
}
