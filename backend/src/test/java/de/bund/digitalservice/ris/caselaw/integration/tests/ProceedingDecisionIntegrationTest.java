package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.ProceedingDecisionController;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionLinkDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class
    },
    controllers = {ProceedingDecisionController.class, DocumentUnitController.class})
public class ProceedingDecisionIntegrationTest {

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
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseProceedingDecisionLinkRepository linkRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    repository.deleteAll().block();
    linkRepository.deleteAll().block();
  }

  @Test
  void testRemoveProceedingDecisionLink() {
    UUID parentUuid = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(parentUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .dataSource(DataSource.NEURIS)
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO).block();

    UUID childUuid = UUID.randomUUID();
    DocumentUnitDTO childDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(childUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("abcdefghjikl")
            .dataSource(DataSource.NEURIS)
            .build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO).block();

    ProceedingDecisionLinkDTO linkDTO =
        ProceedingDecisionLinkDTO.builder()
            .parentDocumentUnitId(parentDocumentUnitDTO.getId())
            .childDocumentUnitId(childDocumentUnitDTO.getId())
            .build();
    linkDTO = linkRepository.save(linkDTO).block();
    assertThat(linkDTO).isNotNull();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions/" + childUuid)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(linkRepository.findById(linkDTO.getId()).block()).isNull();
    assertThat(repository.findById(childDocumentUnitDTO.getId()).block()).isNotNull();
  }

  @Test
  void testRemoveProceedingDecisionLinkAndDeleteOrphanedDocumentUnit() {
    UUID parentUuid = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(parentUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .dataSource(DataSource.NEURIS)
            .build();
    parentDocumentUnitDTO = repository.save(parentDocumentUnitDTO).block();

    UUID childUuid = UUID.randomUUID();
    DocumentUnitDTO childDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(childUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("abcdefghjikl")
            .dataSource(DataSource.PROCEEDING_DECISION)
            .build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO).block();

    ProceedingDecisionLinkDTO linkDTO =
        ProceedingDecisionLinkDTO.builder()
            .parentDocumentUnitId(parentDocumentUnitDTO.getId())
            .childDocumentUnitId(childDocumentUnitDTO.getId())
            .build();
    linkDTO = linkRepository.save(linkDTO).block();
    assertThat(linkDTO).isNotNull();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions/" + childUuid)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(linkRepository.findById(linkDTO.getId()).block()).isNull();
    assertThat(repository.findById(childDocumentUnitDTO.getId()).block()).isNull();
  }
}
