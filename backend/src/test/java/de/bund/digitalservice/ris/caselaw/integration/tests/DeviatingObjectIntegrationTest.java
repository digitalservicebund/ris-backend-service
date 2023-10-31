package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
    },
    controllers = {DocumentUnitController.class})
class DeviatingObjectIntegrationTest {
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
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @BeforeEach
  void setUp() {

    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DigitalService");
                }));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  // Deviating File Number
  @Test
  void testReadOfDeviatingFileNumbers() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingFileNumbers).containsExactlyInAnyOrder("dfn1", "dfn2");
            });
  }

  @Test
  void testAddANewDeviatingFileNumberToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    UUID dfn1Id = savedDTO.getDeviatingFileNumbers().get(0).getId();
    UUID dfn12d = savedDTO.getDeviatingFileNumbers().get(1).getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(3);
              // TODO: ordering by rank
              assertThat(deviatingFileNumbers).containsExactlyInAnyOrder("dfn1", "dfn2", "dfn3");
            });

    DocumentationUnitDTO updatedDocumentationUnitDTO = repository.findById(savedDTO.getId()).get();
    assertThat(updatedDocumentationUnitDTO.getDeviatingFileNumbers())
        .extracting("id")
        .doesNotContain(dfn1Id, dfn12d);
  }

  @Test
  void testAddADeviatingFileNumberTwiceToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingFileNumberFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingFileNumbers(List.of("dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(1);
              assertThat(deviatingFileNumbers).containsExactly("dfn2");
            });
  }

  @Test
  void testRemoveAllDeviatingFileNumberWithAEmplyListFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingFileNumbers(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingFileNumbers() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().value("dfn2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingFileNumbers).containsExactlyInAnyOrder("dfn1", "dfn2");
            });
  }

  // Deviating ECLI

  @Test
  void testReadOfDeviatingECLI() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    repository.save(dto);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingEclis).containsExactlyInAnyOrder("decli1", "decli2");
            });
  }

  @Test
  void testAddANewDeviatingEcliToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    UUID decli1Id = savedDTO.getDeviatingEclis().get(0).getId();
    UUID decli12d = savedDTO.getDeviatingEclis().get(1).getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingEclis(List.of("decli1", "decli2", "decli3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(3);
              // TODO: ordering by rank
              assertThat(deviatingEclis).containsExactlyInAnyOrder("decli1", "decli2", "decli3");
            });

    DocumentationUnitDTO updatedDocumentationUnitDTO = repository.findById(savedDTO.getId()).get();
    assertThat(updatedDocumentationUnitDTO.getDeviatingFileNumbers())
        .extracting("id")
        .doesNotContain(decli1Id, decli12d);
  }

  @Test
  void testAddADeviatingEcliTwiceToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingEclis(List.of("decli1", "decli2", "decli2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingEcliFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingEclis(List.of("decli2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(1);
              assertThat(deviatingEclis).containsExactly("decli2");
            });
  }

  @Test
  void testRemoveAllDeviatingEcliWithAEmplyListFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingEclis(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingEclis() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().value("decli1").build(),
                    DeviatingEcliDTO.builder().value("decli2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingEclis).containsExactly("decli1", "decli2");
            });
  }

  // Deviating Court

  @Test
  void testReadOfDeviatingCourts() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    repository.save(dto);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingCourts).containsExactlyInAnyOrder("dc1", "dc2");
            });
  }

  @Test
  void testAddANewDeviatingCourtToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    UUID dc1Id = savedDTO.getDeviatingCourts().get(0).getId();
    UUID dc12d = savedDTO.getDeviatingCourts().get(1).getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingCourts(List.of("dc1", "dc2", "dc3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(3);
              // TODO: ordering by rank
              assertThat(deviatingCourts).containsExactlyInAnyOrder("dc1", "dc2", "dc3");
            });

    DocumentationUnitDTO updatedDocumentationUnitDTO = repository.findById(savedDTO.getId()).get();
    assertThat(updatedDocumentationUnitDTO.getDeviatingCourts())
        .extracting("id")
        .doesNotContain(dc1Id, dc12d);
  }

  @Test
  void testAddADeviatingCourtTwiceToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingCourtFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingCourts(List.of("dc2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(1);
              assertThat(deviatingCourts).containsExactlyInAnyOrder("dc2");
            });
  }

  @Test
  void testRemoveAllDeviatingCourtWithAEmplyListFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingCourts(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingCourts() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().value("dc1").build(),
                    DeviatingCourtDTO.builder().value("dc2").build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingCourts).containsExactlyInAnyOrder("dc1", "dc2");
            });
  }

  // Deviating Date

  @Test
  void testReadOfDeviatingDates() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    repository.save(dto);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingDates)
                  .containsExactlyInAnyOrder(LocalDate.of(2000, 1, 2), LocalDate.of(2010, 9, 10));
            });
  }

  @Test
  void testAddANewDeviatingDatesToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    UUID dd1Id = savedDTO.getDeviatingDates().get(0).getId();
    UUID dd12d = savedDTO.getDeviatingDates().get(1).getId();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingDecisionDates(
                        List.of(
                            LocalDate.of(2000, 1, 2),
                            LocalDate.of(2010, 9, 10),
                            LocalDate.of(2020, 4, 5)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(3);
              // TODO: ordering by rank
              assertThat(deviatingDates)
                  .containsExactlyInAnyOrder(
                      LocalDate.of(2000, 1, 2),
                      LocalDate.of(2010, 9, 10),
                      LocalDate.of(2020, 4, 5));
            });

    DocumentationUnitDTO updatedDocumentationUnitDTO = repository.findById(savedDTO.getId()).get();
    assertThat(updatedDocumentationUnitDTO.getDeviatingDates())
        .extracting("id")
        .doesNotContain(dd1Id, dd12d);
  }

  @Test
  void testAddADeviatingDateTwiceToAnExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingDecisionDates(
                        List.of(
                            LocalDate.of(2000, 1, 2),
                            LocalDate.of(2010, 9, 10),
                            LocalDate.of(2010, 9, 10)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingDateFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingDecisionDates(List.of(LocalDate.of(2010, 9, 10)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(1);
              assertThat(deviatingDates).containsExactly(LocalDate.of(2010, 9, 10));
            });
  }

  @Test
  void testRemoveAllDeviatingDatesWithAEmplyListFromExistingList() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .deviatingDecisionDates(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeExistingDeviatingDates() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().value(LocalDate.of(2010, 9, 10)).build()))
            .build();

    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("DigitalService").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(2);
              // TODO: ordering by rank
              assertThat(deviatingDates)
                  .containsExactlyInAnyOrder(LocalDate.of(2000, 1, 2), LocalDate.of(2010, 9, 10));
            });
  }
}
