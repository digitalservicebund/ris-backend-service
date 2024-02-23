package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentationUnitIntegrationTest {
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
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository courtRepository;
  @Autowired private DatabaseRegionRepository regionRepository;
  @Autowired private DatabaseDocumentNumberRepository databaseDocumentNumberRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private UUID documentationOfficeUuid;

  @BeforeEach
  void setUp() {
    documentationOfficeUuid =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()).getId();

    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll();
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
  }

  @Test
  void testMetadataCanBeRetrieved() {
    var docUnit =
        repository.save(
            DocumentationUnitDTO.builder()
                .caseFacts("abc")
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findMetadataById(docUnit.getId())).isPresent();
  }

  @Test
  @Transactional(transactionManager = "jpaTransactionManager")
  void testForCorrectDbEntryAfterNewDocumentUnitCreation() {

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("XXRE0");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    DocumentationUnitDTO documentUnitDTO = list.get(0);
    assertThat(documentUnitDTO.getDocumentNumber()).startsWith("XXRE0");
    assertThat(documentUnitDTO.getDecisionDate()).isNull();

    assertThat(documentUnitDTO.getStatus()).hasSize(1);

    assertThat(documentUnitDTO.getStatus().get(0).getPublicationStatus()).isEqualTo(UNPUBLISHED);
    assertThat(documentUnitDTO.getStatus().get(0).isWithError()).isFalse();
  }

  @Test
  void testForFileNumbersDbEntryAfterUpdateByUuid() {

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .fileNumbers(List.of("AkteX"))
                    .documentationOffice(docOffice)
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().fileNumbers().get(0))
                  .isEqualTo("AkteX");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentNumber()).isEqualTo("1234567890123");

    List<FileNumberDTO> fileNumberEntries =
        fileNumberRepository.findAllByDocumentationUnit(list.get(0));
    assertThat(fileNumberEntries).hasSize(1);
    assertThat(fileNumberEntries.get(0).getValue()).isEqualTo("AkteX");
  }

  @Test
  void testDeleteLeadingDecisionNormReferencesForNonBGHDecisions() {
    CourtDTO bghCourt =
        courtRepository.save(
            CourtDTO.builder()
                .type("BGH")
                .isSuperiorCourt(true)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());
    CourtDTO lgCourt =
        courtRepository.save(
            CourtDTO.builder()
                .type("LG")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .leadingDecisionNormReferences(
                    List.of(
                        LeadingDecisionNormReferenceDTO.builder()
                            .normReference("BGB §1")
                            .rank(1)
                            .build()))
                .court(bghCourt)
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .leadingDecisionNormReferences(List.of("BGB §1"))
                    .documentationOffice(docOffice)
                    .court(Court.builder().id(lgCourt.getId()).build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().leadingDecisionNormReferences())
                  .isEmpty();
            });
  }

  @Test
  void testSetRegionForCourt() {
    RegionDTO region =
        regionRepository.save(RegionDTO.builder().id(UUID.randomUUID()).code("DEU").build());

    CourtDTO bghCourt =
        courtRepository.save(
            CourtDTO.builder()
                .type("BGH")
                .location("Karlsruhe")
                .isSuperiorCourt(true)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .region(region)
                .build());

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .documentationOffice(docOffice)
                    .court(Court.builder().id(bghCourt.getId()).build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().region()).isEqualTo("DEU");
            });
  }

  @Test
  void testDocumentTypeToSetIdFromLookuptable() {
    var categoryA =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("A").build());
    var categoryR =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("R").build());
    var categoryC =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("C").build());

    DocumentTypeDTO documentTypeDTOA =
        databaseDocumentTypeRepository.save(
            DocumentTypeDTO.builder()
                .abbreviation("ABC")
                .category(categoryA)
                .label("ABC123")
                .multiple(true)
                .build());
    DocumentTypeDTO documentTypeDTOC =
        databaseDocumentTypeRepository.save(
            DocumentTypeDTO.builder()
                .abbreviation("ABC")
                .category(categoryC)
                .label("ABC123")
                .multiple(true)
                .build());

    var documentTypeDTOR =
        databaseDocumentTypeRepository.save(
            DocumentTypeDTO.builder()
                .abbreviation("ABC")
                .category(categoryR)
                .label("ABC123")
                .multiple(true)
                .build());

    // TODO find out why this is necessary when the whole test class is executed
    repository.deleteAll();
    DocumentationUnitDTO documentationUnitDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(documentationUnitDto.getId())
            .documentNumber(documentationUnitDto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .documentType(
                        DocumentType.builder()
                            .uuid(documentTypeDTOR.getId())
                            .jurisShortcut(documentTypeDTOR.getAbbreviation())
                            .label(documentTypeDTOR.getLabel())
                            .build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType().label())
                  .isEqualTo(documentTypeDTOR.getLabel());
              assertThat(response.getResponseBody().coreData().documentType().jurisShortcut())
                  .isEqualTo(documentTypeDTOR.getAbbreviation());
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentType().getId()).isEqualTo(documentTypeDTOR.getId());
    assertThat(list.get(0).getDocumentType()).isNotNull();
  }

  @Test
  void testUndoSettingDocumentType() {
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentType(docType)
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .build());

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findById(dto.getId())).isPresent();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).documentType(null).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType()).isNull();
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentType()).isNull();
  }

  @Test
  void testSearchResultsAreDeterministic() {
    var office = documentationOfficeRepository.findByAbbreviation("DS");

    var documentNumberToExclude = "KORE000000000";

    for (int i = 0; i < 21; i++) {
      var randomDocNumber =
          i == 0 ? documentNumberToExclude : RandomStringUtils.random(10, true, true);
      CourtDTO court =
          courtRepository.save(
              CourtDTO.builder()
                  .type("LG")
                  .location("Kassel")
                  .isSuperiorCourt(true)
                  .isForeignCourt(false)
                  .jurisId(i)
                  .build());

      DocumentationUnitDTO dto =
          repository.save(
              DocumentationUnitDTO.builder()
                  .documentNumber(randomDocNumber)
                  .court(court)
                  .documentationOffice(office)
                  .build());

      dto = repository.findById(dto.getId()).get();

      repository.save(
          dto.toBuilder()
              .status(
                  List.of(
                      StatusDTO.builder()
                          .documentationUnitDTO(dto)
                          .publicationStatus(PUBLISHED)
                          .createdAt(Instant.now())
                          .build()))
              .build());
    }

    assertThat(repository.findAll()).hasSize(21);

    List<UUID> responseUUIDs = new ArrayList<>();

    PreviousDecision proceedingDecision = PreviousDecision.builder().build();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentNumberToExclude
                + "/search-linkable-documentation-units?pg=0&sz=20")
        .bodyValue(proceedingDecision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content.length()")
        .isEqualTo(20)
        .consumeWith(
            response -> {
              String responseBody = new String(response.getResponseBody(), StandardCharsets.UTF_8);
              assertThat(responseBody).isNotNull();

              List<String> uuids = JsonPath.read(responseBody, "$.content[*].uuid");
              assertThat(uuids).hasSize(20);
              responseUUIDs.addAll(uuids.stream().map(UUID::fromString).toList());

              // make sure the documentNumber originating the search is not in the result
              List<String> documentNumbers =
                  JsonPath.read(responseBody, "$.content[*].documentNumber");
              assertThat(documentNumbers).isNotEmpty();
              assertThat(documentNumbers).doesNotContain(documentNumberToExclude);
            });

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentNumberToExclude
                + "/search-linkable-documentation-units?pg=0&sz=20")
        .bodyValue(proceedingDecision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content.length()")
        .isEqualTo(20)
        .consumeWith(
            response -> {
              String responseBody = new String(response.getResponseBody(), StandardCharsets.UTF_8);
              assertThat(responseBody).isNotNull();

              List<String> uuids = JsonPath.read(responseBody, "$.content[*].uuid");
              List<UUID> responseUUIDs2 = uuids.stream().map(UUID::fromString).toList();

              assertThat(responseUUIDs2).isEqualTo(responseUUIDs);
            });
  }

  @Test
  void testSearchByDocumentUnitSearchInput() {
    DocumentationOffice otherDocOffice = buildDocOffice("BGH");
    UUID otherDocOfficeUuid =
        documentationOfficeRepository.findByAbbreviation(otherDocOffice.abbreviation()).getId();

    List<UUID> docOfficeIds =
        List.of(
            documentationOfficeUuid,
            documentationOfficeUuid,
            documentationOfficeUuid,
            documentationOfficeUuid,
            otherDocOfficeUuid,
            otherDocOfficeUuid);
    List<String> documentNumbers =
        List.of(
            "ABCD202300007",
            "EFGH202200123",
            "IJKL202101234",
            "MNOP202300099",
            "QRST202200102",
            "UVWX202311090");
    List<String> fileNumbers = List.of("jkl", "ghi", "def", "ABC", "mno", "pqr");
    List<String> courtTypes = List.of("MNO", "PQR", "STU", "VWX", "YZA", "BCD");
    List<String> courtLocations =
        List.of("Hamburg", "München", "Berlin", "Frankfurt", "Köln", "Leipzig");
    List<LocalDate> decisionDates =
        List.of(
            LocalDate.parse("2021-01-02"),
            LocalDate.parse("2022-02-03"),
            LocalDate.parse("2023-03-04"),
            LocalDate.parse("2023-08-01"),
            LocalDate.parse("2023-08-10"),
            LocalDate.parse("2023-09-10"));
    List<PublicationStatus> statuses =
        List.of(PUBLISHED, UNPUBLISHED, PUBLISHING, PUBLISHED, UNPUBLISHED, PUBLISHED);
    List<Boolean> errorStatuses = List.of(false, true, true, false, true, true);

    for (int i = 0; i < 6; i++) {

      CourtDTO court =
          courtRepository.save(
              CourtDTO.builder()
                  .type(courtTypes.get(i))
                  .location(courtLocations.get(i))
                  .isSuperiorCourt(true)
                  .isForeignCourt(false)
                  .jurisId(new Random().nextInt())
                  .build());
      DocumentationUnitDTO dto =
          repository.save(
              DocumentationUnitDTO.builder()
                  .id(UUID.randomUUID())
                  .documentNumber(documentNumbers.get(i))
                  .court(court)
                  .decisionDate(decisionDates.get(i))
                  .documentationOffice(
                      DocumentationOfficeDTO.builder().id(docOfficeIds.get(i)).build())
                  .build());

      dto = repository.findById(dto.getId()).get();

      repository.save(
          dto.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .documentationUnit(dto)
                          .value(fileNumbers.get(i))
                          .rank((long) i)
                          .build()))
              .build());

      dto = repository.findById(dto.getId()).get();

      repository.save(
          dto.toBuilder()
              .status(
                  List.of(
                      StatusDTO.builder()
                          .documentationUnitDTO(dto)
                          .publicationStatus(statuses.get(i))
                          .createdAt(Instant.now())
                          .withError(errorStatuses.get(i))
                          .build()))
              .build());
    }

    // no search criteria
    DocumentationUnitSearchInput searchInput = DocumentationUnitSearchInput.builder().build();
    // the unpublished one from the other docoffice is not in it, the others are ordered
    // by documentNumber
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly(
            "ABCD202300007", "EFGH202200123", "IJKL202101234", "MNOP202300099", "UVWX202311090");

    // by documentNumber
    searchInput = DocumentationUnitSearchInput.builder().documentNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("ABCD202300007");

    // by fileNumber
    searchInput = DocumentationUnitSearchInput.builder().fileNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("MNOP202300099");

    // by documentNumber & fileNumber
    searchInput =
        DocumentationUnitSearchInput.builder().fileNumber("abc").documentNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).isEmpty();

    // by court
    searchInput =
        DocumentationUnitSearchInput.builder().courtType("pqr").courtLocation("münchen").build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("EFGH202200123");

    // by decisionDate
    searchInput = DocumentationUnitSearchInput.builder().decisionDate(decisionDates.get(2)).build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("IJKL202101234");

    // by status
    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().publicationStatus(UNPUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("EFGH202200123");

    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().publicationStatus(PUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("ABCD202300007", "MNOP202300099", "UVWX202311090");

    // by error status
    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().withError(true).build())
            .build();
    // the docunit with error from the other docoffice should not appear
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("EFGH202200123", "IJKL202101234");

    // by documentation office
    searchInput = DocumentationUnitSearchInput.builder().myDocOfficeOnly(true).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("ABCD202300007", "EFGH202200123", "IJKL202101234", "MNOP202300099");

    // between two decision dates
    LocalDate start = LocalDate.parse("2022-02-01");
    LocalDate end = LocalDate.parse("2023-08-05");
    searchInput =
        DocumentationUnitSearchInput.builder().decisionDate(start).decisionDateEnd(end).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("EFGH202200123", "IJKL202101234", "MNOP202300099");

    // all combined
    searchInput =
        DocumentationUnitSearchInput.builder()
            .documentNumber("abc")
            .courtType("MNO")
            .courtLocation("Hamburg")
            .decisionDate(decisionDates.get(0))
            .status(Status.builder().publicationStatus(PUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("ABCD202300007");
  }

  private List<String> extractDocumentNumbersFromSearchCall(
      DocumentationUnitSearchInput searchInput) {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("pg", "0");
    queryParams.add("sz", "30");

    if (searchInput.documentNumber() != null) {
      queryParams.add("documentNumber", searchInput.documentNumber());
    }

    if (searchInput.fileNumber() != null) {
      queryParams.add("fileNumber", searchInput.fileNumber());
    }

    if (searchInput.courtType() != null) {
      queryParams.add("courtType", searchInput.courtType());
    }

    if (searchInput.courtLocation() != null) {
      queryParams.add("courtLocation", searchInput.courtLocation());
    }

    if (searchInput.decisionDate() != null) {
      queryParams.add("decisionDate", searchInput.decisionDate().toString());
    }

    if (searchInput.decisionDateEnd() != null) {
      queryParams.add("decisionDateEnd", searchInput.decisionDateEnd().toString());
    }

    if (searchInput.status() != null) {
      if (searchInput.status().publicationStatus() != null) {
        queryParams.add("publicationStatus", searchInput.status().publicationStatus().toString());
      }
      queryParams.add("withError", String.valueOf(searchInput.status().withError()));
    }

    queryParams.add("myDocOfficeOnly", String.valueOf(searchInput.myDocOfficeOnly()));

    EntityExchangeResult<String> result;
    result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/v1/caselaw/documentunits/search")
                        .queryParams(queryParams)
                        .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
    System.out.println(JsonPath.read(result.getResponseBody(), "$.content[*]").toString());
    return JsonPath.read(result.getResponseBody(), "$.content[*].documentNumber");
  }
}
