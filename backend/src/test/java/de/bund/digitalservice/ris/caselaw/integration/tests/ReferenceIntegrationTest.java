package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDependentLiteratureCitationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalPeriodicalRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalEditionRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentationUnitStatusService.class,
      PostgresLegalPeriodicalEditionRepositoryImpl.class,
      DatabaseProcedureService.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class
    },
    controllers = {DocumentationUnitController.class})
class ReferenceIntegrationTest {
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
  @Autowired private DatabaseLegalPeriodicalRepository legalPeriodicalRepository;
  @Autowired private DatabaseDocumentTypeRepository documentTypeRepository;
  @Autowired private LegalPeriodicalEditionRepository editionRepository;
  @Autowired private DatabaseReferenceRepository referenceRepository;
  @Autowired private DatabaseDependentLiteratureCitationRepository literatureCitationRepository;

  @MockitoBean private S3AsyncClient s3AsyncClient;
  @MockitoBean private MailService mailService;
  @MockitoBean private DocxConverterService docxConverterService;
  @MockitoBean private UserService userService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private StagingPortalPublicationService stagingPortalPublicationService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private FmxService fmxService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890126";
  private DocumentType eanDocumentType;
  private LegalPeriodical bverwgeLegalPeriodical;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    when(userService.getDocumentationOffice(any())).thenReturn(docOffice);

    eanDocumentType =
        DocumentTypeTransformer.transformToDomain(
            documentTypeRepository.save(
                DocumentTypeDTO.builder()
                    .label("Anmerkung")
                    .abbreviation("Ean")
                    .multiple(false)
                    .build()));

