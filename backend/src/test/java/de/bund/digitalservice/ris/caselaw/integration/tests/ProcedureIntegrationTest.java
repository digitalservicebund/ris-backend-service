package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.ProcedureController;
import de.bund.digitalservice.ris.caselaw.adapter.ProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
      KeycloakUserService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      ProcedureService.class
    },
    controllers = {DocumentUnitController.class, ProcedureController.class})
class ProcedureIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:12").withInitScript("db/create_extension.sql");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentUnitRepository documentUnitRepository;
  @Autowired private JPADocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private JPAProcedureRepository repository;
  @Autowired private JPAProcedureLinkRepository linkRepository;

  @MockBean private DocumentNumberService numberService;
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private PublicationReportRepository publicationReportRepository;
  @MockBean private UserService userService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private JPADocumentationOfficeDTO documentationOfficeDTO;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO = documentationOfficeRepository.findByLabel(docOffice.label());
    doReturn(Mono.just(docOffice)).when(userService).getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    linkRepository.deleteAll();
    documentUnitRepository.deleteAll().block();
    repository.deleteAll();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "testProcedure",
        "123456",
        "asd 123",
        "äÜö.123!",
        "thisIsALongLongLongLongLongLongLongProcedureName"
      })
  void testAddingNewProcedure(String label) {
    DocumentUnitDTO dto =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .documentationOfficeId(documentationOfficeDTO.getId())
                    .build())
            .block();

    Procedure procedure = Procedure.builder().label(label).build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure));

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findAll().get(0).getLabel()).isEqualTo(label);
    assertThat(repository.findAll().get(0).getDocumentationOffice().getLabel())
        .isEqualTo(docOffice.label());
  }

  @Test
  void testAddSameProcedure() {
    JPAProcedureDTO procedureDTO = createProcedure("testProcedure", documentationOfficeDTO);
    assertThat(repository.findAll()).hasSize(1);

    DocumentUnitDTO dto =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .documentationOfficeId(documentationOfficeDTO.getId())
                    .build())
            .block();

    Procedure procedure = Procedure.builder().label("testProcedure").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure));

    assertThat(repository.findAll()).hasSize(1);
    assertThat(
            linkRepository
                .findFirstByDocumentationUnitIdOrderByCreatedAtDesc(documentUnitFromFrontend.uuid())
                .getProcedureDTO()
                .getId())
        .isEqualTo(procedureDTO.getId());
    assertThat(linkRepository.findAll()).hasSize(1);
  }

  @Test
  void testAddingProcedureToPreviousProcedures() {
    DocumentUnitDTO dto =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .documentationOfficeId(documentationOfficeDTO.getId())
                    .build())
            .block();

    Procedure procedure1 = Procedure.builder().label("foo").build();
    Procedure procedure2 = Procedure.builder().label("bar").build();
    Procedure procedure3 = Procedure.builder().label("baz").build();

    // add first procedure
    DocumentUnit documentUnitFromFrontend1 =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure1).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure1);
              assertThat(response.getResponseBody().coreData().previousProcedures()).isNull();
            });

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findAll().get(0).getLabel()).isEqualTo("foo");

    // add second procedure
    DocumentUnit documentUnitFromFrontend2 =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure2).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend2)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure2);
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .isEqualTo(List.of("foo"));
            });

    assertThat(repository.findAll()).hasSize(2);
    assertThat(repository.findAll().get(1).getLabel()).isEqualTo("bar");

    // add third procedure
    DocumentUnit documentUnitFromFrontend3 =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure3).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend3)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure3);
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .isEqualTo(List.of("bar", "foo"));
            });

    assertThat(repository.findAll()).hasSize(3);
    assertThat(repository.findAll().get(2).getLabel()).isEqualTo("baz");
  }

  @Test
  void testAddProcedureWithSameNameToDifferentOffice() {
    JPADocumentationOfficeDTO bghDocOfficeDTO = documentationOfficeRepository.findByLabel("BGH");
    createProcedure("testProcedure", bghDocOfficeDTO);
    assertThat(repository.findAll()).hasSize(1);

    DocumentUnitDTO dto =
        documentUnitRepository
            .save(
                DocumentUnitDTO.builder()
                    .uuid(UUID.randomUUID())
                    .creationtimestamp(Instant.now())
                    .documentnumber("1234567890123")
                    .documentationOfficeId(documentationOfficeDTO.getId())
                    .build())
            .block();

    Procedure procedure = Procedure.builder().label("testProcedure").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getUuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure()).isEqualTo(procedure));

    assertThat(repository.findAll()).hasSize(2);
  }

  @Test
  void testProcedureControllerReturnsList() {
    createProcedures(
        List.of("testProcedure1", "testProcedure2", "testProcedure3"), documentationOfficeDTO);
    assertThat(repository.findAll()).hasSize(3);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Procedure[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(List.of(response.getResponseBody()).get(0).label())
                  .isEqualTo("testProcedure3");
            });
  }

  @Test
  void testProcedureControllerReturnsFilteredList() {
    createProcedures(List.of("aaabbb", "aaaccc", "dddfff"), documentationOfficeDTO);
    assertThat(repository.findAll()).hasSize(3);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=aaa")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Procedure[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(List.of(response.getResponseBody()).get(0).label()).isEqualTo("aaaccc");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=aaac")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Procedure[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(List.of(response.getResponseBody()).get(0).label()).isEqualTo("aaaccc");
            });
  }

  @Test
  void testProcedureControllerReturnsPerDocOffice() {
    JPADocumentationOfficeDTO bghDocOfficeDTO = documentationOfficeRepository.findByLabel("BGH");
    createProcedures(List.of("procedure1", "procedure2", "procedure3"), bghDocOfficeDTO);
    assertThat(repository.findAll()).hasSize(3);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Procedure[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEmpty();
            });
  }

  private List<JPAProcedureDTO> createProcedures(
      List<String> labels, JPADocumentationOfficeDTO documentationOffice) {
    return labels.stream()
        .map(
            label ->
                repository.save(
                    JPAProcedureDTO.builder()
                        .documentationOffice(documentationOffice)
                        .label(label)
                        .build()))
        .toList();
  }

  private JPAProcedureDTO createProcedure(
      String label, JPADocumentationOfficeDTO documentationOffice) {
    return createProcedures(List.of(label), documentationOffice).get(0);
  }
}
