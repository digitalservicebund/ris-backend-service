package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.ContentRelatedIndexingController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.KeywordService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitFieldsOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitReadRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitWriteRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitFieldsOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitWriteDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresKeywordRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
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
import reactor.test.StepVerifier;

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
class DocumentUnitFieldOfLawIntegrationTest {
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

  @Autowired private DatabaseFieldOfLawRepository fieldOfLawRepository;
  @Autowired private DatabaseDocumentUnitReadRepository documentUnitRepository;
  @Autowired private DatabaseDocumentUnitWriteRepository documentUnitWriteRepository;
  @Autowired private DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;

  @AfterEach
  void cleanUp() {
    fieldOfLawRepository.deleteAll().block();
    documentUnitWriteRepository.deleteAll().block();
    documentUnitFieldsOfLawRepository.deleteAll().block();
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_withoutFieldOfLawLinked_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    generateFieldsOfLaw("SF-01", "SF-02");

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void
      testGetAllFieldsOfLawForDocumentUnit_withFirstFieldOfLawLinked_shouldReturnListWithLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01"));
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_shouldReturnSortedList() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01", "AR-02", "XR-03", "XR-01-02");

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("AR-02", "SF-01", "XR-01-02", "XR-03"));
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_forNotExistingDocumentUnit_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();

    webClient
        .mutateWith(csrf())
        .get()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testAddFieldsOfLawForDocumentUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/SF-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01", "SF-02"));
  }

  @Test
  void
      testAddFieldsOfLawForDocumentUnit_withNotExistingFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/ST-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01"));
  }

  @Test
  void testAddFieldsOfLawForDocumentUnit_withNotExistingDocumentUnit_shouldReturnEmptyMono() {
    UUID documentUnitUuid = UUID.randomUUID();

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/ST-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isNull());
  }

  @Test
  void
      testAddFieldsOfLawForDocumentUnit_withAlreadyLinkedFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/SF-01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01"));
  }

  @Test
  void testRemoveFieldsOfLawForDocumentUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/SF-01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void
      testRemoveFieldsOfLawForDocumentUnit_withNotLinkedFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/ST-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01"));
  }

  @Test
  void
      testRemoveFieldsOfLawForDocumentUnit_withNotExistingFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitWriteDTO = documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    assertThat(documentUnitWriteDTO).isNotNull();

    generateAndAddFieldsOfLaw(documentUnitWriteDTO.getId(), "SF-01");
    generateFieldsOfLaw("SF-02");

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/ST-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("SF-01"));
  }

  @Test
  void testRemoveFieldsOfLawForDocumentUnit_withNotExistingDocumentUnit_shouldReturnEmptyMono() {
    UUID documentUnitUuid = UUID.randomUUID();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentUnitUuid
                + "/contentrelatedindexing/fieldsoflaw/ST-02")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isNull());
  }

  @Test
  void testDeleteCascadeForDocumentUnit() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitWriteDTO savedDocumentUnitWriteDTO =
        documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    FieldOfLawDTO fieldOfLawDTO = FieldOfLawDTO.builder().identifier("SF-01").build();
    FieldOfLawDTO savedFieldOfLawDTO = fieldOfLawRepository.save(fieldOfLawDTO).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .fieldOfLawId(fieldOfLawDTO.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

    assertThat(savedDocumentUnitWriteDTO).isNotNull();
    assertThat(savedFieldOfLawDTO).isNotNull();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll())
        .consumeNextWith(
            link1 ->
                assertThat(link1)
                    .extracting("documentUnitId", "fieldOfLawId")
                    .containsExactly(savedDocumentUnitWriteDTO.getId(), savedFieldOfLawDTO.getId()))
        .verifyComplete();

    documentUnitWriteRepository.delete(documentUnitWriteDTO).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll()).verifyComplete();
  }

  @Test
  void testDeleteCascadeForFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitWriteDTO documentUnitWriteDTO =
        DocumentUnitWriteDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitWriteDTO savedDocumentUnitWriteDTO =
        documentUnitWriteRepository.save(documentUnitWriteDTO).block();

    FieldOfLawDTO fieldOfLawDTO = FieldOfLawDTO.builder().identifier("SF-01").build();
    FieldOfLawDTO savedFieldOfLawDTO = fieldOfLawRepository.save(fieldOfLawDTO).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitWriteDTO.getId())
            .fieldOfLawId(fieldOfLawDTO.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

    assertThat(savedDocumentUnitWriteDTO).isNotNull();
    assertThat(savedFieldOfLawDTO).isNotNull();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll())
        .consumeNextWith(
            link1 ->
                assertThat(link1)
                    .extracting("documentUnitId", "fieldOfLawId")
                    .containsExactly(savedDocumentUnitWriteDTO.getId(), savedFieldOfLawDTO.getId()))
        .verifyComplete();

    fieldOfLawRepository.delete(fieldOfLawDTO).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll()).verifyComplete();
  }

  private void generateAndAddFieldsOfLaw(Long documentUnitId, String... fieldsOfLawIdentifier) {
    for (String identifier : fieldsOfLawIdentifier) {
      FieldOfLawDTO fieldOfLawDTO = generateFieldOfLaw(identifier);

      DocumentUnitFieldsOfLawDTO link =
          DocumentUnitFieldsOfLawDTO.builder()
              .documentUnitId(documentUnitId)
              .fieldOfLawId(fieldOfLawDTO.getId())
              .build();
      documentUnitFieldsOfLawRepository.save(link).block();
    }
  }

  private void generateFieldsOfLaw(String... fieldsOfLawIdentifier) {
    for (String identifier : fieldsOfLawIdentifier) {
      generateFieldOfLaw(identifier);
    }
  }

  private FieldOfLawDTO generateFieldOfLaw(String fieldOfLawIdentifier) {
    FieldOfLawDTO fieldOfLawDTO1 = FieldOfLawDTO.builder().identifier(fieldOfLawIdentifier).build();
    return fieldOfLawRepository.save(fieldOfLawDTO1).block();
  }
}
