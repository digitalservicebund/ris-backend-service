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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDeviatingDecisionDateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseIncorrectCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
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
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
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
import org.apache.commons.lang3.RandomStringUtils;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

/**
 * @deprecated use {@link DocumentationUnitIntegrationTest} instead
 */
@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
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
@Deprecated
class DocumentUnitIntegrationTest {
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
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitMetadataRepository previousDecisionRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DeviatingEcliRepository deviatingEcliRepository;
  @Autowired private StateRepository stateRepository;
  @Autowired private DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseIncorrectCourtRepository incorrectCourtRepository;
  @Autowired private DatabaseDocumentUnitStatusRepository documentUnitStatusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @Autowired private DatabasePublicationReportRepository databasePublishReportRepository;

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
    documentationOfficeUuid =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()).getId();

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
    fileNumberRepository.deleteAll().block();
    deviatingEcliRepository.deleteAll().block();
    previousDecisionRepository.deleteAll().block();
    stateRepository.deleteAll().block();
    deviatingDecisionDateRepository.deleteAll().block();
    incorrectCourtRepository.deleteAll().block();
    repository.deleteAll().block();
    databaseDocumentTypeRepository.deleteAll();
    documentUnitStatusRepository.deleteAll().block();
    databasePublishReportRepository.deleteAll().block();
  }

  // TODO: write a test for add a document type with a wrong shortcut

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

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    DocumentUnitDTO documentUnitDTO = list.get(0);
    assertThat(documentUnitDTO.getDocumentnumber()).startsWith("XXRE");
    assertThat(documentUnitDTO.isDateKnown()).isTrue();

    List<DocumentUnitStatusDTO> statusList =
        documentUnitStatusRepository.findAll().collectList().block();
    assertThat(statusList).hasSize(1);
    DocumentUnitStatusDTO status = statusList.get(0);
    assertThat(status.getPublicationStatus()).isEqualTo(UNPUBLISHED);
    assertThat(status.getDocumentUnitId()).isEqualTo(documentUnitDTO.getUuid());
    assertThat(status.getCreatedAt()).isEqualTo(documentUnitDTO.getCreationtimestamp());
  }

  @Test
  @Disabled
  void testForFileNumbersDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
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

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<FileNumberDTO> fileNumberEntries =
        fileNumberRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();
    assertThat(fileNumberEntries).hasSize(1);
    assertThat(fileNumberEntries.get(0).getFileNumber()).isEqualTo("AkteX");
  }

  @Test
  @Disabled
  void testForDeviatingEcliDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .documentationOfficeId(documentationOfficeUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingEclis(List.of("ecli123", "ecli456"))
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
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
              assertThat(response.getResponseBody().coreData().deviatingEclis().get(0))
                  .isEqualTo("ecli123");
              assertThat(response.getResponseBody().coreData().deviatingEclis().get(1))
                  .isEqualTo("ecli456");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<DeviatingEcliDTO> deviatingEclis =
        deviatingEcliRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();

    assertThat(deviatingEclis).hasSize(2);
    assertThat(deviatingEclis.get(0).getEcli()).isEqualTo("ecli123");
    assertThat(deviatingEclis.get(1).getEcli()).isEqualTo("ecli456");
  }

  @Test
  @Disabled
  void testForDeviatingDecisionDateDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();

    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .deviatingDecisionDates(
                        (List.of(
                            LocalDate.parse("2022-01-31T23:00:00Z"),
                            LocalDate.parse("2022-01-31T23:00:00Z"))))
                    .documentationOffice(docOffice)
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
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
              assertThat(response.getResponseBody().coreData().deviatingDecisionDates().get(0))
                  .isEqualTo("2022-01-31T23:00:00Z");
              assertThat(response.getResponseBody().coreData().deviatingDecisionDates().get(1))
                  .isEqualTo("2022-01-31T23:00:00Z");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<DeviatingDecisionDateDTO> deviatingDecisionDates =
        deviatingDecisionDateRepository
            .findAllByDocumentUnitId(list.get(0).getId())
            .collectList()
            .block();

    assertThat(deviatingDecisionDates).hasSize(2);
    assertThat(deviatingDecisionDates.get(0).decisionDate()).isEqualTo("2022-01-31T23:00:00Z");
    assertThat(deviatingDecisionDates.get(1).decisionDate()).isEqualTo("2022-01-31T23:00:00Z");
  }

  @Test
  @Disabled
  void testUpdate_withIncorrectCourts_shouldHaveIncorrectCourtsSavedInDB() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    IncorrectCourtDTO incorrectCourtDTO =
        IncorrectCourtDTO.builder()
            .documentUnitId(savedDto.getId())
            .court("incorrectCourt1")
            .build();
    incorrectCourtRepository.save(incorrectCourtDTO).block();
    incorrectCourtDTO =
        IncorrectCourtDTO.builder()
            .documentUnitId(savedDto.getId())
            .court("incorrectCourt2")
            .build();
    incorrectCourtRepository.save(incorrectCourtDTO).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .deviatingCourts(
                        List.of("incorrectCourt1", "incorrectCourt3", "incorrectCourt4"))
                    .documentationOffice(docOffice)
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
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
              assertThat(response.getResponseBody().coreData()).isNotNull();
              assertThat(response.getResponseBody().coreData().deviatingCourts()).hasSize(3);
              assertThat(response.getResponseBody().coreData().deviatingCourts())
                  .containsExactly("incorrectCourt1", "incorrectCourt3", "incorrectCourt4");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<IncorrectCourtDTO> incorrectCourtDTOs =
        incorrectCourtRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();

    assertThat(incorrectCourtDTOs).hasSize(3);
    assertThat(incorrectCourtDTOs)
        .extracting("court")
        .containsExactly("incorrectCourt1", "incorrectCourt3", "incorrectCourt4");
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

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();
    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
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

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentTypeId()).isEqualTo(documentTypeDTOR.getId());
    assertThat(list.get(0).getDocumentTypeDTO()).isNull();
  }

  @Test
  @Disabled
  void testUndoSettingDocumentType() {
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentTypeId(docType.getId())
            .documentationOfficeId(documentationOfficeUuid)
            .build();
    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .documentNumber(dto.getDocumentnumber())
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

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentTypeId()).isNull();
    assertThat(list.get(0).getDocumentTypeDTO()).isNull();
  }

  @Test
  @Disabled
  void testLegalEffectToBeSetFromNotSpecifiedToYesBySpecialCourtChangeButBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NOT_SPECIFIED, "BGH", LegalEffect.YES);
  }

  @Test
  @Disabled
  void testLegalEffectToBeSetFromNoToYesBySpecialCourtChangeButBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NO, "BVerfG", LegalEffect.YES);
  }

  @Test
  @Disabled
  void testLegalEffectToBeKeptAtYesBySpecialCourtChangeAndBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.YES, "BSG", LegalEffect.YES);
  }

  @Test
  @Disabled
  void testLegalEffectToBeKeptByNonSpecialCourtChangeAndBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NO, "ABC", LegalEffect.NO);
  }

  private void testLegalEffectChanges(
      LegalEffect valueBefore, String courtType, LegalEffect expectedValueAfter) {
    // outsource and reuse this default way of building a new DocumentUnitDTO? TODO
    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .legalEffect(valueBefore.getLabel())
            .documentationOfficeId(documentationOfficeUuid)
            .build();

    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, valueBefore);

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
              assertThat(response.getResponseBody().coreData().court().type()).isEqualTo(courtType);
              assertThat(response.getResponseBody().coreData().legalEffect())
                  .isEqualTo(expectedValueAfter.getLabel());
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getLegalEffect()).isEqualTo(expectedValueAfter.getLabel());

    // Change to NO, should stay NO
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, LegalEffect.NO));

    // Change to NOT_SPECIFIED, should stay NOT_SPECIFIED
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, LegalEffect.NOT_SPECIFIED));

    // Remove court, should stay NOT_SPECIFIED
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, null, LegalEffect.NOT_SPECIFIED));
  }

  private DocumentUnit buildDocumentUnitFromFrontendWithLegalEffect(
      DocumentUnitDTO dto, String courtType, LegalEffect legalEffect) {
    CoreData coreData;
    if (courtType == null) {
      coreData =
          CoreData.builder()
              .legalEffect(legalEffect.getLabel())
              .documentationOffice(docOffice)
              .build();
    } else {
      coreData =
          CoreData.builder()
              .court(Court.builder().type(courtType).build())
              .legalEffect(legalEffect.getLabel())
              .documentationOffice(docOffice)
              .build();
    }
    return DocumentUnit.builder()
        .uuid(dto.getUuid())
        .documentNumber(dto.getDocumentnumber())
        .coreData(coreData)
        .build();
  }

  private void testCorrectFEtoBECallBehaviourWithLegalEffect(
      DocumentUnit documentUnitFromFrontend) {
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
              if (documentUnitFromFrontend.coreData().court() != null) {
                assertThat(response.getResponseBody().coreData().court().type())
                    .isEqualTo(documentUnitFromFrontend.coreData().court().type());
              }
              assertThat(response.getResponseBody().coreData().legalEffect())
                  .isEqualTo(documentUnitFromFrontend.coreData().legalEffect());
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getLegalEffect())
        .isEqualTo(documentUnitFromFrontend.coreData().legalEffect());
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
                DocumentUnitDTO.builder()
                    .uuid(uuid)
                    .creationtimestamp(Instant.now())
                    .documentnumber(RandomStringUtils.random(10, true, true))
                    .documentationOfficeId(documentationOfficeUuid)
                    .build())
        .flatMap(documentUnitDTO -> repository.save(documentUnitDTO))
        .flatMap(
            documentUnitDTO ->
                documentUnitStatusRepository.save(
                    DocumentUnitStatusDTO.builder()
                        .newEntry(true)
                        .id(UUID.randomUUID())
                        .publicationStatus(published[random.nextInt(3)])
                        .documentUnitId(documentUnitDTO.getUuid())
                        .build()))
        .blockLast();
    assertThat(repository.findAll().collectList().block()).hasSize(20);

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
  void testDefaultStatus() {
    DocumentUnitDTO dto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .documentationOfficeId(documentationOfficeUuid)
                    .build())
            .block();

    assertThat(repository.findAll().collectList().block()).hasSize(1);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + dto.getDocumentnumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().status().publicationStatus())
                  .isEqualTo(PUBLISHED);
            });
  }

  @Test
  @Disabled
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
      DocumentUnitDTO dto =
          repository
              .save(
                  DocumentUnitDTO.builder()
                      .uuid(UUID.randomUUID())
                      .creationtimestamp(Instant.now())
                      .documentnumber(documentNumbers.get(i))
                      .courtType(courtTypes.get(i))
                      .courtLocation(courtLocations.get(i))
                      .decisionDate(decisionDates.get(i))
                      .documentationOfficeId(docOfficeIds.get(i))
                      .build())
              .block();

      documentUnitStatusRepository
          .save(
              DocumentUnitStatusDTO.builder()
                  .id(UUID.randomUUID())
                  .newEntry(true)
                  .documentUnitId(dto.getUuid())
                  .publicationStatus(statuses.get(i))
                  .withError(errorStatuses.get(i))
                  .build())
          .block();

      fileNumberRepository
          .save(
              FileNumberDTO.builder()
                  .documentUnitId(dto.getId())
                  .fileNumber(fileNumbers.get(i))
                  .isDeviating(false)
                  .build())
          .block();
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
