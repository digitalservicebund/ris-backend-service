package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.jayway.jsonpath.JsonPath;
import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentNumberGeneratorService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentationUnitSearchIntegrationTest {
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

    doReturn(Mono.just(DocumentationOfficeTransformer.transformToDomain(docOfficeDTO)))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    DocumentationUnitDTO migrationDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .id(UUID.randomUUID())
                .documentNumber("MIGR202200012")
                .documentationOffice(docOfficeDTO)
                .build());
    // TODO can't the file number be set in the first save()?
    migrationDto =
        repository.save(
            migrationDto.toBuilder()
                .fileNumbers(
                    List.of(
                        FileNumberDTO.builder()
                            .value("AkteM")
                            .documentationUnit(migrationDto)
                            .rank(0L)
                            .build()))
                .build());
    DocumentationUnitDTO newNeurisDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("NEUR202300008")
                .documentationOffice(docOfficeDTO)
                .build());
    newNeurisDto =
        repository.save(
            newNeurisDto.toBuilder()
                .fileNumbers(
                    List.of(
                        FileNumberDTO.builder()
                            .value("AkteY")
                            .documentationUnit(newNeurisDto)
                            .rank(0L)
                            .build()))
                .build());

    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(migrationDto)
            .publicationStatus(PublicationStatus.PUBLISHED)
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
        .isEqualTo("MIGR202200012")
        .jsonPath("$.content[1].documentNumber")
        .isEqualTo("NEUR202300008")
        .jsonPath("$.content[0].fileNumber")
        .isEqualTo("AkteM")
        .jsonPath("$.content[1].fileNumber")
        .isEqualTo("AkteY")
        .jsonPath("$.content[0].status.publicationStatus")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.content[1].status.publicationStatus")
        .isEqualTo("PUBLISHED")
        .jsonPath("$.numberOfElements")
        .isEqualTo(2);
  }

  @Test
  void testOrderedByDocumentNumber() {
    List<String> documentNumbers = Arrays.asList("EFGH202200123", "IJKL202300099", "ABCD202300007");

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
        .containsExactly("ABCD202300007", "EFGH202200123", "IJKL202300099");
  }

  @Test
  void testForCorrectPagination() {
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("1234567801")
            .documentationOffice(docOfficeDTO)
            .build());
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("1234567802")
            .documentationOffice(docOfficeDTO)
            .build());

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

    boolean last = JsonPath.read(result.getResponseBody(), "$.last");
    assertThat(last).isFalse();
  }

  @Test
  void testForCompleteResultListWhenSearchingForFileNumberOrDocumentNumber() {
    for (int i = 0; i < 10; i++) {
      DocumentationUnitDTO doc =
          repository.save(
              DocumentationUnitDTO.builder()
                  // index 0-4 get a "AB" docNumber
                  .documentNumber((i <= 4 ? "AB" : "GE") + "123456780" + i)
                  .documentationOffice(docOfficeDTO)
                  .build());

      repository.save(
          doc.toBuilder()
              .fileNumbers(
                  // even indices get a fileNumber
                  i % 2 == 1
                      ? List.of()
                      : List.of(
                          FileNumberDTO.builder()
                              .value("AB 34/" + i)
                              .documentationUnit(doc)
                              .rank(0L)
                              .build()))
              // index 4+ get a deviating fileNumber
              .deviatingFileNumbers(
                  i < 4
                      ? List.of()
                      : List.of(
                          DeviatingFileNumberDTO.builder()
                              .value("ABC 34/" + i)
                              .documentationUnit(doc)
                              .rank(0L)
                              .build()))
              .build());
    }

    EntityExchangeResult<String> resultPage1 =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=0&sz=4&documentNumberOrFileNumber=" + "AB")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    EntityExchangeResult<String> resultPage2 =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=1&sz=4&documentNumberOrFileNumber=" + "AB")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    EntityExchangeResult<String> resultPage3 =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=2&sz=4&documentNumberOrFileNumber=" + "AB")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    // expect first page...
    // to have 4 results
    String responseBodyFirstPage = resultPage1.getResponseBody();
    assertThat((int) JsonPath.read(responseBodyFirstPage, "$.numberOfElements")).isEqualTo(4);
    // not to be the last page
    assertThat((boolean) JsonPath.read(responseBodyFirstPage, "$.first")).isTrue();
    assertThat((boolean) JsonPath.read(responseBodyFirstPage, "$.last")).isFalse();
    // to have docNumbers "AB1234567800", "AB1234567801", "AB1234567802", "AB1234567803"
    List<String> documentNumbers1Actual =
        JsonPath.read(responseBodyFirstPage, "$.content[*].documentNumber");
    assertThat(documentNumbers1Actual)
        .hasSize(4)
        .containsExactly("AB1234567800", "AB1234567801", "AB1234567802", "AB1234567803");

    // expect second page...
    String responseBodySecondPage = resultPage2.getResponseBody();
    // to have 4 results
    assertThat((int) JsonPath.read(responseBodySecondPage, "$.numberOfElements")).isEqualTo(4);
    // not to be the last or first page
    assertThat((boolean) JsonPath.read(responseBodySecondPage, "$.first")).isFalse();
    assertThat((boolean) JsonPath.read(responseBodySecondPage, "$.last")).isFalse();
    // to have docNumbers "AB1234567804", "GE1234567805", "GE1234567806", "GE1234567807"
    List<String> documentNumbers2Actual =
        JsonPath.read(responseBodySecondPage, "$.content[*].documentNumber");
    assertThat(documentNumbers2Actual)
        .hasSize(4)
        .containsExactly("AB1234567804", "GE1234567805", "GE1234567806", "GE1234567807");

    // expect third page...
    String responseBodyThirdPage = resultPage3.getResponseBody();
    // to have 2 results
    assertThat((int) JsonPath.read(responseBodyThirdPage, "$.numberOfElements")).isEqualTo(2);
    // not to be the first but to be the last page
    assertThat((boolean) JsonPath.read(responseBodyThirdPage, "$.first")).isFalse();
    assertThat((boolean) JsonPath.read(responseBodyThirdPage, "$.last")).isTrue();
    // to have docNumbers "GE1234567808", "GE1234567809"
    List<String> documentNumbers3Actual =
        JsonPath.read(responseBodyThirdPage, "$.content[*].documentNumber");
    assertThat(documentNumbers3Actual).hasSize(2).containsExactly("GE1234567808", "GE1234567809");
  }

  @Test
  void testTrim() {
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("AB1234567802")
            .documentationOffice(docOfficeDTO)
            .build());

    EntityExchangeResult<String> result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(
                "/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumberOrFileNumber=+++AB++")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    List<String> documentNumbersActual =
        JsonPath.read(result.getResponseBody(), "$.content[*].documentNumber");
    assertThat(documentNumbersActual).hasSize(1).containsExactly("AB1234567802");
  }
}
