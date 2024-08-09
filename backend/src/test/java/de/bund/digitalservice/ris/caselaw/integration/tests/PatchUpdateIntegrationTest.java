package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabasePatchMapperService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRelatedDocumentationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedDocumentationDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      AuthService.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabasePatchMapperService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
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
  @Autowired private ObjectMapper objectMapper;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private MailService mailService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private UserService userService;

  @MockBean AttachmentService attachmentService;
  @MockBean private HandoverService handoverService;
  @MockBean private DocumentationUnitDocxMetadataInitializationService initializationService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private UUID court1Id;
  private UUID court2Id;
  private UUID region1Id;

  @BeforeEach
  void setUp() {
    DocumentationOfficeDTO documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

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

    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @AfterEach
  void tearDown() {
    patchRepository.deleteAll();
    repository.deleteAll();
    courtRepository.deleteAll();
    regionRepository.deleteAll();
  }

  @Test
  @Transactional
  void
      testPartialUpdateByUuid_withEmptyPatchAndNoPatchesInBackend_shouldNotChangeDocumentationUnit() {
    TestTransaction.flagForCommit();
    TestTransaction.end();

    DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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

  @Nested
  class SingleValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddEcliAndUser2DoNothing_shouldSendPatchWithEcliToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddEcliAndUser2AddFileNumber_shouldSendPatchWithEcliToUser2AndPatchWithFileNumberToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"),
              Tuple.tuple(
                  1L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"),
              Tuple.tuple(
                  1L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddEcliAndUser2ChangeEcliToo_shouldSendPatchWithEcliAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/ecli");

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class SingleValueEdit {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2DoNothing_shouldSendPatchWithEcliToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2AddFileNumber_shouldSendPatchWithEcliToUser2AndPatchWithFileNumberToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser2");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"),
              Tuple.tuple(
                  1L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L, "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"),
              Tuple.tuple(
                  1L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1ChangeEcliAndUser2ChangeEcliToo_shouldSendPatchWithEcliAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"replace\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser1\"}]"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class ListValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2DoNothing_shouldSendPatchWithFileNumberToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2AddEcli_shouldSendPatchWithFileNumberToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(documentationUnitDTO.getFileNumbers())
          .extracting("value")
          .containsExactly("fileNumberUser1");

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddFileNumberAndUser2AddFileNumberToo_shouldSendPatchWithFileNumberAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/fileNumbers/0\",\"value\":\"fileNumberUser1\"}]"));
      TestTransaction.end();
    }
  }

  @Nested
  class ListValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumnerAndUser2DoNothing_shouldSendPatchWithRemoveFileNumberToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers()
          .add(FileNumberDTO.builder().value("initialFileNumber").documentationUnit(dto).build());
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
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
          .extracting("id", "documentNumber")
          .containsExactly(documentationUnit.uuid(), documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumberAndUser2AddEcli_shouldSendPatchWithRemoveFileNumberToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers()
          .add(FileNumberDTO.builder().value("initialFileNumber").documentationUnit(dto).build());
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveFileNumberAndUser2RemoveFileNumberToo_shouldSendPatchWithRemoveFileNumberAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      TestTransaction.start();
      DocumentationUnitDTO dto = repository.findById(documentationUnit.uuid()).get();
      dto.getFileNumbers()
          .add(FileNumberDTO.builder().value("initialFileNumber").documentationUnit(dto).build());
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
                assertThat(responsePatch.patch().getOperations()).isEmpty();
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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/fileNumbers/0");
                assertThat(addOperation.getValue().isNull()).isTrue();

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/coreData/fileNumbers/0\"}]"));
      assertThat(documentationUnitDTO.getFileNumbers()).isEmpty();
      TestTransaction.end();
    }
  }

  @Nested
  class DropDownValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1SelectCourtAndUser2DoNothing_shouldSendPatchWithCourtAndRegionToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode courtAsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(courtAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

                assertThat(responsePatch.errorPaths()).isEmpty();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddCourtAndUser2AddEcli_shouldSendPatchWithCourtAndRegionToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode courtAsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(courtAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

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
      assertThat(documentationUnitDTO.getCourt())
          .extracting("id", "type", "location")
          .containsExactly(court1Id, "LG", "Detmold");
      assertThat(documentationUnitDTO.getRegions())
          .extracting("id", "code", "longText")
          .containsExactly(Tuple.tuple(region1Id, "NW", "Nordrhein-Westfalen"));

      patches =
          patchRepository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
              documentationUnit.uuid(), 0L);
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddCourtAndUser2AddCourtToo_shouldSendPatchWithCourtAndRegionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

      JsonNode court1AsNode =
          objectMapper.convertValue(
              Court.builder()
                  .type("LG")
                  .location("Detmold")
                  .label("LG Detmold")
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue()).isEqualTo(court1AsNode);

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isEqualTo("NW");

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/coreData/court\",\"value\":"
                      + "{\"id\":\""
                      + court1Id
                      + "\",\"type\":\"LG\","
                      + "\"location\":\"Detmold\",\"label\":\"LG Detmold\",\"revoked\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":\"NW\"}]"));
      TestTransaction.end();
    }
  }

  @Nested
  class DropDownValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1SelectCourtAndUser2DoNothing_shouldSendPatchWithCourtAndRegionToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation addOperation = (RemoveOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2AddEcli_shouldSendPatchWithRemoveCourtAndRegionToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation addOperation = (RemoveOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2RemoveCourtToo_shouldSendPatchWithRemoveCourtAndRegionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                // next iteration: handle add operation with null values
                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemoveCourtAndUser2ReplaceCourt_shouldSendPatchWithRemoveCourtAndRegionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                assertThat(responsePatch.patch().getOperations().get(0))
                    .isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation =
                    (ReplaceOperation) responsePatch.patch().getOperations().get(0);
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/court");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                // next iteration: handle two same operation (unique)
                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/coreData/court");

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/coreData/region");
                assertThat(replaceOperation.getValue().textValue()).isNull();

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"remove\",\"path\":\"/coreData/court\"},"
                      + "{\"op\":\"replace\",\"path\":\"/coreData/region\",\"value\":null}]"));
      TestTransaction.end();
    }
  }

  @Nested
  class EditableListValueAdd {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviousDecisionAndUser2DoNothing_shouldSendPatchWithPreviousDecisionToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision = generateEmptyDocumentationUnit();

      JsonNode previousDecisionAsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .referenceFound(true)
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(2);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviousDecisionAndUser2AddEcli_shouldSendPatchWithPreviousDecisionToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision = generateEmptyDocumentationUnit();

      JsonNode previousDecisionAsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .referenceFound(true)
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(documentationUnitDTO.getEcli()).isNull();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(3);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecisionAsNode);

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(AddOperation.class);
                addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1AddPreviosDecisionAndUser2AddPreviousDecisionToo_shouldSendPatchWithPreviousDecisionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision1 = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision2 = generateEmptyDocumentationUnit();

      JsonNode previousDecision1AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .referenceFound(true)
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
                assertThat(replaceOperation.getValue().textValue()).isNotBlank();

                operation = responsePatch.patch().getOperations().get(1);
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"));
      TestTransaction.end();

      JsonNode previousDecision2AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .referenceFound(true)
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
                assertThat(responsePatch.patch().getOperations()).hasSize(4);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                ReplaceOperation replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/status");
                assertThat(replaceOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                operation = responsePatch.patch().getOperations().get(2);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue()).isEqualTo(previousDecision1AsNode);

                operation = responsePatch.patch().getOperations().get(3);
                assertThat(operation).isInstanceOf(ReplaceOperation.class);
                replaceOperation = (ReplaceOperation) operation;
                assertThat(replaceOperation.getPath()).isEqualTo("/previousDecisions/0/uuid");
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(
                  0L,
                  "[{\"op\":\"add\",\"path\":\"/previousDecisions/0\","
                      + "\"value\":{\"uuid\":null,\"documentNumber\":\""
                      + relatedDocument.getDocumentNumber()
                      + "\","
                      + "\"status\":{\"publicationStatus\":\"UNPUBLISHED\",\"withError\":false,"
                      + "\"createdAt\":null},\"court\":null,\"decisionDate\":null,"
                      + "\"fileNumber\":null,\"documentType\":null,\"referenceFound\":true,"
                      + "\"dateKnown\":true,\"deviatingFileNumber\":null}},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/uuid\","
                      + "\"value\":\""
                      + relatedDocument.getId()
                      + "\"},"
                      + "{\"op\":\"replace\",\"path\":\"/previousDecisions/0/status\","
                      + "\"value\":null}]"));
      TestTransaction.end();
    }
  }

  @Nested
  class EditableListValueRemove {
    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2DoNothing_shouldSendPatchWithRemovePreviousDecisionToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2AddEcli_shouldSendPatchWithRemovePreviousDecisionToUser2AndPatchWithEcliToUser1() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isNull();
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(1);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/coreData/ecli");
                assertThat(addOperation.getValue().textValue()).isEqualTo("ecliUser2");

                assertThat(responsePatch.errorPaths()).isEmpty();
              });

      TestTransaction.start();
      assertThat(repository.findAll()).hasSize(2);
      documentationUnitDTO = repository.findById(documentationUnit.uuid()).get();
      assertThat(documentationUnitDTO.getDocumentNumber())
          .isEqualTo(documentationUnit.documentNumber());
      assertThat(documentationUnitDTO.getEcli()).isEqualTo("ecliUser2");
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
      assertThat(patches)
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"),
              Tuple.tuple(
                  1L, "[{\"op\":\"add\",\"path\":\"/coreData/ecli\",\"value\":\"ecliUser2\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2RemovePreviousDecisionToo_shouldSendPatchWithRemovePreviousDecisionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision1 = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(AddOperation.class);
                AddOperation addOperation = (AddOperation) operation;
                assertThat(addOperation.getPath()).isEqualTo("/previousDecisions/0");
                assertThat(addOperation.getValue().textValue()).isNull();

                operation = responsePatch.patch().getOperations().get(1);
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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
      TestTransaction.end();
    }

    @Test
    @Transactional
    void
        testPartialUpdateByUuid_withUser1RemovePreviousDecisionAndUser2AddOtherPreviousDecision_shouldSendPatchWithRemovePreviousDecisionAndErrorPathToUser2() {
      TestTransaction.flagForCommit();
      TestTransaction.end();

      DocumentUnit documentationUnit = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision1 = generateEmptyDocumentationUnit();
      DocumentUnit previousDecision2 = generateEmptyDocumentationUnit();

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
                assertThat(responsePatch.patch().getOperations()).isEmpty();

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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
      TestTransaction.end();

      JsonNode previousDecision2AsNode =
          objectMapper.convertValue(
              PreviousDecision.builder()
                  .referenceFound(true)
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
                assertThat(responsePatch.patch().getOperations()).hasSize(2);

                JsonPatchOperation operation = responsePatch.patch().getOperations().get(0);
                assertThat(operation).isInstanceOf(RemoveOperation.class);
                RemoveOperation removeOperation = (RemoveOperation) operation;
                assertThat(removeOperation.getPath()).isEqualTo("/previousDecisions/0");

                operation = responsePatch.patch().getOperations().get(1);
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
          .extracting("documentationUnitVersion", "patch")
          .containsExactly(
              Tuple.tuple(0L, "[{\"op\":\"remove\",\"path\":\"/previousDecisions/0\"}]"));
      TestTransaction.end();
    }
  }

  private DocumentUnit generateEmptyDocumentationUnit() {
    RisEntityExchangeResult<DocumentUnit> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/new")
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DocumentUnit.class)
            .returnResult();

    assertThat(result.getResponseBody()).isNotNull();

    return result.getResponseBody();
  }
}