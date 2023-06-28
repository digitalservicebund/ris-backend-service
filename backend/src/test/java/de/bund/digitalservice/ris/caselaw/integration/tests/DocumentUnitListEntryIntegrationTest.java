package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublishReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      KeycloakUserService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresPublishReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitListEntryIntegrationTest {
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

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseDocumentUnitStatusRepository statusRepository;
  @Autowired private DatabaseDocumentUnitMetadataRepository listEntryRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private DocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
    statusRepository.deleteAll().block();

    docOfficeDTO = documentationOfficeRepository.findByLabel("DigitalService").block();

    doReturn(Mono.just(DocumentationOfficeTransformer.transformDTO(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    DocumentUnitDTO neurisDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .dataSource(DataSource.NEURIS)
                    .documentationOfficeId(docOfficeDTO.getId())
                    .build())
            .block();
    DocumentUnitDTO migrationDto =
        repository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("MIGRATION")
                    .dataSource(DataSource.MIGRATION)
                    .build())
            .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(neurisDto.getId())
                .fileNumber("AkteX")
                .isDeviating(false)
                .build())
        .block();

    fileNumberRepository
        .save(
            FileNumberDTO.builder()
                .documentUnitId(migrationDto.getId())
                .fileNumber("AkteM")
                .isDeviating(false)
                .build())
        .block();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits?pg=0&sz=3")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content[0].documentNumber")
        .isEqualTo("1234567890123")
        .jsonPath("$.content[0].uuid")
        .isEqualTo(neurisDto.getUuid().toString())
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteX")
        .jsonPath("$.content[0].documentationOffice.label")
        .isEqualTo("DigitalService")
        .jsonPath("$.content[0].status")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.totalElements")
        .isEqualTo(1);
  }

  @Test
  void testForCorrectOrdering() {
    List<Instant> timestampsExpected = new ArrayList<>();
    for (int i = 0; i < 11; i++) {
      timestampsExpected.add(Instant.now().minus(i, ChronoUnit.DAYS));
    }
    Collections.shuffle(timestampsExpected);

    for (int i = 0; i < 11; i++) {
      repository
          .save(
              DocumentUnitDTO.builder()
                  .uuid(UUID.randomUUID())
                  .creationtimestamp(timestampsExpected.get(i))
                  .documentnumber("123456789012" + i)
                  .dataSource(DataSource.NEURIS)
                  .documentationOfficeId(docOfficeDTO.getId())
                  .build())
          .block();
    }

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> timestampActualStrings =
        JsonPath.read(result.getResponseBody(), "$.content[*].creationTimestamp");
    List<Instant> timestampsActual = timestampActualStrings.stream().map(Instant::parse).toList();
    assertThat(timestampsActual).hasSize(10);

    for (int i = 0; i < timestampsActual.size() - 1; i++) {
      Instant tThis = timestampsActual.get(i);
      Instant tNext = timestampsActual.get(i + 1);
      assertThat(tThis).isAfter(tNext);
    }
  }

  @Test
  void testForCorrectPagination() {
    List<DocumentUnitDTO> documents =
        IntStream.range(0, 99)
            .mapToObj(
                i ->
                    DocumentUnitDTO.builder()
                        .uuid(UUID.randomUUID())
                        .creationtimestamp(Instant.now())
                        .documentnumber("123456780" + i)
                        .dataSource(DataSource.NEURIS)
                        .documentationOfficeId(docOfficeDTO.getId())
                        .build())
            .collect(Collectors.toList());

    repository.saveAll(documents).blockLast();

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits?pg=0&sz=1")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    Integer totalElements = JsonPath.read(result.getResponseBody(), "$.totalElements");
    assertThat(totalElements).isEqualTo(99);
  }
}
