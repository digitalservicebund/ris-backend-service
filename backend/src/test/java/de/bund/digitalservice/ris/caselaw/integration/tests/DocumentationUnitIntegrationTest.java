package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.JURIS_PUBLISHED;
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
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberService.class,
      DatabaseDocumentUnitStatusService.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
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
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private UUID documentationOfficeUuid;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;

  @BeforeEach
  void setUp() {
    documentationOfficeUuid = documentationOfficeRepository.findByLabel(docOffice.label()).getId();

    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DigitalService");
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
                .build());

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findMetadataById(docUnit.getId())).isPresent();
  }

  @Test
  @Disabled
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
              assertThat(response.getResponseBody().documentNumber()).startsWith("XXRE");
              assertThat(response.getResponseBody().coreData().dateKnown()).isTrue();
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    DocumentationUnitDTO documentUnitDTO = list.get(0);
    assertThat(documentUnitDTO.getDocumentNumber()).startsWith("XXRE");
    assertThat(documentUnitDTO.getDecisionDate()).isNull();

    // TODO status
    // TODO
    //    assertThat(status.getCreatedAt()).isEqualTo(documentUnitDTO.getCreationtimestamp());
  }

  @Test
  @Disabled
  void testForFileNumbersDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .id(uuid)
            .documentNumber("1234567890123")
            //            .documentationOfficeId(documentationOfficeUuid)
            .build();

    DocumentationUnitDTO savedDto = repository.save(dto);

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            //            .creationtimestamp(dto.getCreationtimestamp())
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
        .uri("/api/v1/caselaw/documentunits/" + uuid)
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
  @Disabled
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
        DocumentTypeDTO.builder()
            .abbreviation("ABC")
            .category(categoryA)
            .label("ABC123")
            .multiple(true)
            .build();
    DocumentTypeDTO documentTypeDTOC =
        DocumentTypeDTO.builder()
            .abbreviation("ABC")
            .category(categoryC)
            .label("ABC123")
            .multiple(true)
            .build();

    databaseDocumentTypeRepository.saveAllAndFlush(List.of(documentTypeDTOA, documentTypeDTOC));

    var documentTypeDTOR =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder()
                .abbreviation("ABC")
                .category(categoryR)
                .label("ABC123")
                .multiple(true)
                .build());

    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber("1234567890123")
            //            .documentationOfficeId(documentationOfficeUuid)
            .build();
    repository.save(dto);

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .documentType(
                        DocumentType.builder()
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
    assertThat(list.get(0).getDocumentType()).isEqualTo(documentTypeDTOR);
    assertThat(list.get(0).getDocumentType()).isNull();
  }

  @Test
  @Disabled
  void testUndoSettingDocumentType() {
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber("1234567890123")
            .documentType(docType)
            //            .documentationOfficeId(documentationOfficeUuid)
            .build();
    repository.save(dto);

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
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
    assertThat(list.get(0).getDocumentType().getId()).isNull();
    assertThat(list.get(0).getDocumentType()).isNull();
  }

  @Test
  @Disabled
  void testSearchResultsAreDeterministic() {
    PublicationStatus[] published =
        new PublicationStatus[] {PUBLISHED, PUBLISHING, JURIS_PUBLISHED};
    Random random = new Random();
    Flux.range(0, 20)
        .map(index -> UUID.randomUUID())
        .map(
            uuid ->
                DocumentationUnitDTO.builder()
                    .id(uuid)
                    .documentNumber(RandomStringUtils.random(10, true, true))
                    //                    .documentationOfficeId(documentationOfficeUuid)
                    .build())
        .flatMap(documentUnitDTO -> Mono.just(repository.save(documentUnitDTO)))
        // TODO status
        //        .flatMap(
        //            documentUnitDTO ->
        //                documentUnitStatusRepository.save(
        //                    DocumentUnitStatusDTO.builder()
        //                        .newEntry(true)
        //                        .id(UUID.randomUUID())
        //                        .publicationStatus(published[random.nextInt(3)])
        //                        .documentUnitId(documentUnitDTO.getId())
        //                        .build()))
        .blockLast();
    assertThat(repository.findAll()).hasSize(20);

    List<UUID> responseUUIDs = new ArrayList<>();

    ProceedingDecision proceedingDecision = ProceedingDecision.builder().build();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/search-by-linked-documentation-unit?pg=0&sz=20")
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
            });

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/search-by-linked-documentation-unit?pg=0&sz=20")
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
  @Disabled
  void testSearchByDocumentUnitSearchInput() {
    DocumentationOffice otherDocOffice = buildDocOffice("BGH", "CO");
    UUID otherDocOfficeUuid =
        documentationOfficeRepository.findByLabel(otherDocOffice.label()).getId();

    List<UUID> docOfficeIds =
        List.of(
            documentationOfficeUuid,
            documentationOfficeUuid,
            documentationOfficeUuid,
            documentationOfficeUuid,
            otherDocOfficeUuid);
    List<String> documentNumbers =
        List.of(
            "ABCD202300007", "EFGH202200123", "IJKL202101234", "MNOP202300099", "QRST202200102");
    List<String> fileNumbers = List.of("jkl", "ghi", "def", "abc", "mno");
    List<String> courtTypes = List.of("MNO", "PQR", "STU", "VWX", "YZA");
    List<String> courtLocations = List.of("Hamburg", "München", "Berlin", "Frankfurt", "Köln");
    List<LocalDate> decisionDates =
        List.of(
            LocalDate.parse("2021-01-02T00:00:00.00Z"),
            LocalDate.parse("2022-02-03T00:00:00.00Z"),
            LocalDate.parse("2023-03-04T00:00:00.00Z"),
            LocalDate.parse("2023-08-01T00:00:00.00Z"),
            LocalDate.parse("2023-08-10T00:00:00.00Z"));
    List<PublicationStatus> statuses =
        List.of(PUBLISHED, UNPUBLISHED, PUBLISHING, PUBLISHED, UNPUBLISHED);
    List<Boolean> errorStatuses = List.of(false, true, true, false, true);

    for (int i = 0; i < 5; i++) {
      DocumentationUnitDTO dto =
          repository.save(
              DocumentationUnitDTO.builder()
                  .id(UUID.randomUUID())
                  //                      .creationtimestamp(Instant.now())
                  .documentNumber(documentNumbers.get(i))
                  //                      .courtType(courtTypes.get(i))
                  //                      .courtLocation(courtLocations.get(i))
                  .decisionDate(decisionDates.get(i))
                  //                      .documentationOfficeId(docOfficeIds.get(i))
                  .build());

      // TODO status
      //      documentUnitStatusRepository
      //          .save(
      //              DocumentUnitStatusDTO.builder()
      //                  .id(UUID.randomUUID())
      //                  .newEntry(true)
      //                  .documentUnitId(dto.getId())
      //                  .publicationStatus(statuses.get(i))
      //                  .withError(errorStatuses.get(i))
      //                  .build())
      //          .block();

      fileNumberRepository.save(
          FileNumberDTO.builder().documentationUnit(dto).value(fileNumbers.get(i)).build());
    }

    // no search criteria
    DocumentUnitSearchInput searchInput = DocumentUnitSearchInput.builder().build();
    // the unpublished one from the other docoffice is not in it, the others are ordered
    // by documentNumber
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("MNOP202300099", "IJKL202101234", "EFGH202200123", "ABCD202300007");

    // by documentNumber / fileNumber
    searchInput = DocumentUnitSearchInput.builder().documentNumberOrFileNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("MNOP202300099", "ABCD202300007");

    // by court
    searchInput =
        DocumentUnitSearchInput.builder().courtType("PQR").courtLocation("München").build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("EFGH202200123");

    // by decisionDate
    searchInput = DocumentUnitSearchInput.builder().decisionDate(decisionDates.get(2)).build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("IJKL202101234");

    // by status
    searchInput =
        DocumentUnitSearchInput.builder()
            .status(DocumentUnitStatus.builder().publicationStatus(PUBLISHING).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("IJKL202101234");

    // by error status
    searchInput =
        DocumentUnitSearchInput.builder()
            .status(DocumentUnitStatus.builder().withError(true).build())
            .build();
    // the docunit with error from the other docoffice should not appear
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("IJKL202101234", "EFGH202200123");

    // by documentation office
    searchInput = DocumentUnitSearchInput.builder().myDocOfficeOnly(true).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("MNOP202300099", "IJKL202101234", "EFGH202200123", "ABCD202300007");

    // between to decision dates
    LocalDate start = LocalDate.parse("2022-02-01T00:00:00.00Z");
    LocalDate end = LocalDate.parse("2023-08-05T00:00:00.00Z");
    searchInput =
        DocumentUnitSearchInput.builder().decisionDate(start).decisionDateEnd(end).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .containsExactly("MNOP202300099", "IJKL202101234", "EFGH202200123");

    // all combined
    searchInput =
        DocumentUnitSearchInput.builder()
            .documentNumberOrFileNumber("abc")
            .courtType("MNO")
            .courtLocation("Hamburg")
            .decisionDate(decisionDates.get(0))
            .status(DocumentUnitStatus.builder().publicationStatus(PUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("ABCD202300007");
  }

  private List<String> extractDocumentNumbersFromSearchCall(DocumentUnitSearchInput searchInput) {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("pg", "0");
    queryParams.add("sz", "30");

    if (searchInput.documentNumberOrFileNumber() != null) {
      queryParams.add("documentNumberOrFileNumber", searchInput.documentNumberOrFileNumber());
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
    return JsonPath.read(result.getResponseBody(), "$.content[*].documentNumber");
  }
}
