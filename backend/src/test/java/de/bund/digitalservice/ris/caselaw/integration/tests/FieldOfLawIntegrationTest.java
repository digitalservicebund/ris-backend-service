package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresSubjectFieldRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseSubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
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
      FieldOfLawService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresSubjectFieldRepositoryImpl.class
    },
    controllers = {FieldOfLawController.class})
class FieldOfLawIntegrationTest {
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
  @Autowired private DatabaseSubjectFieldRepository repository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
  }

  @Test
  void testGetAllFieldsOfLaw() {
    prepareDatabase();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("FL", "FL-01", "FL-01-01", "FL-02", "FL-03", "FL-04", "FO"));
  }

  @Test
  void testGetFieldsOfLawBySearchQuery() {
    prepareDatabase();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw?searchStr=FL")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("FL", "FL-01", "FL-01-01", "FL-02", "FL-03", "FL-04"));
  }

  @Test
  void testGetChildrenForFieldOfLawNumber() {
    prepareDatabase();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/FL/children")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactly("FL-01", "FL-02", "FL-03", "FL-04"));
  }

  @Test
  void testGetParentForFieldOfLaw() {
    prepareDatabase();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/FL-01-01/tree")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw.class)
        .consumeWith(
            response -> {
              FieldOfLaw field = response.getResponseBody();
              assertThat(field).isNotNull();
              assertThat(field.identifier()).isEqualTo("FL");
              assertThat(field.children()).hasSize(1);
              FieldOfLaw child = field.children().get(0);
              assertThat(child.identifier()).isEqualTo("FL-01");
              assertThat(child.children()).hasSize(1);
              child = child.children().get(0);
              assertThat(child.identifier()).isEqualTo("FL-01-01");
              assertThat(child.children()).isEmpty();
            });
  }

  private void prepareDatabase() {
    // first root child
    SubjectFieldDTO subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(1L)
            .subjectFieldNumber("FL")
            .isNew(true)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // child of the first root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(2L)
            .isNew(true)
            .subjectFieldNumber("FL-01")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // sub child of the child of the first root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(3L)
            .isNew(true)
            .subjectFieldNumber("FL-01-01")
            .parentId(2L)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // second root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(4L)
            .isNew(true)
            .subjectFieldNumber("FO")
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // second child of the first root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(5L)
            .isNew(true)
            .subjectFieldNumber("FL-02")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // third child of the first root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(6L)
            .isNew(true)
            .subjectFieldNumber("FL-03")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();

    // fourth child of the first root child
    subjectFieldDTO =
        SubjectFieldDTO.builder()
            .id(7L)
            .isNew(true)
            .subjectFieldNumber("FL-04")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(subjectFieldDTO).block();
  }
}
