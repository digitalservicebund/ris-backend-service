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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
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

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    doReturn(Mono.just(docOffice)).when(userService).getDocumentationOffice(any(OidcUser.class));

    category =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("R").build());
  }

  @AfterEach
  void cleanUp() {
    // has to be cleaned first to avoid foreign key constraint violation in the following deletions
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
    databasePublishReportRepository.deleteAll();
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
  @Disabled("fix after documentation unit id in related documentation")
  void testLinkExistingProceedingDecision() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("1234567890123")
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO);

    DocumentationUnitDTO childDocumentUnitDTO =
        DocumentationUnitDTO.builder().documentNumber("abcdefghjikl").build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO);
    final UUID childDocumentationUnitUuid = childDocumentUnitDTO.getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(parentDocumentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .previousDecisions(List.of(PreviousDecision.builder() /*documentationUnitId*/.build()))
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
                  .extracting("documentationUnitId")
                  .containsExactly(childDocumentationUnitUuid);
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
  @Disabled("should be done by jpa cascades")
  void testRemoveProceedingDecisionLinkAndDeleteOrphanedDocumentUnit() {
    DocumentationUnitDTO parentDocumentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("1234567890123")
            .previousDecisions(List.of(PreviousDecisionDTO.builder().fileNumber("test").build()))
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
        .isOk();
  }

  @Test
  @Disabled("relevant if linking is migrated")
  void testLinkTheSameDocumentUnitsTwice() {
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
                    PreviousDecision.builder() /*documentationUnitId*/.build(),
                    PreviousDecision.builder() /*same documentationUnitId*/.build()))
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
                  .extracting("documentationUnitId")
                  .containsExactly("xxx");
            });
  }

  // TODO: tests for search
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_noSearchCriteria_shouldMatchAll() {
  //    prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(ProceedingDecision.builder().build())
  //        .jsonPath("$.content")
  //        .isNotEmpty()
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(3);
  //  }
  //
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_onlyDate_shouldMatchOne() {
  //    LocalDate date1 = prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(ProceedingDecision.builder().decisionDate(date1).build())
  //        .jsonPath("$.content")
  //        .isNotEmpty()
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(1)
  //        .jsonPath("$.content[0].decisionDate")
  //        .isEqualTo(date1.toString());
  //  }
  //
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_onlyCourt_shouldMatchOne() {
  //    prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(
  //
  // ProceedingDecision.builder().court(Court.builder().type("SomeCourt").build()).build())
  //        .consumeWith(
  //            result -> {
  //              System.out.println("result = " + result.toString());
  //            })
  //        .jsonPath("$.content")
  //        .isNotEmpty()
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(1)
  //        .jsonPath("$.content[0].court.type")
  //        .isEqualTo("SomeCourt");
  //  }
  //
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_onlyFileNumber_shouldMatchTwo() {
  //    prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(ProceedingDecision.builder().fileNumber("AkteX").build())
  //        .jsonPath("$.content")
  //        .isNotEmpty()
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(2)
  //        .jsonPath("$.content[0].fileNumber")
  //        .isEqualTo("AkteX");
  //  }
  //
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_onlyDocumentType_shouldMatchOne() {
  //    prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(
  //            ProceedingDecision.builder()
  //                .documentType(DocumentType.builder().jurisShortcut("GH").build())
  //                .build())
  //        .jsonPath("$.content")
  //        .isArray()
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(1)
  //        .jsonPath("$.content[0].documentType.jurisShortcut")
  //        .isEqualTo("GH");
  //  }
  //
  //  @Test
  //  void
  //
  // testSearchForDocumentUnitsByProceedingDecisionInput_threeMatchingOneDoesNot_shouldMatchNothing() {
  //    LocalDate date1 = prepareDocumentUnitMetadataDTOs();
  //    simulateAPICall(
  //            ProceedingDecision.builder()
  //                .decisionDate(date1)
  //                .court(Court.builder().type("SomeCourt").build())
  //                .fileNumber("AkteX")
  //                .documentType(DocumentType.builder().jurisShortcut("XY").build())
  //                .build())
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(0);
  //  }
  //
  //  @Test
  //  void testSearchForDocumentUnitsByProceedingDecisionInput_shouldOnlyFindPublished() {
  //    LocalDate date = LocalDate.parse("2023-02-02T00:00:00.00Z");
  //
  //    var du1 =
  //        createDocumentUnit(
  //            "Court1",
  //            "Berlin",
  //            date,
  //            List.of("AkteZ"),
  //            "EF",
  //            "DigitalService",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build());
  //    var du2 =
  //        createDocumentUnit(
  //            "Court2",
  //            "Berlin",
  //            date,
  //            List.of("AkteZ"),
  //            "EF",
  //            "DigitalService",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
  //
  //    var du4 =
  //        createDocumentUnit(
  //            "Court4",
  //            "Berlin",
  //            date,
  //            List.of("AkteZ"),
  //            "EF",
  //            "CC-RIS",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build());
  //
  //    var du5 =
  //        createDocumentUnit(
  //            "Court5",
  //            "Berlin",
  //            date,
  //            List.of("AkteZ"),
  //            "EF",
  //            "CC-RIS",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
  //    var du6 =
  //        createDocumentUnit(
  //            "Court6",
  //            "Berlin",
  //            date,
  //            List.of("AkteZ"),
  //            "EF",
  //            "CC-RIS",
  //            DocumentUnitStatus.builder()
  //                .publicationStatus(PublicationStatus.JURIS_PUBLISHED)
  //                .build());
  //
  //    simulateAPICall(ProceedingDecision.builder().fileNumber("AkteZ").build())
  //        .jsonPath("$.content.length()")
  //        .isEqualTo(3)
  //        .jsonPath("$.content[?(@.uuid=='" + du1.getUuid() + "')]")
  //        .isEmpty()
  //        .jsonPath("$.content[?(@.uuid=='" + du2.getUuid() + "')]")
  //        .isArray()
  //        .jsonPath("$.content[?(@.uuid=='" + du4.getUuid() + "')]")
  //        .isEmpty()
  //        .jsonPath("$.content[?(@.uuid=='" + du5.getUuid() + "')]")
  //        .isArray()
  //        .jsonPath("$.content[?(@.uuid=='" + du6.getUuid() + "')]")
  //        .isArray();
  //  }
  //
  //  private LocalDate prepareDocumentUnitMetadataDTOs() {
  //    LocalDate date1 = LocalDate.parse("2023-01-02T00:00:00.00Z");
  //    DocumentUnitMetadataDTO documentUnit1 =
  //        createDocumentUnit(
  //            "SomeCourt",
  //            "Berlin",
  //            date1,
  //            List.of("AkteX", "AkteY"),
  //            "CD",
  //            "DigitalService",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
  //
  //    LocalDate date2 = LocalDate.parse("2023-02-03T00:00:00.00Z");
  //    DocumentUnitMetadataDTO documentUnit2 =
  //        createDocumentUnit(
  //            "AnotherCourt",
  //            "Hamburg",
  //            date2,
  //            null,
  //            "EF",
  //            "DigitalService",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
  //
  //    LocalDate date3 = LocalDate.parse("2023-03-04T00:00:00.00Z");
  //    DocumentUnitMetadataDTO documentUnit3 =
  //        createDocumentUnit(
  //            "YetAnotherCourt",
  //            "Munich",
  //            date3,
  //            List.of("AkteX"),
  //            "GH",
  //            "DigitalService",
  //
  // DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build());
  //    return date1;
  //  }
  //
  //  private BodyContentSpec simulateAPICall(ProceedingDecision proceedingDecisionSearchInput) {
  //    return risWebTestClient
  //        .withDefaultLogin()
  //        .put()
  //        .uri("/api/v1/caselaw/documentunits/search-by-linked-documentation-unit?pg=0&sz=30")
  //        .bodyValue(proceedingDecisionSearchInput)
  //        .exchange()
  //        .expectStatus()
  //        .isOk()
  //        .expectBody();
  //  }
  //
  //  private DocumentUnitMetadataDTO createDocumentUnit(
  //      String courtType,
  //      String courtLocation,
  //      LocalDate decisionDate,
  //      List<String> fileNumbers,
  //      String documentTypeJurisShortcut,
  //      String documentOfficeLabel,
  //      DocumentUnitStatus status) {
  //
  //    UUID documentTypeId = null;
  //    if (documentTypeJurisShortcut != null) {
  //
  //      var documentType =
  //          databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
  //              documentTypeJurisShortcut, category);
  //
  //      if (documentType == null) {
  //        DocumentTypeDTO documentTypeDTO =
  //            DocumentTypeDTO.builder()
  //                .category(category)
  //                .label("ABC123")
  //                .multiple(true)
  //                .abbreviation(documentTypeJurisShortcut)
  //                .build();
  //        documentTypeId = databaseDocumentTypeRepository.saveAndFlush(documentTypeDTO).getId();
  //      } else {
  //        documentTypeId = documentType.getId();
  //      }
  //    }
  //
  //    DocumentationOfficeDTO documentOffice =
  //        documentationOfficeRepository.findByAbbreviation(documentOfficeLabel);
  //    assertThat(documentOffice).isNotNull();
  //
  //    DocumentUnitMetadataDTO documentUnitMetadataDTO =
  //        DocumentUnitMetadataDTO.builder()
  //            .uuid(UUID.randomUUID())
  //            .documentationOfficeId(documentOffice.getId())
  //            .documentnumber(RandomStringUtils.randomAlphanumeric(13))
  //            .creationtimestamp(Instant.now())
  //            .courtType(courtType)
  //            .courtLocation(courtLocation)
  //            .decisionDate(decisionDate)
  //            .documentTypeId(documentTypeId)
  //            .dataSource(DataSource.NEURIS)
  //            .documentationOfficeId(documentOffice.getId())
  //            .build();
  //    Long id = metadataRepository.save(documentUnitMetadataDTO).block().getId();
  //
  //    List<FileNumberDTO> fileNumberDTOs;
  //    if (fileNumbers != null) {
  //      fileNumberDTOs =
  //          fileNumbers.stream()
  //              .map(fn -> FileNumberDTO.builder().fileNumber(fn).documentUnitId(id).build())
  //              .collect(Collectors.toList());
  //      fileNumberRepository.saveAll(fileNumberDTOs).collectList().block();
  //    }
  //
  //    if (status == null) {
  //      return documentUnitMetadataDTO;
  //    }
  //
  //    assertThat(
  //            statusRepository
  //                .save(
  //                    DocumentUnitStatusDTO.builder()
  //                        .id(UUID.randomUUID())
  //                        .documentUnitId(documentUnitMetadataDTO.getUuid())
  //                        .publicationStatus(status.publicationStatus())
  //                        .withError(status.withError())
  //                        .newEntry(true)
  //                        .build())
  //                .block())
  //        .isNotNull();
  //    return documentUnitMetadataDTO;
  //  }
}
