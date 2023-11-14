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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
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
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseNormReferenceRepository normRepository;
  @Autowired private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;

  @Autowired
  private DatabaseDocumentationUnitSearchRepository databaseDocumentationUnitSearchRepository;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

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
    normRepository.deleteAll();
    repository.deleteAll();
    normAbbreviationRepository.deleteAll();
  }

  // TODO: write a test for add a document type with a wrong shortcut

  @Test
  void testSaveNorm_withoutNorm() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(documentationOfficeDTO)
            .build();

    repository.save(dto);

    DocumentUnit documentUnitFromFrontend = generateDocumentationUnit(dto.getId());

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).isEmpty();
            });
  }

  @Test
  void testSaveNorm_withOneNormAndNoChange() {
    NormAbbreviationDTO normAbbreviation = addNormToDB(2352);
    DocumentationUnitDTO savedDocumentationUnitDTO =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890124")
                .documentationOffice(documentationOfficeDTO)
                .normReferences(
                    List.of(NormReferenceDTO.builder().normAbbreviation(normAbbreviation).build()))
                .build());

    DocumentUnit documentUnitFromFrontend =
        generateDocumentationUnit(savedDocumentationUnitDTO.getId());

    documentUnitFromFrontend
        .contentRelatedIndexing()
        .norms()
        .add(
            NormReference.builder()
                .normAbbreviation(NormAbbreviation.builder().id(normAbbreviation.getId()).build())
                .build());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(normAbbreviation.getId());
            });
  }

  /** Sorting by remove norm abbreviation of a existing norm reference */
  @Test
  void testSaveNorm_RISDEV2185() {
    var dbNormAbbreviation1 = addNormToDB(1);
    var dbNormAbbreviation2 = addNormToDB(2);

    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(documentationOfficeDTO)
                .build());

    DocumentUnit documentUnitFromFrontend = generateDocumentationUnit(dto.getId());

    NormReference norm1 =
        NormReference.builder()
            .normAbbreviation(NormAbbreviation.builder().id(dbNormAbbreviation1.getId()).build())
            .build();
    NormReference norm2 =
        NormReference.builder()
            .normAbbreviation(NormAbbreviation.builder().id(dbNormAbbreviation2.getId()).build())
            .build();
    documentUnitFromFrontend.contentRelatedIndexing().norms().addAll(List.of(norm1, norm2));

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(dbNormAbbreviation1.getId());
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(1)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(dbNormAbbreviation2.getId());
            });
  }

  private DocumentUnit generateDocumentationUnit(UUID uuid) {
    return DocumentUnit.builder()
        .uuid(uuid)
        .documentNumber("1234567890123")
        .coreData(CoreData.builder().documentationOffice(docOffice).build())
        .contentRelatedIndexing(ContentRelatedIndexing.builder().norms(new ArrayList<>()).build())
        .build();
  }

  private NormAbbreviationDTO addNormToDB(int index) {
    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .abbreviation("norm abbreviation " + index)
            .documentId((long) index)
            .build();
    return normAbbreviationRepository.saveAndFlush(normAbbreviationDTO);
  }
}
