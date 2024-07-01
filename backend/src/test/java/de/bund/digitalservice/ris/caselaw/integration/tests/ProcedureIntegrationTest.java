package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.ProcedureController;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcedureTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      PostgresMigrationRepositoryImpl.class,
      KeycloakUserService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DatabaseProcedureService.class
    },
    controllers = {DocumentUnitController.class, ProcedureController.class})
@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:procedures_init.sql"})
@Sql(
    scripts = {"classpath:procedures_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class ProcedureIntegrationTest {
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
  @Autowired private DatabaseDocumentationUnitRepository documentUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcedureRepository repository;
  @Autowired private DatabaseDocumentationUnitProcedureRepository linkRepository;

  @MockBean private DocumentNumberService numberService;
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockBean private PublicationReportRepository publicationReportRepository;
  @MockBean private UserService userService;
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private AttachmentService attachmentService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO docOfficeDTO;
  private DocumentationUnitDTO docUnitDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    docUnitDTO = documentUnitRepository.findByDocumentNumber("1234567890123").get();
    doReturn(docOffice).when(userService).getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    documentUnitRepository.deleteAll();
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
    Procedure procedure = Procedure.builder().label(label).build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure.label());
              assertThat(response.getResponseBody().coreData().procedure().createdAt()).isNotNull();
            });

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice(label, docOfficeDTO);
    assertThat(resultProcedure).isPresent();
    assertThat(resultProcedure.get().getLabel()).isEqualTo(label);
    assertThat(resultProcedure.get().getDocumentationOffice().getAbbreviation())
        .isEqualTo(docOffice.abbreviation());
  }

  @Test
  void testAddSameProcedureLabel_shouldReturnTheExistingProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();

    Procedure procedure = Procedure.builder().label("procedure1").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure())
                    .extracting("id", "label")
                    .containsExactly(procedureDTO.getId(), procedure.label()));

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);

    assertThat(resultProcedure).isPresent();
    ProcedureDTO firstProcedure =
        linkRepository.findFirstByDocumentationUnitOrderByRankDesc(docUnitDTO).getProcedure();
    assertThat(firstProcedure)
        .extracting("id", "label")
        .containsExactly(procedureDTO.getId(), procedureDTO.getLabel());
    assertThat(firstProcedure.getDocumentationUnits()).hasSize(1);
    assertThat(firstProcedure.getDocumentationUnits().get(0))
        .extracting("id", "documentNumber")
        .containsExactly(docUnitDTO.getId(), docUnitDTO.getDocumentNumber());
    assertThat(linkRepository.findAll()).hasSize(1);
  }

  @Test
  void testAddSameProcedureLabelWithTrailingSpaces_shouldReturnTheExistingProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();

    Procedure procedure = Procedure.builder().label("  procedure1  ").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure())
                    .extracting("id", "label")
                    .containsExactly(procedureDTO.getId(), procedure.label().trim()));

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);

    assertThat(resultProcedure).isPresent();
    ProcedureDTO firstProcedure =
        linkRepository.findFirstByDocumentationUnitOrderByRankDesc(docUnitDTO).getProcedure();
    assertThat(firstProcedure)
        .extracting("id", "label")
        .containsExactly(procedureDTO.getId(), procedureDTO.getLabel());
    assertThat(firstProcedure.getDocumentationUnits()).hasSize(1);
    assertThat(firstProcedure.getDocumentationUnits().get(0))
        .extracting("id", "documentNumber")
        .containsExactly(docUnitDTO.getId(), docUnitDTO.getDocumentNumber());
    assertThat(linkRepository.findAll()).hasSize(1);
  }

  @Test
  void testProcedureLabelWithTrailingSpaces_shouldSaveProcedureWithoutTrailingSpaces() {
    Procedure procedure = Procedure.builder().label("  procedure1  ").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure())
                    .extracting("label")
                    .isEqualTo("procedure1"));

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);
    assertThat(resultProcedure).isPresent();
    ProcedureDTO firstProcedure =
        linkRepository.findFirstByDocumentationUnitOrderByRankDesc(docUnitDTO).getProcedure();
    assertThat(firstProcedure).extracting("label").isEqualTo("procedure1");
    assertThat(firstProcedure.getDocumentationUnits()).hasSize(1);
    assertThat(firstProcedure.getDocumentationUnits().get(0))
        .extracting("id", "documentNumber")
        .containsExactly(docUnitDTO.getId(), docUnitDTO.getDocumentNumber());
    assertThat(linkRepository.findAll()).hasSize(1);
  }

  @Test
  @SuppressWarnings("java:S5961")
  void testAddingProcedureToPreviousProcedures() {
    Procedure procedure1 = Procedure.builder().label("foo").build();
    Procedure procedure2 = Procedure.builder().label("bar").build();
    Procedure procedure3 = Procedure.builder().label("baz").build();

    // add first procedure
    DocumentUnit documentUnitFromFrontend1 =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure1).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure1.label());
              assertThat(response.getResponseBody().coreData().previousProcedures()).isEmpty();
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("foo", docOfficeDTO).get())
        .isNotNull();

    // add second procedure
    DocumentUnit documentUnitFromFrontend2 =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure2).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend2)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure2.label());
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .isEqualTo(List.of("foo"));
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("bar", docOfficeDTO).get())
        .isNotNull();

    // add third procedure
    DocumentUnit documentUnitFromFrontend3 =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure3).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend3)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure3.label());
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .containsExactly("foo", "bar");
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("baz", docOfficeDTO).get())
        .isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=" + procedure1.label() + "&sz=1&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(
                      Objects.requireNonNull(response.getResponseBody())
                          .getContent()
                          .get(0)
                          .documentUnitCount())
                  .isZero();
            });

    var procedure1Id =
        repository.findAllByLabelAndDocumentationOffice("foo", docOfficeDTO).get().getId();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure/" + procedure1Id + "/documentunits")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(Objects.requireNonNull(response.getResponseBody())).isEmpty();
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=" + procedure3.label() + "&sz=1&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(
                      Objects.requireNonNull(response.getResponseBody())
                          .getContent()
                          .get(0)
                          .documentUnitCount())
                  .isEqualTo(1);
            });

    var procedure3Id =
        repository.findAllByLabelAndDocumentationOffice("baz", docOfficeDTO).get().getId();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure/" + procedure3Id + "/documentunits")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(Objects.requireNonNull(response.getResponseBody()).get(0).documentNumber())
                  .isEqualTo("1234567890123");
            });
  }

  @Test
  void testAddProcedureWhichIsInHistoryAgain() {
    UUID procedureId = addProcedureToDocUnit("foo", docUnitDTO);
    addProcedureToDocUnit("bar", docUnitDTO);

    DocumentUnit documentUnitFromFrontend1 =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(Procedure.builder().id(procedureId).build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              CoreData coreData = response.getResponseBody().coreData();
              assertThat(coreData.procedure().label()).isEqualTo("foo");
              assertThat(coreData.previousProcedures()).containsExactly("foo", "bar");
            });
  }

  @Test
  void testAddProcedureWithSameNameToDifferentOffice() {
    Procedure procedure = Procedure.builder().label("testProcedure").build();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure().label())
                    .isEqualTo(procedure.label()));

    DocumentationOfficeDTO dsOffice = documentationOfficeRepository.findByAbbreviation("DS");
    assertThat(repository.findAllByLabelAndDocumentationOffice("testProcedure", dsOffice))
        .isNotNull();
    assertThat(repository.findAllByLabelAndDocumentationOffice("testProcedure", docOfficeDTO))
        .isNotNull();
  }

  @Test
  void testSearch_withoutQuery_shouldReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);
    addProcedureToDocUnit("procedure3", docUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure3");
            });
  }

  @Test
  void testSearch_withQuery_shouldReturnProceduresWithDateFirst() {
    DocumentationUnitDTO documentationUnitDTO2 =
        documentUnitRepository.findByDocumentNumber("documentNumber2").get();
    DocumentationUnitDTO documentationUnitDTO3 =
        documentUnitRepository.findByDocumentNumber("documentNumber3").get();

    addProcedureToDocUnit("with date", docUnitDTO);

    ProcedureDTO procedureWithDateInPast =
        repository.findAllByLabelAndDocumentationOffice("with date in past", docOfficeDTO).get();
    ProcedureDTO procedureWithoutDate =
        repository.findAllByLabelAndDocumentationOffice("without date", docOfficeDTO).get();

    documentationUnitDTO2.setProcedures(
        List.of(
            DocumentationUnitProcedureDTO.builder()
                .documentationUnit(documentationUnitDTO2)
                .procedure(procedureWithDateInPast)
                .build()));
    documentUnitRepository.save(documentationUnitDTO2);

    documentationUnitDTO3.setProcedures(
        List.of(
            DocumentationUnitProcedureDTO.builder()
                .documentationUnit(documentationUnitDTO3)
                .procedure(procedureWithoutDate)
                .build()));
    documentUnitRepository.save(documentationUnitDTO3);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=date&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("with date");
              assertThat(response.getResponseBody().getContent().get(1).label())
                  .isEqualTo("with date in past");
              assertThat(response.getResponseBody().getContent().get(2).label())
                  .isEqualTo("without date");
            });
  }

  @Test
  void testSearch_withQuery_shouldOnlyReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);
    addProcedureToDocUnit("procedure3", docUnitDTO);

    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure3");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure2&pg=0&sz=10")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEmpty();
            });
  }

  @Test
  void testSearch_withQueryWithTrailingSpaces_shouldReturnResultWithoutTrailingSpaces() {
    addProcedureToDocUnit("procedure1", docUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q= procedure1 &sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure1");
            });
  }

  @Test
  void testProcedureControllerReturnsPerDocOffice() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&sz=10&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(Objects.requireNonNull(response.getResponseBody()).getContent()).isEmpty();
            });
  }

  @Test
  // only needed for e2e test
  // TODO remove controller endpoint. check how to handle cleanup after e2e tests
  void testDeleteProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();
    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/procedure/" + procedureDTO.getId())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(repository.findAll()).hasSize(6);
  }

  @Test
  void testProcedureControllerReturnsDocUnitsPerProcedure() {
    DocumentationOfficeDTO bghDocOfficeDTO =
        documentationOfficeRepository.findByAbbreviation("BGH");
    ProcedureDTO procedure =
        repository.findAllByLabelAndDocumentationOffice("testProcedure BGH", bghDocOfficeDTO).get();

    DocumentUnit documentUnitFromFrontend1 =
        DocumentUnit.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(ProcedureTransformer.transformToDomain(procedure))
                    .documentationOffice(docOffice)
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure.getLabel());
              assertThat(response.getResponseBody().coreData().previousProcedures()).isEmpty();
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure/" + procedure.getId() + "/documentunits")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(Objects.requireNonNull(response.getResponseBody()).get(0).documentNumber())
                  .isEqualTo("1234567890123");
            });
  }

  @Test
  void testSearch_withQuery_shouldReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);

    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure2");
            });
  }

  private UUID addProcedureToDocUnit(
      String procedureValue, DocumentationUnitDTO documentationUnitDTO) {
    DocumentUnit documentUnitFromFrontend1 =
        DocumentUnit.builder()
            .uuid(documentationUnitDTO.getId())
            .documentNumber(documentationUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(Procedure.builder().label(procedureValue).build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    AtomicReference<UUID> procedureId = new AtomicReference<>();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
        .bodyValue(documentUnitFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              CoreData coreData = response.getResponseBody().coreData();
              assertThat(coreData.procedure().label()).isEqualTo(procedureValue);
              procedureId.set(coreData.procedure().id());
            });

    return procedureId.get();
  }
}
