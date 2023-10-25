package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.AuthUtils;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationUnitLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationUnitLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
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
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
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
import reactor.test.StepVerifier;
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
    timeout = "PT5M")
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
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentationUnitLinkRepository linkRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository documentTypeRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private static final String DOCUMENT_NUMBER_PREFIX = "XXRE20230000";
  private static final String UUID_PREFIX = "88888888-4444-4444-4444-11111111111";
  private static final Instant TIMESTAMP = Instant.parse("2000-02-01T20:13:36.00Z");
  private static final Instant DECISION_DATE = Instant.parse("1983-09-15T08:21:17.00Z");
  private UUID docOfficeUuid;

  private DocumentCategoryDTO category;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;

  @BeforeEach
  void setUp() {
    when(userService.getDocumentationOffice(any(OidcUser.class)))
        .thenReturn(Mono.just(AuthUtils.buildDocOffice("DigitalService")));

    docOfficeUuid = documentationOfficeRepository.findByAbbreviation("DigitalService").getId();

    category = databaseDocumentCategoryRepository.findFirstByLabel("R");
    if (category == null) {
      category =
          databaseDocumentCategoryRepository.saveAndFlush(
              DocumentCategoryDTO.builder().label("R").build());
    }
  }

  @AfterEach
  void cleanUp() {
    linkRepository.deleteAll().block();
    repository.deleteAll().block();
    documentTypeRepository.deleteAll();
  }

  @Test
  void testFindDocumentationUnit_withActiveCitation() {
    prepareTestData(1, 0);

    ActiveCitation activeCitation = generateActiveCitation(1, false);

    DocumentUnit documentUnit = generateDocumentUnit(List.of(activeCitation));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + DOCUMENT_NUMBER_PREFIX + "0")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(documentUnit);
            });
  }

  @Test
  void testUpdateDocumentationUnit_withNewActiveCitation() {
    prepareTestData(1, 0);
    var type = addDocumentTypeToDB(2);

    ActiveCitation activeCitation = generateActiveCitation(1, false);
    ActiveCitation newActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().dataSource(null).uuid(null).build();
    ActiveCitation newActiveCitationResponse = generateActiveCitation(2, true).toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(activeCitation, newActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(activeCitation, newActiveCitationResponse));

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
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("contentRelatedIndexing.activeCitations")
                  .isEqualTo(documentUnitResponse);
              List<ActiveCitation> activeCitations =
                  response.getResponseBody().contentRelatedIndexing().activeCitations();
              assertThat(activeCitations).hasSize(2);
              assertThat(activeCitations.get(0)).isEqualTo(activeCitation);
              assertThat(activeCitations.get(1))
                  .usingRecursiveComparison()
                  .ignoringFields("uuid")
                  .isEqualTo(newActiveCitationResponse);
            });
  }

  @Test
  void testUpdateDocumentationUnit_addLinkToExistingDocumentationUnit() {
    prepareTestData(2, 0);
    removeLink(2);

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
  void testUpdateDocumentationUnit_tryToAddAEmptyActiveCitation() {
    prepareTestData(1, 0);

    ActiveCitation activeCitation = generateActiveCitation(1, false);
    ActiveCitation newActiveCitationRequest = ActiveCitation.builder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(activeCitation, newActiveCitationRequest));
    DocumentUnit documentUnitResponse = generateDocumentUnit(List.of(activeCitation));

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

    StepVerifier.create(repository.findAll()).expectNextCount(2).verifyComplete();
    StepVerifier.create(linkRepository.findAll()).expectNextCount(1).verifyComplete();
  }

  @Test
  void testUpdateDocumentationUnit_removeEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation activeCitation = generateActiveCitation(1, false);

    DocumentUnit documentUnitRequest = generateDocumentUnit(List.of(activeCitation));
    DocumentUnit documentUnitResponse = generateDocumentUnit(List.of(activeCitation));

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

    StepVerifier.create(repository.findByUuid(UUID.fromString(UUID_PREFIX + "2"))).verifyComplete();
    StepVerifier.create(
            linkRepository.findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
                UUID.fromString(UUID_PREFIX + "0"),
                UUID.fromString(UUID_PREFIX + "2"),
                DocumentationUnitLinkType.ACTIVE_CITATION))
        .verifyComplete();
  }

  @Test
  void testUpdateDocumentationUnit_removeRealActiveCitation() {
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

    StepVerifier.create(repository.findByUuid(UUID.fromString(UUID_PREFIX + "1")))
        .expectNextCount(1)
        .verifyComplete();
    StepVerifier.create(
            linkRepository.findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
                UUID.fromString(UUID_PREFIX + "0"),
                UUID.fromString(UUID_PREFIX + "1"),
                DocumentationUnitLinkType.ACTIVE_CITATION))
        .verifyComplete();
  }

  @Test
  void testUpdateDocumentationUnit_removeAllActiveCitation() {
    prepareTestData(1, 1);

    DocumentUnit documentUnitRequest = generateDocumentUnit(Collections.emptyList());
    DocumentUnit documentUnitResponse = generateDocumentUnit(Collections.emptyList());

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

    StepVerifier.create(repository.findByUuid(UUID.fromString(UUID_PREFIX + "2"))).verifyComplete();
    StepVerifier.create(linkRepository.findAll()).verifyComplete();
  }

  @Test
  void testUpdateDocumentationUnit_removeCourtInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitation = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().court(null).build();
    ActiveCitation editableActiveCitationResponse =
        editableActiveCitationRequest.toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationResponse));

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
  void testUpdateDocumentationUnit_removeCitationStyleInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitation = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().citationStyle(null).build();
    ActiveCitation editableActiveCitationResponse =
        editableActiveCitationRequest.toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationResponse));

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
  void testUpdateDocumentationUnit_removeCitationStyleInRealActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitationRequest =
        generateActiveCitation(1, false).toBuilder().citationStyle(null).build();
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
  void testUpdateDocumentationUnit_removeDocumentTypeInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitation = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().documentType(null).build();
    ActiveCitation editableActiveCitationResponse =
        editableActiveCitationRequest.toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationResponse));

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
  void testUpdateDocumentationUnit_removeFileNumberInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitation = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().fileNumber(null).build();
    ActiveCitation editableActiveCitationResponse =
        editableActiveCitationRequest.toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationResponse));

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
  void testUpdateDocumentationUnit_removeDecisionDateInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation realActiveCitation = generateActiveCitation(1, false);
    ActiveCitation editableActiveCitationRequest =
        generateActiveCitation(2, true).toBuilder().decisionDate(null).build();
    ActiveCitation editableActiveCitationResponse =
        editableActiveCitationRequest.toBuilder().build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationRequest));
    DocumentUnit documentUnitResponse =
        generateDocumentUnit(List.of(realActiveCitation, editableActiveCitationResponse));

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

  @Test
  void testUpdateDocumentationUnit_removeAllValuesInEditableActiveCitation() {
    prepareTestData(1, 1);

    ActiveCitation activeCitation = generateActiveCitation(1, false);
    ActiveCitation activeCitationRequest =
        generateActiveCitation(2, true).toBuilder()
            .court(null)
            .fileNumber(null)
            .citationStyle(null)
            .decisionDate(null)
            .documentType(null)
            .build();

    DocumentUnit documentUnitRequest =
        generateDocumentUnit(List.of(activeCitation, activeCitationRequest));
    DocumentUnit documentUnitResponse = generateDocumentUnit(List.of(activeCitation));

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

    DocumentUnitDTO parentDTO =
        DocumentUnitDTO.builder()
            .uuid(UUID.fromString(UUID_PREFIX + "0"))
            .documentationOfficeId(docOfficeUuid)
            .documentnumber(DOCUMENT_NUMBER_PREFIX + "0")
            .creationtimestamp(TIMESTAMP.plus(0, ChronoUnit.MINUTES))
            .decisionDate(LocalDate.ofInstant(DECISION_DATE, ZoneId.of("Europe/Berlin")))
            .documentTypeId(type.getId())
            .build();

    DocumentUnitDTO savedParentDTO = repository.save(parentDTO).block();

    FileNumberDTO fileNumberDTO =
        FileNumberDTO.builder()
            .fileNumber("file number #0")
            .documentUnitId(savedParentDTO.getId())
            .isDeviating(false)
            .build();

    fileNumberRepository.save(fileNumberDTO).block();

    for (int i = 0; i < realActiveCitationCount; i++) {
      addActiveCitationToDB(i + 1, false);
    }

    for (int i = 0; i < editableActiveCitationCount; i++) {
      addActiveCitationToDB(i + realActiveCitationCount + 1, true);
    }
  }

  private void addActiveCitationToDB(int offset, boolean editable) {
    var type = addDocumentTypeToDB(offset);

    DocumentUnitDTO activeCitationDTO =
        DocumentUnitDTO.builder()
            .uuid(UUID.fromString(UUID_PREFIX + offset))
            .documentationOfficeId(docOfficeUuid)
            .documentnumber(DOCUMENT_NUMBER_PREFIX + offset)
            .creationtimestamp(TIMESTAMP.plus(offset * 10L, ChronoUnit.MINUTES))
            .decisionDate(
                LocalDate.ofInstant(
                    DECISION_DATE.plus(offset * 10L, ChronoUnit.MINUTES),
                    ZoneId.of("Europe/Berlin")))
            .courtType("TestCourt #" + offset)
            .courtLocation("Berlin #" + offset)
            .documentTypeId(type.getId())
            .dateKnown(true)
            .dataSource(editable ? DataSource.ACTIVE_CITATION : DataSource.NEURIS)
            .build();

    DocumentUnitDTO savedActiveCitationDTO = repository.save(activeCitationDTO).block();

    FileNumberDTO fileNumberDTO =
        FileNumberDTO.builder()
            .fileNumber("file number #" + offset)
            .documentUnitId(savedActiveCitationDTO.getId())
            .isDeviating(false)
            .build();

    fileNumberRepository.save(fileNumberDTO).block();

    DocumentationUnitLinkDTO linkDTO =
        DocumentationUnitLinkDTO.builder()
            .parentDocumentationUnitUuid(UUID.fromString(UUID_PREFIX + "0"))
            .childDocumentationUnitUuid(UUID.fromString(UUID_PREFIX + offset))
            .type(DocumentationUnitLinkType.ACTIVE_CITATION)
            .build();

    linkRepository.save(linkDTO).block();
  }

  private void removeLink(int number) {
    DocumentationUnitLinkDTO linkDTO =
        linkRepository
            .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
                UUID.fromString(UUID_PREFIX + "0"),
                UUID.fromString(UUID_PREFIX + number),
                DocumentationUnitLinkType.ACTIVE_CITATION)
            .block();

    linkRepository.delete(linkDTO).block();
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
        .dataSource(editable ? DataSource.ACTIVE_CITATION : DataSource.NEURIS)
        .uuid(UUID.fromString(UUID_PREFIX + number))
        .court(
            Court.builder()
                .type("TestCourt #" + number)
                .location("Berlin #" + number)
                .label("TestCourt #" + number + " Berlin #" + number)
                .build())
        .dateKnown(true)
        .decisionDate(
            LocalDate.ofInstant(
                DECISION_DATE.plus(number * 10L, ChronoUnit.MINUTES), ZoneId.of("Europe/Berlin")))
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
                .decisionDate(LocalDate.ofInstant(DECISION_DATE, ZoneId.of("Europe/Berlin")))
                .incorrectCourts(Collections.emptyList())
                .documentationOffice(AuthUtils.buildDefaultDocOffice())
                .build())
        .texts(Texts.builder().build())
        .contentRelatedIndexing(contentRelatedIndexing)
        .status(DocumentUnitStatus.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .proceedingDecisions(Collections.emptyList())
        .build();
  }
}
