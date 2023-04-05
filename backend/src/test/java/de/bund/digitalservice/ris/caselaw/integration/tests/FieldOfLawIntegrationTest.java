package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
  @Autowired private NormRepository normRepository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
    normRepository.deleteAll().block();
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
    assertThat(identifiers).containsExactly("FL-01-01", "FL-04", "FL-02");

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
    assertThat(identifiers).containsExactly("FL-01", "FL-03");

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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "§ 123", // paragraph
        "§ 123 abc", // paragraph followed by norm
        "§123", // paragraph without whitespace
        "abc", // norm
        "abc § 123", // norm followed by paragraph
        "abc §123", // norm followed by paragraph without whitespace
        "abc § 12", // norm followed by incomplete paragraph
      })
  void testGetFieldsOfLawByNormsQuery(String query) {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"" + query + "\"&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$.content[*].identifier");
    assertThat(identifiers).containsExactly("FL");
  }

  @Test
  void testGetFieldsOfLawByNormsAndSearchQuery() {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"def\" some here&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$.content[*].identifier");
    assertThat(identifiers).containsExactly("FL-01");
  }

  @Test
  void testGetFieldsOfLawByIdentifierSearch() {
    prepareDatabase();

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?searchStr=FL-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$[*].identifier");
    assertThat(identifiers).containsExactly("FL-01", "FL-01-01");
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
    FieldOfLawDTO fieldOfLawDTO =
        FieldOfLawDTO.builder().id(1L).identifier("FL").isNew(true).changeIndicator('N').build();
    repository.save(fieldOfLawDTO).block();

    NormDTO normDTO =
        NormDTO.builder()
            .id(1L)
            .fieldOfLawId(1L)
            .abbreviation("ABC")
            .singleNormDescription("§ 123")
            .build();
    normRepository.save(normDTO).block();

    // child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(2L)
            .isNew(true)
            .identifier("FL-01")
            .text("some text here")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    normDTO =
        NormDTO.builder()
            .id(2L)
            .fieldOfLawId(2L)
            .abbreviation("DEF")
            .singleNormDescription("§ 456")
            .build();
    normRepository.save(normDTO).block();

    // sub child of the child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(3L)
            .isNew(true)
            .identifier("FL-01-01")
            .parentId(2L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // second root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder().id(4L).isNew(true).identifier("FO").changeIndicator('N').build();
    repository.save(fieldOfLawDTO).block();

    // second child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(5L)
            .isNew(true)
            .identifier("FL-02")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // third child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(6L)
            .isNew(true)
            .identifier("FL-03")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();

    // fourth child of the first root child
    fieldOfLawDTO =
        FieldOfLawDTO.builder()
            .id(7L)
            .isNew(true)
            .identifier("FL-04")
            .parentId(1L)
            .changeIndicator('N')
            .build();
    repository.save(fieldOfLawDTO).block();
  }

  @Test
  void testOrderingOfGetFieldsOfLawByNormsAndSearchQuery() {
    String[][] fieldOfLawData = {
      {"AB-01", "Some text here", "§ 123", "abc"},
      {"AB-01-01", "More text also here", "§ 456", "cab"},
      {"CD", "Other text without more", null, null},
      {"CD-01", "Text means writing here", "§ 012", "dab"},
      {"CD-02", "Aber a word starting with ab and text + here", "§ 345", "abx"}
    };

    String searchStr = "norm:\"ab\" AB here text";

    List<String> expectedIdentifiers = Arrays.asList("CD-02", "AB-01", "AB-01-01");
    List<Integer> expectedScores = Arrays.asList(38, 28, 28);

    int normCount = 0;
    for (int i = 0; i < fieldOfLawData.length; i++) {
      String[] fol = fieldOfLawData[i];
      long folId = (long) i + 1;
      FieldOfLawDTO fieldOfLawDTO =
          FieldOfLawDTO.builder().id(folId).isNew(true).identifier(fol[0]).text(fol[1]).build();
      repository.save(fieldOfLawDTO).block();
      if (fol[2] == null) continue;
      NormDTO normDTO =
          NormDTO.builder()
              .id((long) normCount++)
              .fieldOfLawId(folId)
              .singleNormDescription(fol[2])
              .abbreviation(fol[3])
              .build();
      normRepository.save(normDTO).block();
    }

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=" + searchStr + "&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    String str = result.getResponseBody();
    List<String> actualIdentifiers = JsonPath.read(str, "$.content[*].identifier");
    List<Integer> actualScores = JsonPath.read(str, "$.content[*].score");

    assertThat(actualIdentifiers).isEqualTo(expectedIdentifiers);
    assertThat(actualScores).isEqualTo(expectedScores);
  }

  @Test
  void testOrderingOfGetFieldsOfLawByIdentifierSearch() {
    String[] identifier = {"AB-01", "AB-01-01", "CD", "CD-01", "CD-02"};

    String searchStr = "01";

    List<String> expectedIdentifiers = Arrays.asList("AB-01", "CD-01", "AB-01-01");

    for (int i = 0; i < identifier.length; i++) {
      FieldOfLawDTO fieldOfLawDTO =
          FieldOfLawDTO.builder().id((long) i + 1).isNew(true).identifier(identifier[i]).build();
      repository.save(fieldOfLawDTO).block();
    }

    EntityExchangeResult<String> result =
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?searchStr=" + searchStr)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> actualIdentifiers = JsonPath.read(result.getResponseBody(), "$[*].identifier");
    assertThat(actualIdentifiers).isEqualTo(expectedIdentifiers);
  }
}
