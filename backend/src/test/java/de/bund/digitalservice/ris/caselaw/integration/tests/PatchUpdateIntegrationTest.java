package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabasePatchMapperService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.ProcedureController;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRelatedDocumentationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedDocumentationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.FmxImportService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      OAuthService.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentationUnitStatusService.class,
      DatabasePatchMapperService.class,
      DatabaseProcedureService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class,
    },
    controllers = {DocumentationUnitController.class, ProcedureController.class})
@Slf4j
@SuppressWarnings("java:S5961")
class PatchUpdateIntegrationTest {
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
  @Autowired private DatabaseDocumentationUnitPatchRepository patchRepository;
  @Autowired private DatabaseCourtRepository courtRepository;
  @Autowired private DatabaseRegionRepository regionRepository;
  @Autowired private DatabaseRelatedDocumentationRepository relatedDocumentationRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcedureRepository procedureRepository;
  @Autowired private DatabaseUserGroupRepository userGroupRepository;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private DocumentationUnitHistoryLogService documentationUnitHistoryLogService;

  @MockitoBean private S3AsyncClient s3AsyncClient;
  @MockitoBean private MailService mailService;
  @MockitoBean DocxConverterService docxConverterService;
  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;

  @MockitoBean AttachmentService attachmentService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private DocumentationUnitDocxMetadataInitializationService initializationService;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private StagingPortalPublicationService stagingPortalPublicationService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private FmxImportService fmxImportService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;

  private UUID court1Id;
  private UUID court2Id;
  private UUID region1Id;

  @BeforeEach
  void setUp() {
    RegionDTO region1DTO =
        regionRepository.save(
            RegionDTO.builder()
                .code("NW")
                .longText("Nordrhein-Westfalen")
                .applicability(true)
                .build());
    region1Id = region1DTO.getId();
    RegionDTO region2DTO =
        regionRepository.save(
            RegionDTO.builder().code("HE").longText("Hessen").applicability(true).build());
    court1Id =
        courtRepository
            .save(
                CourtDTO.builder()
                    .region(region1DTO)
                    .type("LG")
                    .location("Detmold")
                    .isForeignCourt(false)
                    .isSuperiorCourt(false)
                    .jurisId(0)
                    .build())
            .getId();
    court2Id =
        courtRepository
            .save(
                CourtDTO.builder()
                    .region(region2DTO)
                    .type("SG")
                    .location("Fulda")
                    .isForeignCourt(false)
                    .isSuperiorCourt(false)
                    .jurisId(1)
                    .build())
            .getId();

    mockUserGroups(userGroupService);
  }

  @AfterEach
  void tearDown() {
    patchRepository.deleteAll();
    repository.deleteAll();
    courtRepository.deleteAll();
    regionRepository.deleteAll();
    userGroupRepository.deleteAll();
    procedureRepository.deleteAll();
    patchRepository.deleteAll();
    relatedDocumentationRepository.deleteAll();
  }

