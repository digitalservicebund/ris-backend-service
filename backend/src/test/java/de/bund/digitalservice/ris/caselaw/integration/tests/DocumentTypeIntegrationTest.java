package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentTypeController;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      DocumentTypeService.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      PostgresDocumentTypeRepositoryImpl.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentTypeController.class})
@Sql(
    scripts = {"classpath:document_types.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class DocumentTypeIntegrationTest {
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
  @Autowired private DatabaseDocumentTypeRepository repository;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private DocumentUnitService service;

  @BeforeEach()
  void init() {}

  @AfterEach
  void cleanUp() {}

  @Test
  void testGetAllDocumentTypes() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documenttypes")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> labels = JsonPath.read(result.getResponseBody(), "$[*].jurisShortcut");
    assertThat(labels)
        .containsExactly("Amtsrechtliche Anordnung", "Anordnung", "Beschluss", "Urteil");

    List<String> shortcuts = JsonPath.read(result.getResponseBody(), "$[*].label");
    assertThat(shortcuts).containsExactly("AmA", "Ao", "Bes", "Ur");
  }

  @Test
  void testGetDocumentTypesWithQuery() {
    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documenttypes?q=Anord")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> labels = JsonPath.read(result.getResponseBody(), "$[*].jurisShortcut");
    assertThat(labels).containsExactly("Anordnung", "Amtsrechtliche Anordnung");

    List<String> shortcuts = JsonPath.read(result.getResponseBody(), "$[*].label");
    assertThat(shortcuts).containsExactly("Ao", "AmA");
  }
}
