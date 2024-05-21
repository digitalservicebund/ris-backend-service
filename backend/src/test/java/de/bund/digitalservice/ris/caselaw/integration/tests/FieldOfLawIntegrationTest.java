package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      FieldOfLawService.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      PostgresFieldOfLawRepositoryImpl.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {FieldOfLawController.class})
@Sql(
    scripts = {"classpath:fields_of_law_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:fields_of_law_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class FieldOfLawIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseFieldOfLawRepository repository;

  @MockBean private UserService userService;
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private DocumentUnitService service;

  @BeforeEach()
  void init() {}

  @AfterEach
  void cleanUp() {}

  @Test
  void testGetAllFieldsOfLaw() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?pg=0&sz=20")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers)
        .containsExactly(
            "AB-01",
            "AB-01-01",
            "CD",
            "CD-01",
            "CD-02",
            "FL",
            "FL-01",
            "FL-01-01",
            "FL-02",
            "FL-03",
            "FL-04",
            "FO");
  }

  @Test
  void testGetFieldsOfLawBySearchQuery() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-01&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("FL-01", "FL-01-01");
  }

  // TODO: Do we integrate rank or this test is not relevant?
  @Test
  @Disabled("enable it after order by rank")
  void testPaginationInGetFieldsOfLawBySearchQuery() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL-&pg=0&sz=10")
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
    // TODO: order by rank
    assertThat(identifiers).containsExactlyInAnyOrder("FL-01-01", "FL-04", "FL-02");

    result =
        risWebTestClient
            .withDefaultLogin()
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
        risWebTestClient
            .withDefaultLogin()
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

  @Test
  void testGetFieldsOfLawByNormsQuery_OnlyNormText() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"abc\"&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactlyInAnyOrder("FL", "AB-01");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "§ 123", // paragraph
        "§123", // paragraph without whitespace
      })
  void testGetFieldsOfLawByNormsQuery_withoutAbbreviation(String query) {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"" + query + "\"&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("AB-01", "AB-01-01", "CD-02");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "§ 123 abc", // paragraph followed by norm
        "abc", // norm
      })
  void testGetFieldsOfLawByNormsQuery_withAbbreviation(String query) {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"" + query + "\"&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("AB-01", "FL");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "abc § 123", // norm followed by paragraph
        "abc §123", // norm followed by paragraph without whitespace
        "abc § 12", // norm followed by incomplete paragraph
      })
  void testGetFieldsOfLawByNormsQuery_withStartingAbbreviation(String query) {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"" + query + "\"&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("FL");
  }

  @Test
  void testGetFieldsOfLawByNormsAndSearchQuery() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=norm:\"def\" fl-01&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("FL-01");
  }

  @Test
  void testGetFieldsOfLawByIdentifierSearch() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?q=FL-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers = JsonPath.read(result.getResponseBody(), "$[*].identifier");
    assertThat(identifiers).containsExactly("FL-01", "FL-01-01");
  }

  @Test
  void testGetParentlessChildrenForFieldOfLawByNorms() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/root/children")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw[].class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactlyInAnyOrder("AB-01", "CD", "FL", "FO"));
  }

  @Test
  void testGetChildrenForFieldOfLawNumber() {
    // TODO: order by rank
    risWebTestClient
        .withDefaultLogin()
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
                    .containsExactlyInAnyOrder("FL-01", "FL-02", "FL-03", "FL" + "-04"));
  }

  @Test
  void testGetParentForFieldOfLaw() {
    risWebTestClient
        .withDefaultLogin()
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

  // TODO: Is this test still relevant? It is disabled.
  @Test
  @Disabled(
      "wrong test, syntax incorrect, logic replaced in refactoring, have to redesign in a "
          + "later iteration")
  void testOrderingOfGetFieldsOfLawByNormsAndSearchQuery() {
    String searchStr = "norm:\"§ 123 ab\" AB here text";

    List<String> expectedIdentifiers = Arrays.asList("CD-02", "AB-01", "AB-01-01");
    List<Integer> expectedScores = Arrays.asList(38, 28, 28);

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
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
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?q=fl")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> actualIdentifiers = JsonPath.read(result.getResponseBody(), "$[*].identifier");
    // TODO: test order by score
    assertThat(actualIdentifiers)
        .containsExactlyInAnyOrder("FL", "FL-01", "FL-01-01", "FL-02", "FL-03", "FL-04");
  }

  @Test
  void testFindByMultipleSearchTerms() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=FL+multiple&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers).containsExactly("FL-01-01");
  }

  @Test
  void testFindByEmptySearchTerms() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> identifiers =
        JsonPath.read(result.getResponseBody(), "$.content[*]" + ".identifier");
    assertThat(identifiers)
        .containsExactly(
            "AB-01",
            "AB-01-01",
            "CD",
            "CD-01",
            "CD-02",
            "FL",
            "FL-01",
            "FL-01-01",
            "FL-02",
            "FL-03");
  }
}
