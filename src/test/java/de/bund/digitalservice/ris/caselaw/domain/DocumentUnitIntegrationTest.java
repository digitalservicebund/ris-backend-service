package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.DatabasePreviousDecisionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@WebFluxTest(controllers = {DocumentUnitController.class})
@Import({
  DocumentUnitService.class,
  DatabaseDocumentNumberService.class,
  PostgresDocumentUnitRepositoryImpl.class,
  FlywayConfig.class,
  PostgresConfig.class
})
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@WithMockUser
@AutoConfigureDataR2dbc
class DocumentUnitIntegrationTest {
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

  @Autowired private DatabasePreviousDecisionRepository previousDecisionRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @Test
  void testGetDocumentUnit_withPreviousDecisions() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnit = repository.save(documentUnitDTO).block();
    List<PreviousDecisionDTO> previousDecisionDTOs =
        List.of(
            PreviousDecisionDTO.builder().documentUnitId(savedDocumentUnit.getId()).build(),
            PreviousDecisionDTO.builder().documentUnitId(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(previousDecisionDTOs).collectList().block();

    UUID documentUnitUuid2 = UUID.randomUUID();
    documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid2)
            .documentnumber("docnr23456789")
            .creationtimestamp(Instant.now())
            .build();
    savedDocumentUnit = repository.save(documentUnitDTO).block();
    previousDecisionDTOs =
        List.of(
            PreviousDecisionDTO.builder().documentUnitId(savedDocumentUnit.getId()).build(),
            PreviousDecisionDTO.builder().documentUnitId(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(previousDecisionDTOs).collectList().block();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody.uuid()).isEqualTo(documentUnitUuid1);
              assertThat(responseBody.previousDecisions()).hasSize(2);
            });
  }
}
