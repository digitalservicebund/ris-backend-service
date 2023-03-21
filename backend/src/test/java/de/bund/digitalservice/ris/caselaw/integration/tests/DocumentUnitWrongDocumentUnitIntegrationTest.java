package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitListEntryRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
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
      PostgresDocumentUnitListEntryRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitWrongDocumentUnitIntegrationTest {
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
  @Autowired private DatabaseProceedingDecisionRepository previousDecisionRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DeviatingEcliRepository deviatingEcliRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll().block();
    deviatingEcliRepository.deleteAll().block();
    previousDecisionRepository.deleteAll().block();
    repository.deleteAll().block();
  }

  @Test
  void testUpdateDocumentUnit_withPreviousDecisionIdsForOtherDocumentUnit() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    UUID proceedingDecisionUuid1 = UUID.randomUUID();
    UUID proceedingDecisionUuid2 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnit = repository.save(documentUnitDTO).block();
    List<ProceedingDecisionDTO> proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .documentnumber("docnr23456789")
            .creationtimestamp(Instant.now())
            .build();
    savedDocumentUnit = repository.save(documentUnitDTO).block();
    proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitUuid1)
            .documentNumber("newdocnumber12")
            .creationtimestamp(Instant.now())
            .proceedingDecisions(
                List.of(
                    ProceedingDecision.builder().uuid(proceedingDecisionUuid1).fileNumber("prev1").build(),
                    ProceedingDecision.builder().uuid(proceedingDecisionUuid2).fileNumber("prev2").build()))
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1)
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .is5xxServerError();

    List<DocumentUnitDTO> documentUnitDTOs = repository.findAll().collectList().block();
    assertThat(documentUnitDTOs).hasSize(2);
    assertThat(documentUnitDTOs)
        .extracting("documentnumber")
        .containsExactly("docnr12345678", "docnr23456789");

    proceedingDecisionDTOS = previousDecisionRepository.findAll().collectList().block();
    assertThat(proceedingDecisionDTOS).hasSize(4);
    assertThat(proceedingDecisionDTOS)
        .extracting("fileNumber")
        .containsExactly(null, null, null, null);
  }
}
