package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

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
    controllers = {DocumentUnitController.class},
    timeout = "10000000")
class SaveNormIntegrationTest {
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
  @Autowired private DatabaseNormReferenceRepository normRepository;
  @Autowired private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private UUID documentationOfficeUuid;

  @Autowired
  private DatabaseDocumentationUnitSearchRepository databaseDocumentationUnitSearchRepository;

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
    normAbbreviationRepository.deleteAll();
    normRepository.deleteAll();
    repository.deleteAll().block();
  }

  // TODO: write a test for add a document type with a wrong shortcut

  @Test
  void testSaveNorm_withoutNorm() {
    UUID uuid = UUID.randomUUID();
    Instant creationTimestamp = Instant.now();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(creationTimestamp)
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();

    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(uuid)
            .creationtimestamp(creationTimestamp)
            .documentNumber("1234567890123")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).isNull();
            });
  }

  @Test
  void testSaveNorm_withOneNormAndNoChange() {
    UUID uuid = UUID.randomUUID();
    Instant creationTimestamp = Instant.now();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(creationTimestamp)
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();
    DocumentUnitDTO savedDTO = repository.save(dto).block();
    NormReferenceDTO norm = addNormToDB(1, savedDTO);

    DocumentUnitNorm norm1 =
        DocumentUnitNorm.builder().normAbbreviation("norm abbreviation 1").id(norm.getId()).build();

    DocumentUnit documentUnitFromFrontend = generateDocumentationUnit(uuid, creationTimestamp);
    documentUnitFromFrontend.contentRelatedIndexing().norms().add(norm1);

    List<DocumentUnitNorm> expectedNormList = List.of(norm1);

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms())
                  .containsExactlyElementsOf(expectedNormList);
            });
  }

  /** Sorting by remove norm abbreviation of a existing norm reference */
  @Test
  void testSaveNorm_RISDEV2185() {
    UUID uuid = UUID.randomUUID();
    Instant creationTimestamp = Instant.now();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(creationTimestamp)
            .documentnumber("1234567890123")
            .documentationOfficeId(documentationOfficeUuid)
            .build();
    DocumentUnitDTO savedDTO = repository.save(dto).block();
    var dbnorm1 = addNormToDB(1, savedDTO);
    var dbnorm2 = addNormToDB(2, savedDTO);

    DocumentUnit documentUnitFromFrontend = generateDocumentationUnit(uuid, creationTimestamp);

    DocumentUnitNorm norm1 =
        DocumentUnitNorm.builder()
            .id(dbnorm1.getId())
            .normAbbreviation("norm abbreviation 1")
            .build();
    DocumentUnitNorm norm2 = DocumentUnitNorm.builder().id(dbnorm2.getId()).build();
    documentUnitFromFrontend.contentRelatedIndexing().norms().addAll(List.of(norm1, norm2));

    List<DocumentUnitNorm> expectedNormList = List.of(norm1, norm2);

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms())
                  .containsExactlyElementsOf(expectedNormList);
            });
  }

  private DocumentUnit generateDocumentationUnit(UUID uuid, Instant creationTimestamp) {
    return DocumentUnit.builder()
        .uuid(uuid)
        .creationtimestamp(creationTimestamp)
        .documentNumber("1234567890123")
        .coreData(CoreData.builder().documentationOffice(docOffice).build())
        .contentRelatedIndexing(ContentRelatedIndexing.builder().norms(new ArrayList<>()).build())
        .build();
  }

  private NormReferenceDTO addNormToDB(int index, DocumentUnitDTO parent) {
    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .abbreviation("norm abbreviation " + index)
            .documentId((long) index)
            .build();
    normAbbreviationDTO = normAbbreviationRepository.save(normAbbreviationDTO);

    NormReferenceDTO normDTO =
        NormReferenceDTO.builder()
            .legacyDocUnitId(parent.getUuid())
            .normAbbreviation(normAbbreviationDTO.getAbbreviation())
            .build();
    normDTO = normRepository.save(normDTO);
    return normDTO;
  }
}
