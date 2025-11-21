package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserApi;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Import({TestConfig.class})
@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.flyway.locations=classpath:db/migration",
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com",
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.recipientAddress=neuris@example.com",
      "management.endpoint.health.probes.enabled=true",
      "management.health.livenessState.enabled=true",
      "management.health.readinessState.enabled=true",
      "management.endpoint.health.group.readiness.include=readinessState,db,redis",
      "spring.security.oauth2.client.provider.keycloak.issuer-uri=localhost",
    })
@AutoConfigureMockMvc
@Tag("integration")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
    scripts = {"classpath:doc_office_init.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIntegrationTest {

  static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer("postgres:14").withInitScript("init_db.sql").withReuse(true);

  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7.0"))
          .withExposedPorts(6379)
          .withReuse(true);

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());

    registry.add("spring.data.redis.host", () -> redis.getHost());
    registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    registry.add("spring.data.redis.timeout", () -> "200");
  }

  @MockitoBean FeatureToggleService featureToggleService;
  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean UserGroupService userGroupService;
  @MockitoBean UserApiService userApiService;

  static DataSource dataSource;

  @Autowired
  void setDataSource(DataSource dataSource) {
    BaseIntegrationTest.dataSource = dataSource;
  }

  @BeforeAll
  public static void baseBeforeAll() {
    postgreSQLContainer.start();
    redis.start();
  }

  @BeforeEach
  void baseBeforeEach() {
    // Replace with test configuration instead of mocking difficult because doc offices are not
    // available on startup when needed by user group service
    mockUserGroups(userGroupService);
    mockUserApi(userApiService);
  }

  @AfterEach
  void baseAfterEach() {
    Mockito.reset(featureToggleService);
  }

  /**
   * We cannot use @Sql with AFTER_TEST_CLASS because it will be overwritten by individual tests
   * that have their own @Sql annotations.
   */
  @AfterAll
  static void baseAfterAll() throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(conn, new ClassPathResource("documentation_unit_cleanup.sql"));
    }
  }
}
