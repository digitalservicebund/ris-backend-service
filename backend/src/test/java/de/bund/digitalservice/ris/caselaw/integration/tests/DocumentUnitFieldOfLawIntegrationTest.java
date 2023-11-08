package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
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
@Sql(
    scripts = {"classpath:fields_of_law_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:fields_of_law_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class DocumentUnitFieldOfLawIntegrationTest {
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
  @Autowired private DatabaseDocumentationUnitRepository documentUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    when(userService.getDocumentationOffice(any())).thenReturn(Mono.just(docOffice));
  }

  @AfterEach
  void cleanUp() {
    documentUnitRepository.deleteAll();
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_withoutFieldOfLawLinked_shouldReturnEmptyList() {
    UUID documentUnitUuid = UUID.randomUUID();
    documentUnitRepository.save(
        DocumentationUnitDTO.builder()
            .id(documentUnitUuid)
            .documentationOffice(documentationOfficeDTO)
            .documentNumber("docnr12345678")
            .build());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .isEmpty());
  }

  @Test
  void
      testGetAllFieldsOfLawForDocumentUnit_withFirstFieldOfLawLinked_shouldReturnListWithLinkedFieldOfLaw() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.save(
            DocumentationUnitDTO.builder()
                .id(documentUnitUuid)
                .documentationOffice(documentationOfficeDTO)
                .documentNumber("docnr12345678")
                .fieldsOfLaw(
                    List.of(
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
                            .build()))
                .build());

    assertThat(documentUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactly("FL-01"));
  }

  @Test
  void testGetAllFieldsOfLawForDocumentUnit_shouldReturnSortedList() {
    // TODO: have to refactor after rank exist
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.save(
            DocumentationUnitDTO.builder()
                .id(documentUnitUuid)
                .documentationOffice(documentationOfficeDTO)
                .documentNumber("docnr12345678")
                .fieldsOfLaw(
                    List.of(
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
                            .build(),
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("6959af10-7355-4e22-858d-29a485189957"))
                            .build(),
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("93393410-0ab0-48ab-a61d-5056e440174a"))
                            .build(),
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("b4f9ee05-38ed-49c3-89d6-50141f031017"))
                            .build()))
                .build());

    assertThat(documentUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactlyInAnyOrder("AB-01", "CD-01", "FL-01", "FL-02"));
  }

  @Test
  void testAddFieldsOfLawForDocumentUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeDTO)
                .documentNumber("docnr12345678")
                .build());

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .fieldsOfLaw(
                        List.of(
                            FieldOfLaw.builder()
                                .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
                                .build(),
                            FieldOfLaw.builder()
                                .id(UUID.fromString("93393410-0ab0-48ab-a61d-5056e440174a"))
                                .build()))
                    .build())
            .build();

    assertThat(documentUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                // TODO: right order after use of rank
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactlyInAnyOrder("FL-01", "FL-02"));
  }

  @Test
  void
      testAddFieldsOfLawForDocumentUnit_withNotExistingFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeDTO)
                .documentNumber("docnr12345678")
                .build());

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .fieldsOfLaw(
                        List.of(
                            FieldOfLaw.builder()
                                .id(UUID.fromString("11defe05-cd4d-43e5-a07e-06c611b81a26"))
                                .build()))
                    .build())
            .build();

    assertThat(documentUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void testRemoveFieldsOfLawForDocumentUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.save(
            DocumentationUnitDTO.builder()
                .documentationOffice(documentationOfficeDTO)
                .documentNumber("docnr12345678")
                .fieldsOfLaw(
                    List.of(
                        FieldOfLawDTO.builder()
                            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
                            .build()))
                .build());

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitDTO.getId())
            .documentNumber("docnr12345678")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().fieldsOfLaw(Collections.emptyList()).build())
            .build();

    assertThat(documentUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitDTO.getId())
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .isEmpty());
  }
}
