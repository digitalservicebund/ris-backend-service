package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.ContentRelatedIndexingController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.KeywordService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresKeywordRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
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

@RISIntegrationTest(
    imports = {
      KeywordService.class,
      FieldOfLawService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresKeywordRepositoryImpl.class,
      PostgresFieldOfLawRepositoryImpl.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
    },
    controllers = {ContentRelatedIndexingController.class})
class DocumentUnitKeywordIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseKeywordRepository keywordRepository;
  @Autowired private DatabaseDocumentUnitRepository documentUnitRepository;
  @Autowired private JPADocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private DocumentUnitService documentUnitService;
  @MockBean private UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private JPADocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByLabel("DigitalService");

    doReturn(Mono.just(DocumentationOfficeTransformer.transformDTO(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    keywordRepository.deleteAll().block();
    documentUnitRepository.deleteAll().block();
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_withoutKeywords_shouldReturnEmptyList() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_withKeywords_shouldReturnList() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder().documentUnitId(documentUnitDTO.getId()).keyword("keyword01").build();
    keywordRepository.save(keywordDTO01).block();

    KeywordDTO keywordDTO02 =
        KeywordDTO.builder().documentUnitId(documentUnitDTO.getId()).keyword("keyword02").build();
    keywordRepository.save(keywordDTO02).block();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody()).containsExactly("keyword01", "keyword02"));
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_forNonExistingDocumentUnit_shouldReturnForbidden() {
    when(documentUnitService.getByUuid(TEST_UUID)).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testAddKeywordForDocumentUnit_shouldReturnListWithAllKeywords() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + TEST_UUID
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response -> assertThat(response.getResponseBody()).containsExactly("keyword01"));
  }

  @Test
  void testAddKeywordForNonExistingDocumentUnit_shouldReturnForbidden() {
    when(documentUnitService.getByUuid(TEST_UUID)).thenReturn(Mono.empty());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + TEST_UUID
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void
      testAddExistingKeywordForDocumentUnit_shouldNotAddDuplicateKeywordAndReturnListWithAllKeywords() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder().documentUnitId(documentUnitDTO.getId()).keyword("keyword01").build();
    keywordRepository.save(keywordDTO01).block();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + TEST_UUID
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response -> assertThat(response.getResponseBody()).containsExactly("keyword01"));
  }

  @Test
  void testDeleteKeywordForDocumentUnit_shouldReturnListWithAllKeywords() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder().documentUnitId(documentUnitDTO.getId()).keyword("keyword01").build();
    keywordRepository.save(keywordDTO01).block();

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + TEST_UUID
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testDeleteNonExistingKeywordForDocumentUnit_shouldReturnListWithAllKeywords() {
    DocumentUnitDTO documentUnitDTO =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(TEST_UUID)
                    .documentationOffice(docOfficeDTO)
                    .documentnumber("docnr12345678")
                    .creationtimestamp(Instant.now())
                    .build())
            .block();

    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(Mono.just(DocumentUnitTransformer.transformDTO(documentUnitDTO)));

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder().documentUnitId(documentUnitDTO.getId()).keyword("keyword01").build();
    keywordRepository.save(keywordDTO01).block();

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + TEST_UUID
                + "/contentrelatedindexing/keywords/keyword02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response -> assertThat(response.getResponseBody()).containsExactly("keyword01"));
  }
}
