package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
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
@Sql(scripts = {"classpath:ensuing_decisions_init.sql"})
@Sql(
    scripts = {"classpath:ensuing_decisions_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class EnsuingDecisionsIntegrationTest {
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

  private DocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");

    doReturn(Mono.just(DocumentationOfficeTransformer.transformToDomain(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @Test
  void testGetDocumentUnit_withoutEnsuingDecisions_shouldReturnEmptyList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).isEmpty());
  }

  @Test
  void testGetDocumentUnit_withEnsuingDecisions_shouldReturnList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions().size()).isEqualTo(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending())
                  .isEqualTo(false);
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending())
                  .isEqualTo(true);
            });
  }

  @Test
  void testUpdateDocumentUnit_addNewEnsuingDecision() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().ensuingDecisions().size()).isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                        .pending(true)
                        .note("note2")
                        .build(),
                    EnsuingDecision.builder().pending(false).note("note3").build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(3);
              assertThat(response.getResponseBody().ensuingDecisions().get(2).isPending())
                  .isEqualTo(false);
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateExistingEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("note1");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("abc");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getUuid())
                  .isEqualTo(ensuingDecisionUUID1);
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("updatedNote1");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("cba");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getUuid())
                  .isEqualTo(ensuingDecisionUUID1);
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getUuid())
                  .isEqualTo(ensuingDecisionUUID2);
            });
  }

  @Test
  void testUpdateDocumentationUnit_tryToAddAEmptyEnsuingDecision_shouldNotSucceed() {
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build(),
                    EnsuingDecision.builder().build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
            });
  }

  @Test
  void testUpdateDocumentUnit_removeValuesDeletesEnsuingDecision() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                        .build(),
                    EnsuingDecision.builder().build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1);
            });
  }

  @Test
  void testUpdateDocumentUnit_removeActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1);
            });
  }

  // Todo: add linking ensuing decisions tests

}
