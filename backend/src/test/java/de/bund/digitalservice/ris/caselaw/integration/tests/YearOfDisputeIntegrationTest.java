package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class YearOfDisputeIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

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

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private AttachmentService attachmentService;
  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testDuplicatedYearsAreNotAllowed() {

    String documentNumber = "1234567890123";
    List<Year> years = List.of(Year.now(), Year.now());
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber(documentNumber)
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo(documentNumber);
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(1);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .isEqualTo(Year.now().toString());
            });
  }

  @Test
  void testYearsSorting() {
    String documentNumber = "1234567890123";

    var firstYear = Year.parse("2022");
    var secondYear = Year.parse("2010");
    var lastYear = Year.parse("2030");

    List<Year> years = List.of(firstYear, secondYear, lastYear);
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .documentNumber(documentNumber)
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo(documentNumber);
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(3);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .isEqualTo(firstYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(1).toString())
                  .isEqualTo(secondYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(2).toString())
                  .isEqualTo(lastYear.toString());
            });
  }

  @Test
  void testYearIsNotSorting() {
    String documentNumber = "1234567890123";

    var firstYear = Year.parse("2022");
    var secondYear = Year.parse("2010");
    var lastYear = Year.parse("2030");

    List<Year> years = List.of(firstYear, secondYear, lastYear);
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .documentNumber(documentNumber)
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo(documentNumber);
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(3);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .isEqualTo(firstYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(1).toString())
                  .isEqualTo(secondYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(2).toString())
                  .isEqualTo(lastYear.toString());
            });
  }

  @Test
  void testFutureYearsAreNotAllowed() {
    String documentNumber = "1234567890123";

    var currentYear = Year.now();
    var futureYear = (currentYear.plusYears(1));

    List<Year> years = List.of(currentYear, futureYear);
    DocumentationUnitDTO dto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeRepository.findByAbbreviation("DS"))
                .documentNumber(documentNumber)
                .build());

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo(documentNumber);
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(1);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .isEqualTo(currentYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .isNotEqualTo(futureYear.toString());
            });
  }
}
