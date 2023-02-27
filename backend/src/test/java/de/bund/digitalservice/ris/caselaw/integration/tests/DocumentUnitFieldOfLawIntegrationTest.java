package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.ContentRelatedIndexingController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitFieldsOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitFieldsOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresSubjectFieldRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseSubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
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
      FieldOfLawService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresSubjectFieldRepositoryImpl.class
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

  @Autowired private DatabaseSubjectFieldRepository fieldOfLawRepository;
  @Autowired private DatabaseDocumentUnitRepository documentUnitRepository;
  @Autowired private DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;

  @AfterEach
  void cleanUp() {
    fieldOfLawRepository.deleteAll().block();
    documentUnitRepository.deleteAll().block();
    documentUnitFieldsOfLawRepository.deleteAll().block();
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_withoutFieldOfLawLinked_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
  void testAddFieldsOfLawForDocumentUnit_withNotExistingDocumentUnit_shouldReturnEmptyList() {
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
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void
      testAddFieldsOfLawForDocumentUnit_withAlreadyLinkedFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();
    SubjectFieldDTO fieldOfLawDTO2 = SubjectFieldDTO.builder().subjectFieldNumber("SF-02").build();
    fieldOfLawRepository.save(fieldOfLawDTO2).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    documentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO1 = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    fieldOfLawDTO1 = fieldOfLawRepository.save(fieldOfLawDTO1).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO1.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

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
  void testRemoveFieldsOfLawForDocumentUnit_withNotExistingDocumentUnit_shouldReturnEmptyList() {
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
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testDeleteCascadeForDocumentUnit() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    SubjectFieldDTO savedFieldOfLawDTO = fieldOfLawRepository.save(fieldOfLawDTO).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll())
        .consumeNextWith(
            link1 ->
                assertThat(link1)
                    .extracting("documentUnitId", "fieldOfLawId")
                    .containsExactly(savedDocumentUnitDTO.getId(), savedFieldOfLawDTO.getId()))
        .verifyComplete();

    documentUnitRepository.delete(documentUnitDTO).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll()).verifyComplete();
  }

  @Test
  void testDeleteCascadeForFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = documentUnitRepository.save(documentUnitDTO).block();

    SubjectFieldDTO fieldOfLawDTO = SubjectFieldDTO.builder().subjectFieldNumber("SF-01").build();
    SubjectFieldDTO savedFieldOfLawDTO = fieldOfLawRepository.save(fieldOfLawDTO).block();

    DocumentUnitFieldsOfLawDTO link =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitDTO.getId())
            .fieldOfLawId(fieldOfLawDTO.getId())
            .build();
    documentUnitFieldsOfLawRepository.save(link).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll())
        .consumeNextWith(
            link1 ->
                assertThat(link1)
                    .extracting("documentUnitId", "fieldOfLawId")
                    .containsExactly(savedDocumentUnitDTO.getId(), savedFieldOfLawDTO.getId()))
        .verifyComplete();

    fieldOfLawRepository.delete(fieldOfLawDTO).block();

    StepVerifier.create(documentUnitFieldsOfLawRepository.findAll()).verifyComplete();
  }
}
