package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      FieldOfLawService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresFieldOfLawRepositoryImpl.class
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
  @Autowired private DatabaseFieldOfLawRepository repository;
  // @Autowired private NormRepository normRepository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
    // normRepository.deleteAll().block();
  }

  @Test
  void testGetAllFieldsOfLaw() {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$.content[*].identifier");
    assertThat(identifiers)
        .containsExactly("FL", "FL-01", "FL-01-01", "FL-02", "FL-03", "FL-04", "FO");
  }

  @Test
  void testGetFieldsOfLawBySearchQuery() {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-01&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$.content[*].identifier");
    assertThat(identifiers).containsExactly("FL-01", "FL-01-01");
  }

  @Test
  void testPaginationInGetFieldsOfLawBySearchQuery() {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
    String str = result.getResponseBody();

    assertThat((Integer) JsonPath.read(str, "$.pageable.pageNumber")).isZero();
    assertThat((Integer) JsonPath.read(str, "$.pageable.pageSize")).isEqualTo(3);
    assertThat((Integer) JsonPath.read(str, "$.totalPages")).isEqualTo(2);
    assertThat((Integer) JsonPath.read(str, "$.totalElements")).isEqualTo(5);
    assertThat((Integer) JsonPath.read(str, "$.numberOfElements")).isEqualTo(3);
    assertThat((Boolean) JsonPath.read(str, "$.first")).isTrue();
    assertThat((Boolean) JsonPath.read(str, "$.last")).isFalse();
    List<String> identifiers = JsonPath.read(str, "$.content[*].identifier");
    assertThat(identifiers).containsExactly("FL-04", "FL-02", "FL-01");

    result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-&pg=1&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
    str = result.getResponseBody();

    assertThat((Integer) JsonPath.read(str, "$.pageable.pageNumber")).isEqualTo(1);
    assertThat((Integer) JsonPath.read(str, "$.pageable.pageSize")).isEqualTo(3);
    assertThat((Integer) JsonPath.read(str, "$.totalPages")).isEqualTo(2);
    assertThat((Integer) JsonPath.read(str, "$.totalElements")).isEqualTo(5);
    assertThat((Integer) JsonPath.read(str, "$.numberOfElements")).isEqualTo(2);
    assertThat((Boolean) JsonPath.read(str, "$.first")).isFalse();
    assertThat((Boolean) JsonPath.read(str, "$.last")).isTrue();
    identifiers = JsonPath.read(str, "$.content[*].identifier");
    assertThat(identifiers).containsExactly("FL-03", "FL-01-01");

    result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-&pg=2&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
    str = result.getResponseBody();

    assertThat((Integer) JsonPath.read(str, "$.pageable.pageNumber")).isEqualTo(2);
    assertThat((Integer) JsonPath.read(str, "$.numberOfElements")).isZero();
    identifiers = JsonPath.read(str, "$.content[*].identifier");
    assertThat(identifiers).isEmpty();
  }

  /*@Test
  void testGetFieldsOfLawByNormsQuery() {
    prepareDatabase();

    // TODO
  }

  @Test
  void testGetFieldsOfLawByNormsAndSearchQuery() {
    prepareDatabase();

    // TODO
  }*/

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
    FieldOfLawDTO fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(1L)
            .subjectFieldNumber("FL")
            .isNew(true)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    /*NormDTO normDTO =
        NormDTO.builder()
            .id(1L)
            .subjectFieldId(1L)
            .abbreviation("ABC")
            .singleNormDescription("§ 1234")
            .build();
    normRepository.save(normDTO).block();
    // TODO: add more norms*/

    // child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(2L)
            .isNew(true)
            .subjectFieldNumber("FL-01")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // sub child of the child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(3L)
            .isNew(true)
            .subjectFieldNumber("FL-01-01")
            .parentId(2L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // second root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(4L)
            .isNew(true)
            .subjectFieldNumber("FO")
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // second child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(5L)
            .isNew(true)
            .subjectFieldNumber("FL-02")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // third child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(6L)
            .isNew(true)
            .subjectFieldNumber("FL-03")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // fourth child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(7L)
            .isNew(true)
            .subjectFieldNumber("FL-04")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();
  }
}
