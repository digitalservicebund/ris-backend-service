package de.bund.digitalservice.ris.caselaw.integration.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com",
      "management.endpoint.health.probes.enabled=true",
      "management.health.livenessState.enabled=true",
      "management.health.readinessState.enabled=true",
      "management.endpoint.health.group.readiness.include=readinessState,db,redis"
    })
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class HealthEndpointIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7.0")).withExposedPorts(6379);

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

  @Autowired WebTestClient webTestClient;

  @Test
  void shouldExposeHealthEndpoint() {
    webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    webTestClient.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
  }

  @Test
  void shouldBeUnhealthyWithoutRedis() {
    redis.stop();
    webTestClient
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    webTestClient.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    webTestClient
        .get()
        .uri("/actuator/health/readiness")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
  }
}
