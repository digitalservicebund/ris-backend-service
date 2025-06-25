package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      FieldOfLawService.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      PostgresFieldOfLawRepositoryImpl.class,
      SecurityConfig.class,
      OAuthService.class,
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

  @MockitoBean private UserService userService;
  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private DocumentationUnitService service;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private ProcedureService procedureService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  @Test
  void testGetAllFieldsOfLaw() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?pg=0&sz=20")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("identifier")
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
  void testGetFieldsOfLawByIdentifier() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?identifier=FL-01&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01", "FL-01-01");
  }

  @Test
  void testGetFieldsOfLawBySearchTerms() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=other text&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("CD");
  }

  @Test
  void testGetFieldsOfLawByNormsQuery_OnlyNormText() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=abc&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactlyInAnyOrder("FL", "AB-01");
  }

  @ParameterizedTest
  @ValueSource(strings = {"aber hallo § 123", "aber", "ABER HALLO"})
  void testGetFieldsOfLawWithExactSearch_OnlyNormText(String input) {
    String quotedString = "\"" + input + "\""; // wrap in quotes for exact match
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + quotedString + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactlyInAnyOrder("CD-02");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "§ 123", // paragraph
        "§123", // paragraph without whitespace
      })
  void testGetFieldsOfLawByNormsQuery_withoutAbbreviation(String query) {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + query + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("AB-01", "AB-01-01", "CD-02");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "abc § 123", // norm followed by paragraph
        "abc §123", // norm followed by paragraph without whitespace
        "abc § 12", // norm followed by incomplete paragraph
        "§ 123 abc", // paragraph followed by norm
        "§ 12 abc", // incomplete paragraph followed by norm
        "abc", // norm
      })
  void testGetFieldsOfLawByNormsQuery_withAbbreviation(String query) {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + query + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("AB-01", "FL");
  }

  @Test
  void testGetFieldsOfLawByNormAndIdentifier() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=def&identifier=fl&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01");
  }

  @Test
  void testFindByIdentifierAndDescription() {
    SliceTestImpl<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?identifier=FL&q=multiple cats&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01-01");
  }

  @Test
  void testFindByDescriptionAndNorm() {
    SliceTestImpl<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=some text&norm=§123&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("AB-01");
  }

  @Test
  void testGetFieldsOfLawByIdentifierSearch() {
    List<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?q=FL-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01", "FL-01-01");
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
        .expectBody(new TypeReference<List<FieldOfLaw>>() {})
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
        .expectBody(new TypeReference<List<FieldOfLaw>>() {})
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
}