    bverwgeLegalPeriodical =
        LegalPeriodicalTransformer.transformToDomain(
            legalPeriodicalRepository.save(
                LegalPeriodicalDTO.builder()
                    .abbreviation("BVerwGE")
                    .title("Bundesverwaltungsgerichtsentscheidungen")
                    .subtitle("Entscheidungen des Bundesverwaltungsgerichts")
                    .jurisId(123)
                    .primaryReference(true)
                    .build()));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    legalPeriodicalRepository.deleteAll();
  }

  // TODO add test for rank

  @Test
  void testReferencesCanBeSaved() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice);

    UUID referenceId = UUID.randomUUID();
    DocumentationUnit documentationUnitFromFrontend =
        DocumentationUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .caselawReferences(
                List.of(
                    Reference.builder()
                        .id(referenceId)
                        .citation("2024, S.3")
                        .referenceSupplement("Klammerzusatz")
                        .footnote("footnote")
                        .referenceType(ReferenceType.CASELAW)
                        .legalPeriodical(
                            LegalPeriodical.builder()
                                .uuid(bverwgeLegalPeriodical.uuid())
                                .abbreviation("BVerwGE")
                                .primaryReference(true)
                                .build())
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentationUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber())
                  .isEqualTo(DEFAULT_DOCUMENT_NUMBER);
              assertThat(response.getResponseBody().caselawReferences()).hasSize(1);
              assertThat(response.getResponseBody().caselawReferences())
                  .extracting("citation", "referenceSupplement", "footnote", "id")
                  .containsExactly(tuple("2024, S.3", "Klammerzusatz", "footnote", referenceId));
              assertThat(response.getResponseBody().caselawReferences())
                  .extracting("legalPeriodical")
                  .usingRecursiveComparison()
                  .isEqualTo(List.of(bverwgeLegalPeriodical));
            });
  }

  @Test
  void testLiteratureReferencesCanBeSaved() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice);

    UUID referenceId = UUID.randomUUID();
    DocumentationUnit documentationUnitFromFrontend =
        DocumentationUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .literatureReferences(
                List.of(
                    Reference.builder()
                        .id(referenceId)
                        .citation("2024, S.3")
                        .author("Heinz Otto")
                        .documentType(eanDocumentType)
                        .referenceType(ReferenceType.LITERATURE)
                        .legalPeriodical(
                            LegalPeriodical.builder()
                                .uuid(bverwgeLegalPeriodical.uuid())
                                .abbreviation("BVerwGE")
                                .primaryReference(true)
                                .build())
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentationUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber())
                  .isEqualTo(DEFAULT_DOCUMENT_NUMBER);
              assertThat(response.getResponseBody().literatureReferences()).hasSize(1);
              assertThat(response.getResponseBody().literatureReferences())
                  .extracting("citation", "author", "documentType", "id")
                  .containsExactly(tuple("2024, S.3", "Heinz Otto", eanDocumentType, referenceId));
              assertThat(response.getResponseBody().literatureReferences())
                  .extracting("legalPeriodical")
                  .usingRecursiveComparison()
                  .isEqualTo(List.of(bverwgeLegalPeriodical));
            });
  }

  @Test
  void testReferencesAndLiteratureCitationsCanBeDeleted() {
    DecisionDTO dto =
        (DecisionDTO)
            EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
                repository, documentationOffice);

    UUID referenceId = UUID.randomUUID();
    UUID literatureCitationId = UUID.randomUUID();

    repository.save(
        dto.toBuilder()
            .caselawReferences(
                List.of(
                    CaselawReferenceDTO.builder()
                        .documentationUnitRank(1)
                        .documentationUnit(dto)
                        .id(referenceId)
                        .citation("2024, S.3")
                        .legalPeriodicalRawValue("BVerwGE")
                        .legalPeriodical(
                            LegalPeriodicalDTO.builder().id(bverwgeLegalPeriodical.uuid()).build())
                        .build()))
            .literatureReferences(
                List.of(
                    LiteratureReferenceDTO.builder()
                        .documentationUnitRank(1)
                        .documentationUnit(dto)
                        .id(literatureCitationId)
                        .citation("2024, S.3")
                        .author("Curie, Marie")
                        .legalPeriodicalRawValue("BVerwGE")
                        .documentTypeRawValue("Ean")
                        .documentType(DocumentTypeDTO.builder().id(eanDocumentType.uuid()).build())
                        .legalPeriodical(
                            LegalPeriodicalDTO.builder().id(bverwgeLegalPeriodical.uuid()).build())
                        .build()))
            .build());

    DocumentationUnit documentationUnitFromFrontend =
        DocumentationUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .caselawReferences(List.of())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentationUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().caselawReferences()).isEmpty();
            });

    assertThat(referenceRepository.findById(referenceId)).isEmpty();
    assertThat(literatureCitationRepository.findById(literatureCitationId)).isEmpty();
  }

  @Test
  void testReferencesAndLiteratureCitationsOriginatedFromEditionCanBeDeleted() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice);

    UUID referenceId = UUID.randomUUID();
    UUID literatureCitationId = UUID.randomUUID();

    var edition =
        editionRepository.save(
            LegalPeriodicalEdition.builder()
                .id(UUID.randomUUID())
                .legalPeriodical(bverwgeLegalPeriodical)
                .name("2024")
                .references(
                    List.of(
                        Reference.builder()
                            .referenceType(ReferenceType.CASELAW)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder().uuid(dto.getId()).build())
                            .id(referenceId)
                            .citation("2024, S.3")
                            .legalPeriodicalRawValue("BVerwGE")
                            .legalPeriodical(
                                LegalPeriodical.builder()
                                    .uuid(bverwgeLegalPeriodical.uuid())
                                    .primaryReference(true)
                                    .build())
                            .build(),
                        Reference.builder()
                            .referenceType(ReferenceType.LITERATURE)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder().uuid(dto.getId()).build())
                            .id(literatureCitationId)
                            .citation("2024, S.3")
                            .author("Curie, Marie")
                            .legalPeriodicalRawValue("BVerwGE")
                            .documentType(eanDocumentType)
                            .legalPeriodical(bverwgeLegalPeriodical)
                            .build()))
                .build());

    DocumentationUnit documentationUnitFromFrontend =
        DocumentationUnit.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .caselawReferences(List.of())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(documentationUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().caselawReferences()).isEmpty();
            });

    assertThat(referenceRepository.findById(referenceId)).isEmpty();
    assertThat(literatureCitationRepository.findById(literatureCitationId)).isEmpty();
    assertThat(editionRepository.findById(edition.id()).get().references()).isEmpty();

    editionRepository.delete(edition);
  }
}
