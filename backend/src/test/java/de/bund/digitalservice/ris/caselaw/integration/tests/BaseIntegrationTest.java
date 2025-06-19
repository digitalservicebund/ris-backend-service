package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.UserGroupsTestConfig;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Import({TestConfig.class, UserGroupsTestConfig.class})
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.flyway.locations=classpath:db/migration",
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com",
      "management.endpoint.health.probes.enabled=true",
      "management.health.livenessState.enabled=true",
      "management.health.readinessState.enabled=true",
      "management.endpoint.health.group.readiness.include=readinessState,db,redis",
      "spring.security.oauth2.client.provider.keycloak.issuer-uri=localhost",
    })
@AutoConfigureMockMvc
@Tag("integration")
@Sql(
    scripts = {"classpath:doc_office_init.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIntegrationTest {

  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql").withReuse(true);

  @Autowired MockMvc mockMvc;
  @MockitoBean FeatureToggleService featureToggleService;

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

  @MockitoBean ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private UserGroupService userGroupService;

  @BeforeAll
  public static void beforeAll() {
    postgreSQLContainer.start();
    redis.start();
  }

  @BeforeEach
  void baseBeforeEach() {
    // Replace with test configuration instead of mocking difficult because doc offices are not
    // available on startup when needed by user group service
    mockUserGroups(userGroupService);
  }
}
