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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitListEntryIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14")
          .withInitScript("db/create_migration_scheme_and_extensions.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @Autowired private DatabaseStatusRepository statusRepository;

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

  private DocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");

    doReturn(Mono.just(DocumentationOfficeTransformer.transformDTO(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  @Disabled
  void testForCorrectResponseWhenRequestingAll() {
    DocumentationUnitDTO migrationDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .id(UUID.randomUUID())
                .documentNumber("MIGR202200012")
                .documentationOffice(docOfficeDTO)
                .build());
    DocumentationUnitDTO newNeurisDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("NEUR202300008")
                .documentationOffice(docOfficeDTO)
                .build());

    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(migrationDto)
            .publicationStatus(PublicationStatus.JURIS_PUBLISHED)
            .build());
    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(newNeurisDto)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .build());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=3")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.content")
        .isArray()
        .jsonPath("$.content[0].documentNumber")
        .isEqualTo("NEUR202300008")
        .jsonPath("$.content[1].documentNumber")
        .isEqualTo("MIGR202200012")
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteY")
        .jsonPath("$.content[1].fileNumber")
        .isEqualTo("AkteM")
        .jsonPath("$.content[0].status.publicationStatus")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.content[1].status.publicationStatus")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.totalElements")
        .isEqualTo(2);
  }

  @Test
  @Disabled
  void testForCorrectOrdering() {
    List<String> documentNumbers = Arrays.asList("ABCD202300007", "EFGH202200123", "IJKL202300099");

    for (String documentNumber : documentNumbers) {
      repository.save(
          DocumentationUnitDTO.builder()
              .documentNumber(documentNumber)
              .documentationOffice(docOfficeDTO)
              .build());
    }

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> documentNumbersActual =
        JsonPath.read(result.getResponseBody(), "$.content[*].documentNumber");
    assertThat(documentNumbersActual)
        .hasSize(3)
        .containsExactly("IJKL202300099", "EFGH202200123", "ABCD202300007");
  }

  @Test
  @Disabled
  void testForCorrectPagination() {
    List<DocumentationUnitDTO> documents =
        IntStream.range(0, 99)
            .mapToObj(
                i ->
                    DocumentationUnitDTO.builder()
                        .documentNumber("123456780" + i)
                        .documentationOffice(docOfficeDTO)
                        .build())
            .collect(Collectors.toList());

    repository.saveAll(documents);

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=1")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    Integer totalElements = JsonPath.read(result.getResponseBody(), "$.totalElements");
    assertThat(totalElements).isEqualTo(99);
  }
}
