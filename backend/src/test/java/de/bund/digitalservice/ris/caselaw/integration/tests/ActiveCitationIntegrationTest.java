package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.AuthUtils;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRelatedDocumentationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
      DatabaseDocumentNumberService.class,
      DatabaseDocumentUnitStatusService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class},
    timeout = "PT5M")
@Sql(scripts = {"classpath:active_citations_init.sql"})
@Sql(
    scripts = {"classpath:active_citations_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class ActiveCitationIntegrationTest {
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
  @Autowired private DatabaseDocumentTypeRepository documentTypeRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseRelatedDocumentationRepository relatedDocumentationRepository;
  @MockBean private UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;

  private static final String DOCUMENT_NUMBER_PREFIX = "XXRE20230000";
  private static final String UUID_PREFIX = "88888888-4444-4444-4444-11111111111";
  private static final Instant TIMESTAMP = Instant.parse("2000-02-01T20:13:36.00Z");
  private static final LocalDate DECISION_DATE = LocalDate.parse("1983-09-15");
  private UUID docOfficeUuid;

  private DocumentCategoryDTO category;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;

  @BeforeEach
  void setUp() {
    DocumentationOfficeDTO docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");

    doReturn(Mono.just(DocumentationOfficeTransformer.transformToDomain(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @Test
  void testGetDocumentUnit_withoutActiveCitation_shouldReturnEmptyList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .isEmpty());
  }

  @Test
  void testGetDocumentUnit_withActiveCitation_shouldReturnList() {
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });
  }

  @Test
  void testUpdateDocumentUnit_addNewActiveCitation() {
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
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("7da39a1e-78a9-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("7da39a1e-78a9-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(3);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(2)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateExistingActiveCitation() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("abc");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(activeCitationUUID1);
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("cba")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("cba");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(activeCitationUUID1);
            });
  }

  @Test
  void testUpdateDocumentationUnit_tryToAddAEmptyActiveCitation_shouldNotSucceed() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
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
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("abc")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder().build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
            });
  }

  @Test
  void testUpdateDocumentUnit_removeValuesDeletesActiveCitation() {
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
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("7da39a1e-78a9-11ee-b962-0242ac120002"))
                                .build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
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
            response ->
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
            });
  }

  @Test
  void testUpdateDocumentUnit_removeAllActiveCitation() {
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
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                // Todo is this a realistic scenario to delete all active citations?
                ContentRelatedIndexing.builder()
                    .activeCitations(List.of(ActiveCitation.builder().build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(0);
            });
  }

  @Test
  void testUpdateDocumentationUnit_withListOfMixedVariations() {
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
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .activeCitations()
                            .size())
                    .isEqualTo(2));

    UUID uuid = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(uuid)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("abc")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build(),
                            ActiveCitation.builder().build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(uuid);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeCourtInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .isNotNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .extracting("id")
                  .isEqualTo(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"));
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeCitationStyleInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("uuid")
                  .isEqualTo(UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"));
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("uuid")
                  .isEqualTo(UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"));
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeDocumentTypeInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentType())
                  .extracting("label")
                  .isEqualTo("Beschluss");
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentType())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeFileNumberInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("abc");
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeDecisionDateInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDecisionDate())
                  .isEqualTo("2011-01-21");
            });

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(
                      response.getResponseBody().contentRelatedIndexing().activeCitations().size())
                  .isEqualTo(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDecisionDate())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_addLinkToExistingDocumentationUnit() {
    prepareTestData(2, 0);
    //  TODO relatedDocumentationRepository.deleteById(id);

    ActiveCitation activeCitation = generateActiveCitation(1, false);
    ActiveCitation linkedActiveCitation = generateActiveCitation(2, false);

    DocumentUnit documentUnit = generateDocumentUnit(List.of(activeCitation, linkedActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnit);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_addLinkToExistingDocumentationUnitWhichIsLinkedAlready() {
    prepareTestData(2, 0);

    ActiveCitation activeCitation = generateActiveCitation(1, false);
    ActiveCitation linkedActiveCitation = generateActiveCitation(2, false);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(activeCitation, linkedActiveCitation, linkedActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(activeCitation, linkedActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeLinkedActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest = generateDocumentUnit(List.of(editableActiveCitation));
    DocumentUnit documentUnitResponse = generateDocumentUnit(List.of(editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });

    assert repository.findById(UUID.fromString(UUID_PREFIX + "1")).isPresent();

    assertThat(
            relatedDocumentationRepository
                .findAllByReferencedDocumentationUnitId(UUID.fromString(UUID_PREFIX + "0"))
                .get(0)
                .getReferencedDocumentationUnitId())
        .isEqualTo(UUID.fromString(UUID_PREFIX + "1"));
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeCourtInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationResponse = generateActiveCitation(1, false);
    ActiveCitation realActiveCitationRequest =
        realActiveCitationResponse.toBuilder().court(null).build();
    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitationRequest, editableActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitationResponse, editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeCitationStyleInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationRequest =
        generateActiveCitation(1, false).toBuilder().citationType(null).build();
    ActiveCitation realActiveCitationResponse = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitationRequest, editableActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitationResponse, editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeDocumentTypeInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationRequest =
        generateActiveCitation(1, false).toBuilder().documentType(null).build();
    ActiveCitation realActiveCitationResponse = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitationRequest, editableActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitationResponse, editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeFileNumberInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationRequest =
        generateActiveCitation(1, false).toBuilder().fileNumber(null).build();
    ActiveCitation realActiveCitationResponse = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitationRequest, editableActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitationResponse, editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  @Test
  @Disabled
  void testUpdateDocumentationUnit_removeDecisionDateInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationRequest =
        generateActiveCitation(1, false).toBuilder().decisionDate(null).build();
    ActiveCitation realActiveCitationResponse = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitation = generateActiveCitation(2, true);

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitationRequest, editableActiveCitation));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitationResponse, editableActiveCitation));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + UUID_PREFIX + "0")
        .bodyValue(documentUnitRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody()).isEqualTo(documentUnitResponse);
            });
  }

  private void prepareTestData(int realActiveCitationCount, int editableActiveCitationCount) {
    var type = addDocumentTypeToDB(0);

    DocumentationUnitDTO parentDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.fromString(UUID_PREFIX + "0"))
            //            .documentationOffice(docOffice) TODO
            .documentNumber(DOCUMENT_NUMBER_PREFIX + "0")
            .decisionDate(DECISION_DATE)
            .fileNumbers(List.of(FileNumberDTO.builder().value("file number #0").build()))
            .documentType(type)
            .build();

    DocumentationUnitDTO savedParentDTO = repository.save(parentDTO);

    for (int i = 0; i < realActiveCitationCount; i++) {
      addActiveCitationToDB(i + 1, false);
    }

    for (int i = 0; i < editableActiveCitationCount; i++) {
      addActiveCitationToDB(i + realActiveCitationCount + 1, true);
    }
  }

  private void addActiveCitationToDB(int offset, boolean editable) {
    var type = addDocumentTypeToDB(offset);

    ActiveCitationDTO activeCitationDTO =
        ActiveCitationDTO.builder()
            .id(UUID.fromString(UUID_PREFIX + offset))
            .documentNumber(editable ? null : DOCUMENT_NUMBER_PREFIX + offset)
            .referencedDocumentationUnitId(
                editable ? null : UUID.randomUUID()) // TODO create docUnit instead
            .date(DECISION_DATE.plus(offset * 10L, ChronoUnit.MINUTES))
            .courtType("TestCourt #" + offset)
            .courtLocation("Berlin #" + offset)
            .fileNumber("file number #" + offset)
            .documentType(type)
            .build();

    ActiveCitationDTO savedActiveCitationDTO =
        relatedDocumentationRepository.save(activeCitationDTO);

    repository.save(
        DocumentationUnitDTO.builder().activeCitations(List.of(savedActiveCitationDTO)).build());
  }

  private DocumentTypeDTO addDocumentTypeToDB(int number) {
    DocumentTypeDTO documentTypeDTO =
        DocumentTypeDTO.builder()
            .abbreviation("abbreviation" + number)
            .label("R")
            .category(category)
            .multiple(true)
            .build();

    return documentTypeRepository.saveAndFlush(documentTypeDTO);
  }

  private ActiveCitation generateActiveCitation(int number, boolean editable) {
    return ActiveCitation.builder()
        .uuid(UUID.fromString(UUID_PREFIX + number))
        .court(
            Court.builder()
                .type("TestCourt #" + number)
                .location("Berlin #" + number)
                .label("TestCourt #" + number + " Berlin #" + number)
                .build())
        .decisionDate(DECISION_DATE.plus(number, ChronoUnit.DAYS))
        .documentNumber(DOCUMENT_NUMBER_PREFIX + number)
        .documentType(
            DocumentType.builder().jurisShortcut("abbreviation" + number).label("R").build())
        .fileNumber("file number #" + number)
        .build();
  }

  private DocumentUnit generateDocumentUnit(List<ActiveCitation> activeCitations) {
    ContentRelatedIndexing contentRelatedIndexing =
        ContentRelatedIndexing.builder()
            .activeCitations(activeCitations)
            .keywords(Collections.emptyList())
            .norms(Collections.emptyList())
            .fieldsOfLaw(Collections.emptyList())
            .build();

    return DocumentUnit.builder()
        .uuid(UUID.fromString(UUID_PREFIX + "0"))
        .documentNumber(DOCUMENT_NUMBER_PREFIX + "0")
        .dataSource(DataSource.NEURIS)
        .coreData(
            CoreData.builder()
                .deviatingEclis(Collections.emptyList())
                .deviatingDecisionDates(Collections.emptyList())
                .deviatingFileNumbers(Collections.emptyList())
                .fileNumbers(List.of("file number #0"))
                .documentType(
                    DocumentType.builder().jurisShortcut("abbreviation0").label("R").build())
                .decisionDate(DECISION_DATE)
                .deviatingCourts(Collections.emptyList())
                .documentationOffice(AuthUtils.buildDefaultDocOffice())
                .build())
        .texts(Texts.builder().build())
        .contentRelatedIndexing(contentRelatedIndexing)
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .previousDecisions(Collections.emptyList())
        .build();
  }
}
