package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisBodySpec;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentNumberGeneratorService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class PreviousDecisionIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

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
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;

  private final AtomicInteger courtJurisId = new AtomicInteger(100);

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    doReturn(docOffice).when(userService).getDocumentationOffice(any(OidcUser.class));

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
                List.of(
                    PreviousDecisionDTO.builder()
                        .fileNumber("test")
                        .deviatingFileNumber("deviatest")
                        .rank(1)
                        .build()))
            .build();
    repository.save(parentDocumentUnitDTO);

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
              assertThat(responseBody.previousDecisions())
                  .extracting("deviatingFileNumber")
                  .containsExactly("deviatest");
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
              assertThat(Objects.requireNonNull(response.getResponseBody()).previousDecisions())
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

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(
                List.of(
                    PreviousDecision.builder()
                        .documentNumber(childDocumentUnitDTO.getDocumentNumber())
                        .deviatingFileNumber("deviatest")
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
              assertThat(response.getResponseBody().previousDecisions().get(0).isReferenceFound())
                  .isTrue();
              assertThat(response.getResponseBody().previousDecisions())
                  .extracting("deviatingFileNumber")
                  .containsExactly("deviatest");
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
    prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(3);
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyDate_shouldMatchOne() {
    LocalDate date1 = prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().decisionDate(date1).build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(1);
    PreviousDecision documentUnit = (PreviousDecision) content.get(0);
    assertThat(documentUnit.getDecisionDate()).isEqualTo(date1);
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyCourt_shouldMatchThree() {
    prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(
                PreviousDecision.builder().court(Court.builder().type("Court1").build()).build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(3);
    PreviousDecision documentUnit = (PreviousDecision) content.get(0);
    assertThat(documentUnit.getCourt()).extracting("type").isEqualTo("Court1");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyFileNumber_shouldMatchTwo() {
    prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().fileNumber("AkteX").build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(2);
    PreviousDecision documentUnit = (PreviousDecision) content.get(0);
    assertThat(documentUnit.getFileNumber()).isEqualTo("AkteX");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyFileNumber_shouldNotMatchDocNumber() {
    prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().fileNumber("XX").build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).isEmpty();
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_onlyDocumentType_shouldMatchOne() {
    prepareDocumentUnitDTOs();
    DocumentType documentType =
        DocumentTypeTransformer.transformToDomain(
            databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory("GH", category));
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().documentType(documentType).build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(1);
    PreviousDecision documentUnit = (PreviousDecision) content.get(0);
    assertThat(documentUnit.getDocumentType().jurisShortcut()).isEqualTo("GH");
  }

  @Test
  void testSearchForDocumentUnitsByPreviousDecisionInput_nullDocumentType_shouldAll() {
    prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().documentType(null).build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(3);
  }

  @Test
  void
      testSearchForDocumentUnitsByPreviousDecisionInput_threeMatchingOneDoesNot_shouldMatchNothing() {
    LocalDate date1 = prepareDocumentUnitDTOs();
    List<PreviousDecision> content =
        simulateAPICall(
                PreviousDecision.builder()
                    .decisionDate(date1)
                    .court(Court.builder().type("Court1").build())
                    .fileNumber("AkteX")
                    .documentType(
                        DocumentType.builder().uuid(UUID.randomUUID()).jurisShortcut("XY").build())
                    .build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).isEmpty();
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

    var du5 =
        createDocumentUnit(
            date,
            List.of("AkteZ"),
            "EF",
            "CC-RIS",
            Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    List<PreviousDecision> content =
        simulateAPICall(PreviousDecision.builder().fileNumber("AkteZ").build())
            .returnResult()
            .getResponseBody()
            .getContent();
    assertThat(content).hasSize(3);
    assertThat(content)
        .extracting(RelatedDocumentationUnit::getUuid)
        .containsExactlyInAnyOrder(du1.getId(), du2.getId(), du5.getId());
  }

  private LocalDate prepareDocumentUnitDTOs() {
    LocalDate date1 = LocalDate.parse("2023-01-02");

    createDocumentUnit(
        date1,
        List.of("AkteX", "AkteY"),
        "CD",
        "DS",
        Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    LocalDate date2 = LocalDate.parse("2023-02-03");

    createDocumentUnit(
        date2,
        null,
        "EF",
        "DS",
        Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());

    LocalDate date3 = LocalDate.parse("2023-03-04");

    createDocumentUnit(
        date3,
        List.of("AkteX"),
        "GH",
        "DS",
        Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
    return date1;
  }

  private RisBodySpec<SliceTestImpl<PreviousDecision>> simulateAPICall(
      PreviousDecision previousDecision) {
    return risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/KORE000000000/search-linkable-documentation-units?pg=0&sz=30")
        .bodyValue(previousDecision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<>() {});
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
