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
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import java.time.Instant;
import java.util.List;
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
    linkRepository.deleteAll().block();
    repository.deleteAll().block();
  }

  @Test
  void testAddProceedingDecisionLink() {
    UUID parentUuid = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(parentUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890")
            .dataSource(DataSource.NEURIS)
            .build();
    repository.save(parentDocumentUnitDTO).block();

    ProceedingDecision proceedingDecision =
        ProceedingDecision.builder().dataSource(DataSource.PROCEEDING_DECISION).build();

    assertThat(
            linkRepository
                .findAllByParentDocumentUnitId(parentDocumentUnitDTO.getId())
                .collectList()
                .block())
        .isEmpty();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions")
        .bodyValue(proceedingDecision)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(
            linkRepository
                .findAllByParentDocumentUnitId(parentDocumentUnitDTO.getId())
                .collectList()
                .block())
        .hasSize(1);

    List<Long> childUuids =
        linkRepository
            .findAllByParentDocumentUnitId(parentDocumentUnitDTO.getId())
            .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId)
            .collectList()
            .block();

    childUuids.stream()
        .map(childUuid -> assertThat(repository.findById(childUuid).block()).isNotNull());
  }

  @Test
  void testAddProceedingDecisionLink_alsoAppendsPreviousDecisionsToDocumentUnit() {
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

    DocumentUnitDTO finalParentDocumentUnitDTO = parentDocumentUnitDTO;
    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody.proceedingDecisions()).hasSize(1);
            });
  }

  @Test
  void testLinkExistingProceedingDecision() {
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

    assertThat(
            linkRepository
                .findByParentDocumentUnitIdAndChildDocumentUnitId(
                    parentDocumentUnitDTO.getId(), childDocumentUnitDTO.getId())
                .block())
        .isNull();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions/" + childUuid)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(
            linkRepository
                .findByParentDocumentUnitIdAndChildDocumentUnitId(
                    parentDocumentUnitDTO.getId(), childDocumentUnitDTO.getId())
                .block())
        .isNotNull();
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
  void testRemoveProceedingDecisionLink_alsoRemovesProceedingDecisionFromDocumentUnit() {
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

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody.proceedingDecisions()).isEmpty();
            });
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

  @Test
  void testRemoveProceedingDecisionLinkAndKeepLinkedProceedingDecision() {
    UUID parentUuid1 = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO1 =
        DocumentUnitDTO.builder()
            .uuid(parentUuid1)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .dataSource(DataSource.NEURIS)
            .build();
    parentDocumentUnitDTO1 = repository.save(parentDocumentUnitDTO1).block();

    UUID parentUuid2 = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO2 =
        DocumentUnitDTO.builder()
            .uuid(parentUuid2)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890124")
            .dataSource(DataSource.NEURIS)
            .build();
    parentDocumentUnitDTO2 = repository.save(parentDocumentUnitDTO2).block();

    UUID childUuid = UUID.randomUUID();
    DocumentUnitDTO childDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(childUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("abcdefghjikl")
            .dataSource(DataSource.PROCEEDING_DECISION)
            .build();
    childDocumentUnitDTO = repository.save(childDocumentUnitDTO).block();

    ProceedingDecisionLinkDTO linkDTO1 =
        ProceedingDecisionLinkDTO.builder()
            .parentDocumentUnitId(parentDocumentUnitDTO1.getId())
            .childDocumentUnitId(childDocumentUnitDTO.getId())
            .build();
    linkDTO1 = linkRepository.save(linkDTO1).block();
    assertThat(linkDTO1).isNotNull();

    ProceedingDecisionLinkDTO linkDTO2 =
        ProceedingDecisionLinkDTO.builder()
            .parentDocumentUnitId(parentDocumentUnitDTO2.getId())
            .childDocumentUnitId(childDocumentUnitDTO.getId())
            .build();
    linkDTO2 = linkRepository.save(linkDTO2).block();
    assertThat(linkDTO2).isNotNull();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid1 + "/proceedingdecisions/" + childUuid)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(linkRepository.findById(linkDTO1.getId()).block()).isNull();
    assertThat(linkRepository.findById(linkDTO2.getId()).block()).isNotNull();
    assertThat(repository.findById(childDocumentUnitDTO.getId()).block()).isNotNull();
  }

  @Test
  void testRemoveNonExistingProceedingDecisionLink() {
    UUID parentUuid = UUID.randomUUID();
    DocumentUnitDTO parentDocumentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(parentUuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .dataSource(DataSource.NEURIS)
            .build();
    repository.save(parentDocumentUnitDTO).block();

    webClient
        .mutateWith(csrf())
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions/" + "invalidUUID")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void testLinkTwoExistingDocumentUnits() {
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

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + parentUuid + "/proceedingdecisions/" + childUuid)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().proceedingDecisions().size()).isEqualTo(1);
              assertThat(response.getResponseBody().proceedingDecisions().get(0).uuid())
                  .isEqualTo(childUuid);
            });

    List<ProceedingDecisionLinkDTO> list = linkRepository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getParentDocumentUnitId()).isEqualTo(parentDocumentUnitDTO.getId());
    assertThat(list.get(0).getChildDocumentUnitId()).isEqualTo(childDocumentUnitDTO.getId());
  }
}
