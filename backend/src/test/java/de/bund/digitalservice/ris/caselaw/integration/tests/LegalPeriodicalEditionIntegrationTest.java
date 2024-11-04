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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalEditionRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresLegalPeriodicalRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
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
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalRepository;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@Sql(scripts = {"classpath:legal_periodical_init.sql"})
@Sql(
    scripts = {"classpath:legal_periodical_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class LegalPeriodicalEditionIntegrationTest {
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

  @MockBean private UserService userService;
  @MockBean private DocumentationUnitStatusService statusService;
  @MockBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private ProcedureService procedureService;
  @MockBean private HandoverService handoverService;
  @MockBean private DocumentNumberService numberService;
  @MockBean private DocumentNumberRecyclingService recyclingService;
  @MockBean private AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;

  private static final String EDITION_ENDPOINT = "/api/v1/caselaw/legalperiodicaledition";
  private final DocumentationOffice docOffice = buildDSDocOffice();

  @BeforeEach
  void setUp() {
    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @Test
  void testFindLegalPeriodical_byAbbreviationOrTitle_shouldSucceed() {
    Assertions.assertNotNull(
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("A&G")).stream().findFirst(),
        "Expected a legal periodical by abbreviation but none was found");

    Assertions.assertNotNull(
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("Arbeit & Gesundheit")).stream()
            .findFirst(),
        "Expected a legal periodical by title but none was found");
  }

  @Test
  void testGetLegalPeriodicalEditions_byLegalPeriodical_shouldReturnValue() {

    var legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("ABC")).stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));

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
  void testGetEditionById() {
    var legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("ABC")).stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));

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
  void testDeleteEditionWithoutReferences() {
    var legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("ABC")).stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));

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
  void testGetLegalPeriodicalEditionsWithReferences() throws DocumentationUnitNotExistsException {
    var legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("ABC")).stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));

    var existingReferenceId = UUID.randomUUID();

    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    documentationUnitRepository.save(
        docUnit.toBuilder()
            .references(
                List.of(
                    ReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .rank(1)
                        .citation("1")
                        .legalPeriodicalRawValue("A")
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build(),
                    ReferenceDTO.builder()
                        .id(existingReferenceId)
                        .citation("Original Citation")
                        .legalPeriodicalRawValue("B")
                        .rank(2)
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build(),
                    ReferenceDTO.builder()
                        .id(UUID.randomUUID())
                        .rank(3)
                        .citation("3")
                        .legalPeriodicalRawValue("C")
                        .documentationUnit(
                            DocumentationUnitDTO.builder()
                                .id(docUnit.getId())
                                .documentNumber("DOC_NUMBER")
                                .build())
                        .build()))
            .build());

    UUID newReferenceId = UUID.randomUUID();

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
                            .id(existingReferenceId)
                            .citation("New Citation")
                            .legalPeriodicalRawValue("B")
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build(),
                        Reference.builder()
                            .id(newReferenceId)
                            .citation("New Reference")
                            .legalPeriodicalRawValue("D")
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build()))
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
    Assertions.assertEquals(2, editionList.get(0).references().size());
    Assertions.assertEquals("New Reference", editionList.get(0).references().get(0).citation());
    Assertions.assertEquals("New Citation", editionList.get(0).references().get(1).citation());

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").references())
        .hasSize(4)
        .satisfies(
            list -> {
              assertThat(list.get(1).id()).isEqualTo(existingReferenceId);
              assertThat(list.get(1).citation()).isEqualTo("New Citation");
              assertThat(list.get(3).id()).isEqualTo(newReferenceId);
              assertThat(list.get(3).citation()).isEqualTo("New Reference");
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testGetLegalPeriodicalEditionsWithDocUnitCreatedByReference()
      throws DocumentationUnitNotExistsException {
    var legalPeriodical =
        legalPeriodicalRepository.findAllBySearchStr(Optional.of("ABC")).stream()
            .findAny()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Legal periodical not found, check legal_periodical_init.sql"));

    var referenceId = UUID.randomUUID();

    // create skeleton doc unit to retrieve ID
    var docUnit =
        documentationUnitRepository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("DOC_NUMBER")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .build());

    // add status and source
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .status(
                StatusDTO.builder()
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .createdAt(Instant.now())
                    .withError(false)
                    .documentationUnit(docUnit)
                    .build())
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .reference(
                                ReferenceDTO.builder()
                                    .id(referenceId)
                                    .rank(1)
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
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
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
}
