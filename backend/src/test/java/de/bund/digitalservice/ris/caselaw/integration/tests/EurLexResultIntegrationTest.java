package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.PageTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresEurLexResultRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexController;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.MockEurlexRetrievalService;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      TestConfig.class,
      OAuthService.class,
      KeycloakUserService.class,
      EurLexSOAPSearchService.class,
      PostgresEurLexResultRepositoryImpl.class,
      MockEurlexRetrievalService.class,
    },
    controllers = {EurLexController.class})
@Sql(
    scripts = {"classpath:eurlex_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:eurlex_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings("java:S5976")
class EurLexResultIntegrationTest {
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

  @MockitoSpyBean private UserService userService;

  @MockitoBean private ClientRegistrationRepository registrationRepository;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private DocumentationUnitService documentationUnitService;
  @MockitoBean private ProcedureService procedureService;

  @BeforeEach
  void setUp() {
    mockUserGroups(userGroupService);
  }

  @Test
  void getSearchResults() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults)
        .extracting("celex")
        .containsExactly("62017CA0578", "62017TA0577", "62017CB0576", "62017TB0575");
  }

  @Test
  void getSearchResults_withDocOfficeBGH() {
    doReturn(DocumentationOffice.builder().abbreviation("BGH").build())
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));

    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withDocOfficeBFH() {
    doReturn(DocumentationOffice.builder().abbreviation("BFH").build())
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));

    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017CB0576");
  }

  @Test
  void getSearchResults_withNotAllowedDocOffice() {
    doReturn(DocumentationOffice.builder().abbreviation("NotAllowedDocOffice").build())
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));

    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }

  @Test
  void getSearchResults_withParameterCourtTypeSetToEuG() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?court=EuG")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withParameterCourtTypeSetToEuGH() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?court=EuGH")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017CB0576");
  }

  @Test
  void getSearchResults_withParameterStartDate() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?start-date=2012-01-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017TA0577");
  }

  @Test
  void getSearchResults_withParameterStartDateAndEndDate() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?start-date=2012-01-01&end-date=2013-01-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterCelex() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017TA0577")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterCelexOnlyStartOf() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017T")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withParameterCelexForAnAssignedDecision() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017CA0579")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }

  @Test
  void getSearchResults_withParameterFileNumberExactValue() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=T-88/25")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterFileNumberOnlyStartOf() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=T-88")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterFileNumberForAnAssignedDecision() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=C-12/25")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }
}
