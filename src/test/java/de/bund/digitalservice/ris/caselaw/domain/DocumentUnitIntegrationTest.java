package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
  DocumentUnitRepositoryImpl.class,
  FlywayConfig.class,
  PostgresConfig.class
})
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@WithMockUser
@AutoConfigureDataR2dbc
// This is because retryWhen(Retry.backoff... in DocumentUnitService can take up to ~127 seconds
@AutoConfigureWebTestClient(timeout = "150000")
public class DocumentUnitIntegrationTest {
  @Container
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;

  @Autowired private WebTestClient webClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private FileNumberRepository fileNumberRepository;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
  }

  @Test
  void testForCorrectDbEntryAfterNewDocumentUnitCreation() {
    DocumentUnitCreationInfo info = new DocumentUnitCreationInfo("ABC", "D");

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/caselaw/documentunits/")
        .bodyValue(info)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("ABCD");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).startsWith("ABCD");
  }

  @Test
  void testForCorrectDbEntriesAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .id(savedDto.id)
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(CoreData.builder().fileNumbers(List.of("AkteX")).build())
            .texts(Texts.builder().decisionName("decisionName").build())
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().fileNumbers().get(0))
                  .isEqualTo("AkteX");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<FileNumberDTO> fileNumberEntries =
        fileNumberRepository.findAllByDocumentUnitId(list.get(0).id).collectList().block();
    assertThat(fileNumberEntries).hasSize(1);
    assertThat(fileNumberEntries.get(0).getFileNumber()).isEqualTo("AkteX");
  }
}
