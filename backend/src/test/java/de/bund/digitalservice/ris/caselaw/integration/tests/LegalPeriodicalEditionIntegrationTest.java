package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.LegalPeriodicalEditionController;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDependentLiteratureCitationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalEditionRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalRepository;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      LegalPeriodicalEditionService.class,
      PostgresLegalPeriodicalEditionRepositoryImpl.class,
      PostgresLegalPeriodicalRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      PostgresFieldOfLawRepositoryImpl.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class
    },
    controllers = {LegalPeriodicalEditionController.class})
@Sql(scripts = {"classpath:legal_periodical_init.sql", "classpath:document_types.sql"})
@Sql(
    scripts = {"classpath:legal_periodical_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class LegalPeriodicalEditionIntegrationTest {
  public static final DocumentType EBS =
      DocumentType.builder()
          .uuid(UUID.fromString("198b276e-8e6d-4df6-8692-44d74ed4fcba"))
          .jurisShortcut("Ebs")
          .build();
  public static final DocumentType EAN =
      DocumentType.builder()
          .uuid(UUID.fromString("f718a7ee-f419-46cf-a96a-29227927850c"))
          .jurisShortcut("Ean")
          .build();

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
  @Autowired private LegalPeriodicalEditionRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private LegalPeriodicalRepository legalPeriodicalRepository;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DocumentationUnitService documentationUnitService;
  @Autowired private DatabaseReferenceRepository referenceRepository;
  @Autowired private DatabaseDependentLiteratureCitationRepository literatureCitationRepository;

  @MockBean private UserService userService;
  @MockBean private DocumentationUnitStatusService statusService;
  @MockBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private ProcedureService procedureService;
  @MockBean private HandoverService handoverService;
  @MockBean private DocumentNumberService numberService;
  @MockBean private DocumentNumberRecyclingService recyclingService;
  @MockBean private AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;
  @MockBean private DuplicateCheckService duplicateCheckService;

  private static final String EDITION_ENDPOINT = "/api/v1/caselaw/legalperiodicaledition";
  private final DocumentationOffice docOffice = buildDSDocOffice();
  private LegalPeriodical legalPeriodical;

  @BeforeEach
  void setUp() {
    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).getFirst().equals("/DS");
                }));
    doReturn(true)
        .when(userService)
        .isInternal(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).getFirst().equals("/DS");
                }));

    legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr("ABC").stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));
  }

  @AfterEach
  void tearDown() {
    documentationUnitRepository.deleteAll();
  }

  @Test
  void testFindLegalPeriodical_byAbbreviationOrTitle_shouldSucceed() {
    Assertions.assertNotNull(
        legalPeriodicalRepository.findAllBySearchStr("A&G").stream().findFirst(),
        "Expected a legal periodical by abbreviation but none was found");

    Assertions.assertNotNull(
        legalPeriodicalRepository.findAllBySearchStr("Arbeit & Gesundheit").stream().findFirst(),
        "Expected a legal periodical by title but none was found");
  }

  @Test
  void testGetEditions_byLegalPeriodical_shouldReturnValue() {
    repository.save(
        LegalPeriodicalEdition.builder()
            .id(UUID.randomUUID())
            .legalPeriodical(legalPeriodical)
            .name("2024 Sonderheft 1")
            .prefix("2024,")
            .suffix("- Sonderheft 1")
            .build());

    var editionList =
        Arrays.stream(
                risWebTestClient
                    .withDefaultLogin()
                    .get()
                    .uri(EDITION_ENDPOINT + "?legal_periodical_id=" + legalPeriodical.uuid())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(LegalPeriodicalEdition[].class)
                    .returnResult()
                    .getResponseBody())
            .toList();

    Assertions.assertFalse(editionList.isEmpty(), "List should not be empty");
    Assertions.assertEquals("2024 Sonderheft 1", editionList.get(0).name());
  }

  @Test
  void testGetEdition_ById_shouldSucceed() {

    var saved =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(UUID.randomUUID())
                .legalPeriodical(legalPeriodical)
                .name("2024 Sonderheft 1")
                .prefix("2024,")
                .suffix("- Sonderheft 1")
                .build());

    Assertions.assertNotNull(saved.createdAt());

    var result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(EDITION_ENDPOINT + "/" + saved.id())
            .exchange()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertEquals(saved, result);
  }

  @Test
  void testDeleteEdition_withoutReferences_shouldSucceed() {

    var legalPeriodicalEdition =
        LegalPeriodicalEdition.builder()
            .id(UUID.randomUUID())
            .legalPeriodical(legalPeriodical)
            .prefix("2024, ")
            .build();
    legalPeriodicalEdition = repository.save(legalPeriodicalEdition);
    assertThat(repository.findAllByLegalPeriodicalId(legalPeriodical.uuid())).hasSize(1);
    repository.delete(legalPeriodicalEdition);

    assertThat(repository.findAllByLegalPeriodicalId(legalPeriodical.uuid())).isEmpty();
  }

  @Test
  void testGetEdition_withMixedReferencesAndLiteratureCitationsFromDocUnitAndEdition_shouldSucceed()
      throws DocumentationUnitNotExistsException {

    UUID existingReferenceId = UUID.randomUUID();
    UUID existingLiteratureCitationId = UUID.randomUUID();

    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    documentationUnitRepository.save(
        docUnit.toBuilder()
            .literatureReferences(
                List.of(
                    LiteratureReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .documentationUnitRank(1)
                        .citation("Literature Reference Citation from Docunit")
                        .legalPeriodicalRawValue("A")
                        .author("author 1")
                        .documentType(
                            DocumentTypeDTO.builder()
                                .id(UUID.fromString("f718a7ee-f419-46cf-a96a-29227927850c"))
                                .abbreviation("Ean")
                                .build())
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build(),
                    LiteratureReferenceDTO.builder()
                        .id(existingLiteratureCitationId)
                        .documentationUnitRank(2)
                        .citation("Original Literature Reference Citation from Docunit")
                        .author("author 2")
                        .documentType(
                            DocumentTypeDTO.builder()
                                .id(UUID.fromString("198b276e-8e6d-4df6-8692-44d74ed4fcba"))
                                .abbreviation("Ebs")
                                .build())
                        .legalPeriodicalRawValue("B")
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build()))
            .caselawReferences(
                List.of(
                    CaselawReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .documentationUnitRank(1)
                        .citation("Caselaw Reference Citation from Docunit")
                        .legalPeriodicalRawValue("A")
                        .type("amtlich")
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build(),
                    CaselawReferenceDTO.builder()
                        .id(existingReferenceId)
                        .citation("Original Caselaw Reference Citation from Docunit")
                        .legalPeriodicalRawValue("B")
                        .type("amtlich")
                        .documentationUnitRank(2)
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build()))
            .build());

    UUID newReferenceId = UUID.randomUUID();
    UUID newLiteratureReferenceId = UUID.randomUUID();
    UUID editionId = UUID.randomUUID();

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_NUMBER")
            .build();
    var edition =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(editionId)
                .legalPeriodical(legalPeriodical)
                .name("2024 Sonderheft 1")
                .prefix("2024,")
                .suffix("- Sonderheft 1")
                .references(
                    List.of(
                        Reference.builder()
                            .id(existingReferenceId)
                            .referenceType(ReferenceType.CASELAW)
                            .citation("Updated Caselaw Reference Citation from Edition")
                            .legalPeriodicalRawValue("B")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(existingLiteratureCitationId)
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("Updated Literature Reference Citation from Edition")
                            .author("author 2")
                            .documentType(EBS)
                            .legalPeriodicalRawValue("B")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(newReferenceId)
                            .referenceType(ReferenceType.CASELAW)
                            .citation("New Caselaw Reference Citation from Edition")
                            .legalPeriodicalRawValue("D")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(newLiteratureReferenceId)
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("New Literature Reference Citation from Edition")
                            .author("author 3")
                            .documentType(EAN)
                            .legalPeriodicalRawValue("C")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build()))
                .build());

    var editionResponse =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(EDITION_ENDPOINT + "/" + edition.id())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertEquals("2024 Sonderheft 1", edition.name());
    List<Reference> references = editionResponse.references();
    Assertions.assertEquals(4, references.size());
    Assertions.assertEquals(
        "Updated Caselaw Reference Citation from Edition", references.get(0).citation());
    Assertions.assertEquals(
        "Updated Literature Reference Citation from Edition", references.get(1).citation());
    Assertions.assertEquals(
        "New Caselaw Reference Citation from Edition", references.get(2).citation());
    Assertions.assertEquals(
        "New Literature Reference Citation from Edition", references.get(3).citation());

    // first, caselaw references
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").references())
        .hasSize(3)
        .satisfies(
            list -> {
              assertThat(list.get(0).citation())
                  .isEqualTo("Caselaw Reference Citation from Docunit");
              assertThat(list.get(1).id()).isEqualTo(existingReferenceId);
              assertThat(list.get(1).citation())
                  .isEqualTo("Updated Caselaw Reference Citation from Edition");
              assertThat(list.get(1).documentationUnitRank()).isEqualTo(2);
              assertThat(list.get(2).id()).isEqualTo(newReferenceId);
              assertThat(list.get(2).citation())
                  .isEqualTo("New Caselaw Reference Citation from Edition");
              assertThat(list.get(2).documentationUnitRank()).isEqualTo(3);
            });

    // then, literature references
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").literatureReferences())
        .hasSize(3)
        .satisfies(
            list -> {
              assertThat(list.get(0).citation())
                  .isEqualTo("Literature Reference Citation from Docunit");
              assertThat(list.get(1).id()).isEqualTo(existingLiteratureCitationId);
              assertThat(list.get(1).citation())
                  .isEqualTo("Updated Literature Reference Citation from Edition");
              assertThat(list.get(1).documentationUnitRank()).isEqualTo(2);
              assertThat(list.get(2).id()).isEqualTo(newLiteratureReferenceId);
              assertThat(list.get(2).citation())
                  .isEqualTo("New Literature Reference Citation from Edition");
              assertThat(list.get(2).documentationUnitRank()).isEqualTo(3);
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testDeleteReferencesAndLiteratureCitations_fromEdition_shouldSucceed()
      throws DocumentationUnitNotExistsException {

    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    // add preexisting references and literature citations
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .literatureReferences(
                List.of(
                    LiteratureReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .documentationUnitRank(1)
                        .citation("Literature Reference from Docunit")
                        .legalPeriodicalRawValue("A")
                        .author("author 1")
                        .documentType(
                            DocumentTypeDTO.builder()
                                .id(UUID.fromString("f718a7ee-f419-46cf-a96a-29227927850c"))
                                .abbreviation("Ean")
                                .build())
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build()))
            .caselawReferences(
                List.of(
                    CaselawReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .documentationUnitRank(1)
                        .citation("Caselaw Reference from Docunit")
                        .legalPeriodicalRawValue("A")
                        .type("amtlich")
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build()))
            .build());

    UUID literatureCitationId = UUID.randomUUID();
    UUID referenceId = UUID.randomUUID();
    UUID editionId = UUID.randomUUID();

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_NUMBER")
            .build();

    var edition =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(editionId)
                .legalPeriodical(legalPeriodical)
                .name("2024 Sonderheft 1")
                .prefix("2024,")
                .suffix("- Sonderheft 1")
                .references(
                    List.of(
                        Reference.builder()
                            .id(referenceId)
                            .referenceType(ReferenceType.CASELAW)
                            .citation("Updated Caselaw Reference Citation from Edition")
                            .legalPeriodicalRawValue("B")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(literatureCitationId)
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("Updated Literature Reference Citation from Edition")
                            .author("author 2")
                            .documentType(EBS)
                            .legalPeriodicalRawValue("B")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(UUID.randomUUID())
                            .referenceType(ReferenceType.CASELAW)
                            .citation("New Caselaw Reference from Edition")
                            .legalPeriodicalRawValue("D")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
                            .id(UUID.randomUUID())
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("New Literature Reference from Edition")
                            .author("author 4")
                            .documentType(EAN)
                            .legalPeriodicalRawValue("C")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build()))
                .build());

    edition.references().remove(0); // delete Updated Caselaw Reference Citation from Edition
    edition.references().remove(0); // delete Updated Literature Reference Citation from Edition

    assertThat(referenceRepository.findById(referenceId)).isPresent();
    assertThat(literatureCitationRepository.findById(literatureCitationId)).isPresent();

    var editionResponse =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri(EDITION_ENDPOINT)
            .bodyValue(edition)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertEquals("2024 Sonderheft 1", edition.name());
    List<Reference> references = editionResponse.references();
    Assertions.assertEquals(2, references.size());
    Assertions.assertEquals("New Caselaw Reference from Edition", references.get(0).citation());
    Assertions.assertEquals("New Literature Reference from Edition", references.get(1).citation());

    // assure rank is updated
    assertThat(references.get(0).documentationUnitRank()).isEqualTo(1);
    assertThat(references.get(1).documentationUnitRank()).isEqualTo(2);

    // documentation unit references are updated
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").references())
        .hasSize(2)
        .satisfies(
            list -> {
              // first, caselaw references
              assertThat(list.get(0).citation()).isEqualTo("Caselaw Reference from Docunit");
              assertThat(list.get(0).documentationUnitRank()).isEqualTo(1);
              assertThat(list.get(1).citation()).isEqualTo("New Caselaw Reference from Edition");
              assertThat(list.get(1).documentationUnitRank()).isEqualTo(2);
            });

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").literatureReferences())
        .hasSize(2)
        .satisfies(
            list -> {
              // then, literature citations
              assertThat(list.get(0).citation()).isEqualTo("Literature Reference from Docunit");
              assertThat(list.get(0).documentationUnitRank()).isEqualTo(1);
              assertThat(list.get(1).citation()).isEqualTo("New Literature Reference from Edition");
              assertThat(list.get(1).documentationUnitRank()).isEqualTo(2);
            });

    assertThat(referenceRepository.findById(referenceId)).isEmpty();
    assertThat(literatureCitationRepository.findById(literatureCitationId)).isEmpty();

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testGetEdition_withDocUnitCreatedByReference_shouldSucceed()
      throws DocumentationUnitNotExistsException {

    var referenceId = UUID.randomUUID();

    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    // add status and source
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .reference(
                                CaselawReferenceDTO.builder()
                                    .id(referenceId)
                                    .documentationUnitRank(1)
                                    .legalPeriodicalRawValue("ABC")
                                    .citation("ABC 2024, 3")
                                    .documentationUnit(docUnit)
                                    .legalPeriodical(
                                        LegalPeriodicalTransformer.transformToDTO(legalPeriodical))
                                    .build())
                            .value("ABC 2024, 3")
                            .build())))
            .build());

    // add reference via edition
    var edition =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(UUID.randomUUID())
                .legalPeriodical(legalPeriodical)
                .name("2024 Sonderheft 1")
                .prefix("2024,")
                .suffix("- Sonderheft 1")
                .references(
                    List.of(
                        Reference.builder()
                            .id(referenceId)
                            .referenceType(ReferenceType.CASELAW)
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
                            .primaryReference(true)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .createdByReference(referenceId)
                                    .build())
                            .build()))
                .build());

    var editionList =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(EDITION_ENDPOINT + "/" + edition.id())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertNotNull(editionList, "Edition should not be null");
    var firstEditionReferences = editionList.references();
    Assertions.assertEquals(1, firstEditionReferences.size());
    Assertions.assertEquals("ABC 2024, 3", firstEditionReferences.get(0).citation());
    Assertions.assertEquals(
        referenceId, firstEditionReferences.get(0).documentationUnit().getCreatedByReference());

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").references())
        .hasSize(1)
        .satisfies(
            list -> {
              assertThat(list.get(0).id()).isEqualTo(referenceId);
              assertThat(list.get(0).citation()).isEqualTo("ABC 2024, 3");
              assertThat(list.get(0).documentationUnit().getCreatedByReference())
                  .isEqualTo(referenceId);
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testDeleteReference_shouldCleanupDocUnitSource() throws DocumentationUnitNotExistsException {

    var referenceId = UUID.randomUUID();

    // create skeleton doc unit to retrieve ID
    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    // add status and source
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .fileNumbers(List.of(EntityBuilderTestUtil.createTestFileNumberDTO()))
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .reference(
                                CaselawReferenceDTO.builder()
                                    .id(referenceId)
                                    .documentationUnitRank(1)
                                    .legalPeriodicalRawValue("ABC")
                                    .citation("ABC 2024, 3")
                                    .documentationUnit(docUnit)
                                    .legalPeriodical(
                                        LegalPeriodicalTransformer.transformToDTO(legalPeriodical))
                                    .build())
                            .value("ABC 2024, 3")
                            .build())))
            .build());

    // add reference via edition
    var edition =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(UUID.randomUUID())
                .legalPeriodical(legalPeriodical)
                .name("2024 Sonderheft 1")
                .prefix("2024,")
                .suffix("- Sonderheft 1")
                .references(
                    List.of(
                        Reference.builder()
                            .id(referenceId)
                            .referenceType(ReferenceType.CASELAW)
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
                            .primaryReference(true)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .createdByReference(referenceId)
                                    .build())
                            .build()))
                .build());

    // delete reference
    edition = repository.save(edition.toBuilder().references(List.of()).build());

    var editionList =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(EDITION_ENDPOINT + "/" + edition.id())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertNotNull(editionList, "Edition should not be null");
    Assertions.assertEquals(0, editionList.references().size());

    assertThat(
            documentationUnitService
                .searchLinkableDocumentationUnits(
                    RelatedDocumentationUnit.builder().fileNumber("AB 34/1").build(),
                    docOffice,
                    Optional.empty(),
                    Pageable.ofSize(1))
                .iterator()
                .next()
                .getCreatedByReference())
        .isNull();

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").references()).isEmpty();
  }
}
