package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.ContentRelatedIndexingController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.KeywordService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitReadRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitWriteRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitWriteDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresKeywordRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      KeywordService.class,
      FieldOfLawService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresKeywordRepositoryImpl.class,
      PostgresFieldOfLawRepositoryImpl.class
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

  @Autowired private WebTestClient webClient;
  @Autowired private DatabaseKeywordRepository keywordRepository;
  @Autowired private DatabaseDocumentUnitReadRepository documentUnitRepository;
  @Autowired private DatabaseDocumentUnitWriteRepository documentUnitWriteRepository;

  @AfterEach
  void cleanUp() {
    keywordRepository.deleteAll().block();
    documentUnitWriteRepository.deleteAll().block();
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_withoutKeywords_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_withKeywords_shouldReturnList() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .keyword("keyword01")
            .build();
    keywordRepository.save(keywordDTO01).block();

    KeywordDTO keywordDTO02 =
        KeywordDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .keyword("keyword02")
            .build();
    keywordRepository.save(keywordDTO02).block();

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody()).containsExactly("keyword01", "keyword02"));
  }

  @Test
  void testGetAllKeywordsForDocumentUnit_forNonExistingDocumentUnit_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testAddKeywordForDocumentUnit_shouldReturnListWithAllKeywords() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response -> assertThat(response.getResponseBody()).containsExactly("keyword01"));
  }

  @Test
  void testAddKeywordForNonExistingDocumentUnit_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void
      testAddExistingKeywordForDocumentUnit_shouldNotAddDuplicateKeywordAndReturnListWithAllKeywords() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .keyword("keyword01")
            .build();
    keywordRepository.save(keywordDTO01).block();

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
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
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .keyword("keyword01")
            .build();
    keywordRepository.save(keywordDTO01).block();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords/keyword01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testDeleteNonExistingKeywordForDocumentUnit_shouldReturnListWithAllKeywords() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    KeywordDTO keywordDTO01 =
        KeywordDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .keyword("keyword01")
            .build();
    keywordRepository.save(keywordDTO01).block();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/keywords/keyword02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String[].class)
        .consumeWith(
            response -> assertThat(response.getResponseBody()).containsExactly("keyword01"));
  }
}