  @Test
  @Transactional
  void
      testPartialUpdateByUuid_withEmptyPatchAndNoPatchesInBackend_shouldNotChangeDocumentationUnit() {
    TestTransaction.flagForCommit();
    TestTransaction.end();

    DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

    RisJsonPatch patch =
        new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

    risWebTestClient
        .withDefaultLogin()
        .patch()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
        .bodyValue(patch)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(RisJsonPatch.class)
        .consumeWith(
            response -> {
              RisJsonPatch responsePatch = response.getResponseBody();
              assertThat(responsePatch).isNotNull();
              assertThat(responsePatch.documentationUnitVersion()).isZero();
              assertThat(responsePatch.patch().getOperations()).isEmpty();
              assertThat(responsePatch.errorPaths()).isEmpty();
            });

    TestTransaction.start();
    List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
    assertThat(allDocumentationUnits).hasSize(1);
    DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
    assertThat(documentationUnitDTO)
        .extracting("id", "documentNumber", "ecli")
        .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber(), null);
    assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
    TestTransaction.end();
  }

  @Test
  @Transactional
  void testPartialUpdateByUuid_withProcedurePatch_shouldWriteHistoryLogEntries() {
    TestTransaction.flagForCommit();
    TestTransaction.end();

    DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

    var procedure1AsNode =
        objectMapper.convertValue(Procedure.builder().label("Vorgang1").build(), JsonNode.class);
    List<JsonPatchOperation> operations1 =
        List.of(new AddOperation("/coreData/procedure", procedure1AsNode));
    RisJsonPatch patch1 = new RisJsonPatch(0L, new JsonPatch(operations1), Collections.emptyList());

    risWebTestClient
        .withDefaultLogin()
        .patch()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
        .bodyValue(patch1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    var user = User.builder().documentationOffice(buildDSDocOffice()).build();
    var logs = documentationUnitHistoryLogService.getHistoryLogs(documentationUnit.uuid(), user);
    assertThat(logs).hasSize(3);
    assertThat(logs)
        .map(HistoryLog::eventType)
        .containsExactly(
            HistoryLogEventType.UPDATE, HistoryLogEventType.PROCEDURE, HistoryLogEventType.CREATE);
    assertThat(logs).map(HistoryLog::createdBy).containsExactly("testUser", "testUser", "testUser");
    assertThat(logs).map(HistoryLog::documentationOffice).containsExactly("DS", "DS", "DS");
    assertThat(logs.get(0).description()).isEqualTo("Dokeinheit bearbeitet");
    assertThat(logs.get(1).description()).isEqualTo("Vorgang gesetzt: Vorgang1");
    assertThat(logs.get(2).description()).isEqualTo("Dokeinheit angelegt");

    var procedure2AsNode =
        objectMapper.convertValue(Procedure.builder().label("Vorgang2").build(), JsonNode.class);
    List<JsonPatchOperation> operations2 =
        List.of(new ReplaceOperation("/coreData/procedure", procedure2AsNode));
    RisJsonPatch patch2 = new RisJsonPatch(1L, new JsonPatch(operations2), Collections.emptyList());

    risWebTestClient
        .withDefaultLogin()
        .patch()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
        .bodyValue(patch2)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    // Existing update event will be updated
    var logs2 = documentationUnitHistoryLogService.getHistoryLogs(documentationUnit.uuid(), user);
    assertThat(logs2).hasSize(4);
    assertThat(logs2)
        .map(HistoryLog::eventType)
        .containsExactly(
            HistoryLogEventType.UPDATE,
            HistoryLogEventType.PROCEDURE,
            HistoryLogEventType.PROCEDURE,
            HistoryLogEventType.CREATE);
    assertThat(logs2)
        .map(HistoryLog::createdBy)
        .containsExactly("testUser", "testUser", "testUser", "testUser");
    assertThat(logs2).map(HistoryLog::documentationOffice).containsExactly("DS", "DS", "DS", "DS");
    assertThat(logs2.get(0).description()).isEqualTo("Dokeinheit bearbeitet");
    assertThat(logs2.get(1).description()).isEqualTo("Vorgang geändert: Vorgang1 → Vorgang2");
    assertThat(logs2.get(2).description()).isEqualTo("Vorgang gesetzt: Vorgang1");
    assertThat(logs2.get(3).description()).isEqualTo("Dokeinheit angelegt");
  }

  @Nested
  class SingleValueAdd {
    @Test
    @Transactional
    void testPartialUpdateByUuid_withUser1AddEcliAndUser2DoNothing_shouldSendPatchWithEcliToUser2()
        throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser1");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddEcliAndUser2AddFileNumber_shouldSendPatchWithEcliToUser2AndPatchWithFileNumberToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser1");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().textValue()).isEqualTo("fileNumberUser2");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddEcliAndUser2ChangeEcliToo_shouldSendPatchWithEcliAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/ecli");

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser1");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/ecli");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class SingleValueEdit {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2DoNothing_shouldSendPatchWithEcliToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DecisionDTO dto = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      dto.setEcli("initialEcli");
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new ReplaceOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation addOperation = (ReplaceOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser1");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2AddFileNumber_shouldSendPatchWithEcliToUser2AndPatchWithFileNumberToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DecisionDTO dto = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      dto.setEcli("initialEcli");
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new ReplaceOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation addOperation = (ReplaceOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser1");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().textValue()).isEqualTo("fileNumberUser2");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2ChangeEcliToo_shouldSendPatchWithEcliAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DecisionDTO dto = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      dto.setEcli("initialEcli");
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new ReplaceOperation("/coreData/ecli", new TextNode("ecliUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new ReplaceOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("testUser");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("DS");

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("ecliUser1");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/ecli");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser1");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "replace", "path", "/coreData/ecli", "value", "ecliUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class ListValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2DoNothing_shouldSendPatchWithFileNumberToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().textValue()).isEqualTo("fileNumberUser1");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2AddEcli_shouldSendPatchWithFileNumberToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2AddFileNumberToo_shouldSendPatchWithFileNumberAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser1")));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/fileNumbers/0", new TextNode("fileNumberUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().textValue()).isEqualTo("fileNumberUser1");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/fileNumbers/0");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "add", "path", "/coreData/fileNumbers/0", "value", "fileNumberUser1"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();
    }
  }

  @Nested
  class ListValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumberAndUser2DoNothing_shouldSendPatchWithRemoveFileNumberToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers().add(FileNumberDTO.builder().value("initialFileNumber").build());
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/coreData/fileNumbers/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();
                assertThat(responsePatch.errorPaths()).isEmpty();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumberAndUser2AddEcli_shouldSendPatchWithRemoveFileNumberToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers().add(FileNumberDTO.builder().value("initialFileNumber").build());
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/coreData/fileNumbers/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumberAndUser2RemoveFileNumberToo_shouldSendPatchWithRemoveFileNumberAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers().add(FileNumberDTO.builder().value("initialFileNumber").build());
      repository.save(dto);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/coreData/fileNumbers/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new RemoveOperation("/coreData/fileNumbers/0"));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().isNull()).isTrue();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/fileNumbers/0");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/coreData/fileNumbers/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class DropDownValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1SelectCourtAndUser2DoNothing_shouldSendPatchWithCourtAndRegionToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode courtAsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
                  .id(court1Id)
                  .jurisdictionType("")
                  .region("NW")
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/court", courtAsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertCourt(patches);

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(courtAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();
                assertThat(responsePatch.errorPaths()).isEmpty();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertCourt(patches);

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddCourtAndUser2AddEcli_shouldSendPatchWithCourtAndRegionToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode courtAsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
                  .jurisdictionType("")
                  .region("NW")
                  .id(court1Id)
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/court", courtAsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertCourt(patches);
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(courtAsNode);

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertCourt(patches);

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertCourt(patches);

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddCourtAndUser2AddCourtToo_shouldSendPatchWithCourtAndRegionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode court1AsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
                  .jurisdictionType("")
                  .region("NW")
                  .id(court1Id)
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/coreData/court", court1AsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      DocumentationUnitDTO documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertCourt(patches);
      TestTransaction.end();

      JsonNode court2AsNode =
          objectMapper.convertValue(
              Court.builder().type("AG").location("Hamm").label("AG Hamm").id(court2Id).build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/court", court2AsNode));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(court1AsNode);

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotNull();

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/court");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code")
          .containsExactly(Tuple.tuple(region1Id, "NW"));

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertCourt(patches);
      TestTransaction.end();
    }
  }

  @Nested
  class DropDownValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1SelectCourtAndUser2DoNothing_shouldSendPatchWithCourtAndRegionToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      documentationUnitDTO.setCourt(courtRepository.findById(court1Id).get());
      documentationUnitDTO.getRegions().add(regionRepository.findById(region1Id).get());
      repository.save(documentationUnitDTO);
      TestTransaction.flagForCommit();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 = List.of(new RemoveOperation("/coreData/court"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation addOperation = (RemoveOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");

                //                operation = responsePatch.patch().getOperations().get(1);
                //                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                //                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                //
                // assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                //                assertThat(replaceOperation.getValue().textValue()).isNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2AddEcli_shouldSendPatchWithRemoveCourtAndRegionToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      documentationUnitDTO.setCourt(courtRepository.findById(court1Id).get());
      documentationUnitDTO.getRegions().add(regionRepository.findById(region1Id).get());
      repository.save(documentationUnitDTO);
      TestTransaction.flagForCommit();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 = List.of(new RemoveOperation("/coreData/court"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation addOperation = (RemoveOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber", "ecli")
          .containsExactly(
              documentationUnit.uuid(), documentationUnit.documentNumber(), "ecliUser2");
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2RemoveCourtToo_shouldSendPatchWithRemoveCourtAndRegionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      documentationUnitDTO.setCourt(courtRepository.findById(court1Id).get());
      documentationUnitDTO.getRegions().add(regionRepository.findById(region1Id).get());
      repository.save(documentationUnitDTO);
      TestTransaction.flagForCommit();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 = List.of(new RemoveOperation("/coreData/court"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 = List.of(new RemoveOperation("/coreData/court"));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                // next iteration: handle add operation with null values
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/court");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2ReplaceCourt_shouldSendPatchWithRemoveCourtAndRegionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      documentationUnitDTO.setCourt(courtRepository.findById(court1Id).get());
      documentationUnitDTO.getRegions().add(regionRepository.findById(region1Id).get());
      repository.save(documentationUnitDTO);
      TestTransaction.flagForCommit();
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 = List.of(new RemoveOperation("/coreData/court"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);
                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      List<DocumentationUnitDTO> allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();

      JsonNode court2AsNode = objectMapper.convertValue(Court.builder().build(), JsonNode.class);
      List<JsonPatchOperation> operationsUser2 =
          List.of(
              new RemoveOperation("/coreData/court"),
              new AddOperation("/coreData/court", court2AsNode));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(6);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                // next iteration: handle two same operation (unique)
                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                assertThat(responsePatch.errorPaths()).containsExactly("/coreData/court");
              });

      TestTransaction.start();
      allDocumentationUnits = repository.findAll();
      assertThat(allDocumentationUnits).hasSize(1);
      documentationUnitDTO = allDocumentationUnits.get(0);
      assertThat(documentationUnitDTO)
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getCourt()).isNull();
      assertThat(documentationUnitDTO.getRegions()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertOnSavedPatchEntry(
          patches.get(0).getPatch(),
          Map.of("op", "remove", "path", "/coreData/court"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }
  }

  @Nested
  class EditableListValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviousDecisionAndUser2DoNothing_shouldSendPatchWithPreviousDecisionToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision = generateEmptyDocumentationUnit();

      JsonNode previousDecisionAsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .documentNumber(previousDecision.documentNumber())
                  .status(previousDecision.status())
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/previousDecisions/0", previousDecisionAsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision.documentNumber());
      documentationUnitDTO = repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      RelatedDocumentationDTO relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision.documentNumber());

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertPreviousDecision(patches, relatedDocument);

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(6);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(5);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision.documentNumber());
      documentationUnitDTO = repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision.documentNumber());

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertPreviousDecision(patches, relatedDocument);

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviousDecisionAndUser2AddEcli_shouldSendPatchWithPreviousDecisionToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision = generateEmptyDocumentationUnit();

      JsonNode previousDecisionAsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .documentNumber(previousDecision.documentNumber())
                  .status(previousDecision.status())
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/previousDecisions/0", previousDecisionAsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DecisionDTO documentationUnitDTO =
          (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isNull();
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision.documentNumber());
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      RelatedDocumentationDTO relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision.documentNumber());

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertPreviousDecision(patches, relatedDocument);

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(7);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(5);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(6);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision.documentNumber());
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision.documentNumber());

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertPreviousDecision(patches, relatedDocument);
      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(8);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(5);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(6);
                assertThat(operation).isInstanceOf(AddOperation.class);
                addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                operation = responsePatch.patch().getOperations().get(7);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision.documentNumber());
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision.documentNumber());

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertPreviousDecision(patches, relatedDocument);
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviousDecisionAndUser2AddPreviousDecisionToo_shouldSendPatchWithPreviousDecisionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision1 = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision2 = generateEmptyDocumentationUnit();

      JsonNode previousDecision1AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .documentNumber(previousDecision1.documentNumber())
                  .status(previousDecision1.status())
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser1 =
          List.of(new AddOperation("/previousDecisions/0", previousDecision1AsNode));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(3);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision1.documentNumber());
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision2.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision2.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      RelatedDocumentationDTO relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision1.documentNumber());

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertPreviousDecision(patches, relatedDocument);

      TestTransaction.end();

      JsonNode previousDecision2AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .documentNumber(previousDecision2.documentNumber())
                  .status(previousDecision2.status())
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/previousDecisions/0", previousDecision2AsNode));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(7);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByName");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecision1AsNode);

                operation = responsePatch.patch().getOperations().get(4);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(5);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedAtDateTime");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(6);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath())
                    .isEqualTo("/managementData/lastUpdatedByDocOffice");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                assertThat(responsePatch.errorPaths()).containsExactly("/previousDecisions/0");
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(3);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions())
          .extracting("documentNumber")
          .containsExactly(previousDecision1.documentNumber());
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision2.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision2.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).hasSize(1);
      relatedDocument = allRelatedDocuments.get(0);
      assertThat(relatedDocument.getDocumentNumber()).isEqualTo(previousDecision1.documentNumber());

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertPreviousDecision(patches, relatedDocument);

      TestTransaction.end();
    }
  }

  @Nested
  class EditableListValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2DoNothing_shouldSendPatchWithRemovePreviousDecisionToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision = generateEmptyDocumentationUnit();

      TestTransaction.start();
      TestTransaction.flagForCommit();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getPreviousDecisions()
          .add(
              PreviousDecisionDTO.builder()
                  .documentNumber(previousDecision.documentNumber())
                  .rank(1)
                  .build());
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/previousDecisions/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches).hasSize(1);
      assertThat(patches.getFirst().getDocumentationUnitVersion()).isZero();
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation addOperation = (RemoveOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches).hasSize(1);
      assertThat(patches.getFirst().getDocumentationUnitVersion()).isZero();
      String savedPatchJson = patches.getFirst().getPatch();
      assertOnSavedPatchEntry(
          savedPatchJson,
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2AddEcli_shouldSendPatchWithRemovePreviousDecisionToUser2AndPatchWithEcliToUser1()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision = generateEmptyDocumentationUnit();

      TestTransaction.start();
      TestTransaction.flagForCommit();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getPreviousDecisions()
          .add(
              PreviousDecisionDTO.builder()
                  .documentNumber(previousDecision.documentNumber())
                  .rank(1)
                  .build());
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/previousDecisions/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DecisionDTO documentationUnitDTO =
          (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isNull();
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/coreData/ecli", new TextNode("ecliUser2")));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L, 1L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();

      RisJsonPatch emptyPatchUser1 =
          new RisJsonPatch(1L, new JsonPatch(Collections.emptyList()), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(emptyPatchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(2L);
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = (DecisionDTO) repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = (DecisionDTO) repository.findById(previousDecision.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L, 1L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      assertOnSavedPatchEntry(
          patches.get(1).getPatch(),
          Map.of("op", "add", "path", "/coreData/ecli", "value", "ecliUser2"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"));

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2RemovePreviousDecisionToo_shouldSendPatchWithRemovePreviousDecisionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision1 = generateEmptyDocumentationUnit();

      TestTransaction.start();
      TestTransaction.flagForCommit();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getPreviousDecisions()
          .add(
              PreviousDecisionDTO.builder()
                  .documentNumber(previousDecision1.documentNumber())
                  .rank(1)
                  .build());
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/previousDecisions/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      List<JsonPatchOperation> operationsUser2 =
          List.of(new RemoveOperation("/previousDecisions/0"));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                assertThat(responsePatch.errorPaths()).containsExactly("/previousDecisions/0");
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);

      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2AddOtherPreviousDecision_shouldSendPatchWithRemovePreviousDecisionAndErrorPathToUser2()
            throws JsonProcessingException {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentationUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision1 = generateEmptyDocumentationUnit();
      DocumentationUnit previousDecision2 = generateEmptyDocumentationUnit();

      TestTransaction.start();
      TestTransaction.flagForCommit();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getPreviousDecisions()
          .add(
              PreviousDecisionDTO.builder()
                  .documentNumber(previousDecision1.documentNumber())
                  .rank(1)
                  .build());
      TestTransaction.end();

      List<JsonPatchOperation> operationsUser1 =
          List.of(new RemoveOperation("/previousDecisions/0"));
      RisJsonPatch patchUser1 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser1), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser1)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(3);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision2.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision2.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      List<RelatedDocumentationDTO> allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      List<DocumentationUnitPatchDTO> patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);

      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));

      TestTransaction.end();

      JsonNode previousDecision2AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .documentNumber(previousDecision2.documentNumber())
                  .status(previousDecision2.status())
                  .build(),
              JsonNode.class);
      List<JsonPatchOperation> operationsUser2 =
          List.of(new AddOperation("/previousDecisions/0", previousDecision2AsNode));
      RisJsonPatch patchUser2 =
          new RisJsonPatch(0L, new JsonPatch(operationsUser2), Collections.emptyList());

      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnit.uuid())
          .bodyValue(patchUser2)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class)
          .consumeWith(
              response -> {
                RisJsonPatch responsePatch = response.getResponseBody();
                assertThat(responsePatch).isNotNull();
                assertThat(responsePatch.documentationUnitVersion()).isEqualTo(1L);
                assertThat(responsePatch.patch().getOperations()).hasSize(5);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                assertThat(responsePatch.errorPaths()).containsExactly("/previousDecisions/0");
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(3);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision1.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision1.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();
      documentationUnitDTO = repository.findById(previousDecision2.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(previousDecision2.documentNumber());
      assertThat(documentationUnitDTO.getPreviousDecisions()).isEmpty();

      allRelatedDocuments = relatedDocumentationRepository.findAll();
      assertThat(allRelatedDocuments).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .map(DocumentationUnitPatchDTO::getDocumentationUnitVersion)
          .containsExactly(0L);
      assertOnSavedPatchEntry(
          patches.getFirst().getPatch(),
          Map.of("op", "remove", "path", "/previousDecisions/0"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedByDocOffice", "value", "DS"),
          Map.of("op", "replace", "path", "/managementData/lastUpdatedAtDateTime"),
          Map.of(
              "op", "replace", "path", "/managementData/lastUpdatedByName", "value", "testUser"));
      TestTransaction.end();
    }
  }

  /** The patches will always include entries for lastUpdated fields of managementData. */
  @SafeVarargs
  private void assertOnSavedPatchEntry(
      String savedPatchJson, Map<String, String>... expectedPatchEntries)
      throws JsonProcessingException {
    List<Map<String, String>> parsedPatches;
    parsedPatches = objectMapper.readValue(savedPatchJson, new TypeReference<>() {});
    assertThat(parsedPatches).hasSize(expectedPatchEntries.length);
    for (int i = 0; i < expectedPatchEntries.length; i++) {
      assertThat(parsedPatches.get(i)).containsAllEntriesOf(expectedPatchEntries[i]);
    }
  }

  private void assertPatchJsonContains(String patchJson, String... expectedFragments)
      throws JsonProcessingException {
    JsonNode actualArray = objectMapper.readTree(patchJson);

    for (String expected : expectedFragments) {
      JsonNode expectedNode = objectMapper.readTree(expected);
      boolean matchFound = false;

      for (JsonNode actualNode : actualArray) {
        if (actualNode.equals(expectedNode)) {
          matchFound = true;
          break;
        }
      }

      if (!matchFound) {
        fail("Expected patch fragment not found:\n" + expectedNode.toPrettyString());
      }
    }
  }

  private void assertPreviousDecision(
      List<DocumentationUnitPatchDTO> patches, RelatedDocumentationDTO relatedDocument)
      throws JsonProcessingException {
    assertPatchJsonContains(
        patches.get(0).getPatch(),
        """
        {
          "op": "add",
          "path": "/previousDecisions/0",
          "value": {
            "uuid": null,
            "newEntry": false,
            "documentNumber": "%s",
            "status": {
              "publicationStatus": "UNPUBLISHED",
              "withError": false,
              "createdAt": null
            },
            "court": null,
            "decisionDate": null,
            "fileNumber": null,
            "documentType": null,
            "createdByReference": null,
            "documentationOffice": null,
            "creatingDocOffice": null,
            "hasPreviewAccess": false,
            "dateKnown": true,
            "deviatingFileNumber": null
          }
        }
        """
            .formatted(relatedDocument.getDocumentNumber()),
        """
        {
          "op": "replace",
          "path": "/managementData/lastUpdatedByDocOffice",
          "value": "DS"
        }
        """,
        """
        {
          "op": "replace",
          "path": "/managementData/lastUpdatedByName",
          "value": "testUser"
        }
        """,
        """
        {
          "op": "replace",
          "path": "/previousDecisions/0/uuid",
          "value": "%s"
        }
        """
            .formatted(relatedDocument.getId()),
        """
        {
          "op": "replace",
          "path": "/previousDecisions/0/status",
          "value": null
        }
        """);
  }

  private void assertCourt(List<DocumentationUnitPatchDTO> patches) throws JsonProcessingException {
    assertPatchJsonContains(
        patches.get(0).getPatch(),
        """
        {
          "op": "add",
          "path": "/coreData/court",
          "value": {
            "id": "%s",
            "type": "LG",
            "location": "Detmold",
            "label": "LG Detmold",
            "revoked": null,
            "jurisdictionType": "",
            "region": "NW",
            "responsibleDocOffice": null
          }
        }
        """
            .formatted(court1Id),
        """
        {
          "op": "replace",
          "path": "/managementData/lastUpdatedByDocOffice",
          "value": "DS"
        }
        """,
        """
        {
          "op": "replace",
          "path": "/managementData/lastUpdatedByName",
          "value": "testUser"
        }
        """);
  }

  @Nested
  @Sql(
      scripts = {
        "classpath:doc_office_init.sql",
        "classpath:user_group_init.sql",
        "classpath:procedures_init.sql",
      })
  class AuthorizationForExternalUsers {
    @Test
    @Transactional
    void
        test_partialUpdateByUuid_withAssignedExternalUserAndAllowedOperations_shouldApplyOperations() {
      // Arrange
      TestTransaction.flagForCommit();
      TestTransaction.end();
      DecisionDTO docUnitDTO = (DecisionDTO) repository.findByDocumentNumber("1234567890123").get();
      docUnitDTO.setHeadline("oldHeadline");
      docUnitDTO.setGuidingPrinciple("guidingPrinciple");
      repository.save(docUnitDTO);
      assignProcedure(docUnitDTO);

      // Act
      risWebTestClient
          .withExternalLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
          .bodyValue(RisJsonPatch.builder().patch(new JsonPatch(getAllowedOperations())).build())
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(RisJsonPatch.class);

      TestTransaction.start();
      DecisionDTO result = (DecisionDTO) repository.findById(docUnitDTO.getId()).get();

      // Assert
      assertThat(result.getDecisionNames().get(0).getValue()).isEqualTo("decisionName");
      assertThat(result.getHeadline()).isEqualTo("newHeadline");
      assertThat(result.getGuidingPrinciple()).isNull();
      TestTransaction.end();
    }

    @Test
    void
        test_partialUpdateByUuid_withAssignedExternalUserAndAtLeastOneProhibitedOperation_shouldBeForbidden() {
      // Arrange
      DocumentationUnitDTO docUnitDTO = repository.findByDocumentNumber("1234567890123").get();
      assignProcedure(docUnitDTO);

      // Act
      risWebTestClient
          .withExternalLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
          .bodyValue(
              RisJsonPatch.builder()
                  .patch(new JsonPatch(getAtLeastOneProhibitedOperation()))
                  .build())
          .exchange()
          .expectStatus()
          // Assert
          .isForbidden();
    }

    @Test
    void
        test_partialUpdateByUuid_withUnassignedExternalUserAndAllowedOperations_shouldBeForbidden() {
      // Arrange
      DocumentationUnitDTO docUnitDTO = repository.findByDocumentNumber("1234567890123").get();

      // Act
      risWebTestClient
          .withExternalLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
          .bodyValue(RisJsonPatch.builder().patch(new JsonPatch(getAllowedOperations())).build())
          .exchange()
          .expectStatus()
          // Assert
          .isForbidden();
    }

    private void assignProcedure(DocumentationUnitDTO docUnitDTO) {
      DocumentationOffice documentationOffice =
          DocumentationOfficeTransformer.transformToDomain(docUnitDTO.getDocumentationOffice());
      UUID procedureId = addProcedureToDocUnit(docUnitDTO, documentationOffice);
      UUID userGroupId = userGroupRepository.findAll().get(0).getId();

      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + userGroupId)
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @NotNull
    private static List<JsonPatchOperation> getAllowedOperations() {
      JsonNode decisionName = new TextNode("decisionName");
      JsonNode newHeadline = new TextNode("newHeadline");
      String firstAllowedPath = "/shortTexts/decisionName";
      String secondAllowedPath = "/shortTexts/headline";
      String thirdAllowedPath = "/shortTexts/guidingPrinciple";
      return List.of(
          new AddOperation(firstAllowedPath, decisionName),
          new ReplaceOperation(secondAllowedPath, newHeadline),
          new RemoveOperation(thirdAllowedPath));
    }

    @NotNull
    private static List<JsonPatchOperation> getAtLeastOneProhibitedOperation() {
      JsonNode decisionName = new TextNode("decisionName");
      JsonNode newHeadline = new TextNode("newHeadline");
      String firstAllowedPath = "/shortTexts/decisionName";
      String secondAllowedPath = "/shortTexts/headline";
      String prohibitedPath = "/coreData/court";
      return List.of(
          new AddOperation(firstAllowedPath, decisionName),
          new ReplaceOperation(secondAllowedPath, newHeadline),
          new RemoveOperation(prohibitedPath));
    }

    private UUID addProcedureToDocUnit(
        DocumentationUnitDTO documentationUnitDTO, DocumentationOffice docOffice) {
      DocumentationUnit documentationUnitFromFrontend =
          DocumentationUnit.builder()
              .uuid(documentationUnitDTO.getId())
              .documentNumber(documentationUnitDTO.getDocumentNumber())
              .coreData(
                  CoreData.builder()
                      .procedure(Procedure.builder().label("procedure1").build())
                      .documentationOffice(docOffice)
                      .build())
              .build();

      AtomicReference<UUID> procedureId = new AtomicReference<>();
      risWebTestClient
          .withDefaultLogin()
          .put()
          .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
          .bodyValue(documentationUnitFromFrontend)
          .exchange()
          .expectStatus()
          .is2xxSuccessful()
          .expectBody(DocumentationUnit.class)
          .consumeWith(
              response -> {
                CoreData coreData = response.getResponseBody().coreData();
                assertThat(coreData.procedure().label()).isEqualTo("procedure1");
                procedureId.set(coreData.procedure().id());
              });

      return procedureId.get();
    }
  }

  private DocumentationUnit generateEmptyDocumentationUnit() {
    RisEntityExchangeResult<DocumentationUnit> result =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/new")
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DocumentationUnit.class)
            .returnResult();

    assertThat(result.getResponseBody()).isNotNull();

    return result.getResponseBody();
  }
}
