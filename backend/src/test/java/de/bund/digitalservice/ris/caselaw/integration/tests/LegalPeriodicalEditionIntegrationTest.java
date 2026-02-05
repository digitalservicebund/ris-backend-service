package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabasePassiveCitationUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalRepository;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:legal_periodical_init.sql", "classpath:document_types.sql"})
@Sql(
    scripts = {"classpath:legal_periodical_cleanup.sql", "classpath:document_types_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class LegalPeriodicalEditionIntegrationTest extends BaseIntegrationTest {
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

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private LegalPeriodicalEditionRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private LegalPeriodicalRepository legalPeriodicalRepository;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseLegalPeriodicalEditionRepository databaseEditionRepository;
  @Autowired private DocumentationUnitService documentationUnitService;
  @Autowired private DatabaseReferenceRepository referenceRepository;
  @Autowired private DatabasePassiveCitationUliRepository passiveCitationUliRepository;

  private static final String EDITION_ENDPOINT = "/api/v1/caselaw/legalperiodicaledition";
  private final DocumentationOffice docOffice = buildDSDocOffice();
  private LegalPeriodical legalPeriodical;

  @BeforeEach
  void setUp() {
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
    referenceRepository.deleteAll();
    databaseEditionRepository.deleteAll();
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

    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    // 1. Speichern mit null-IDs, um generierte IDs zu erhalten
    var savedDocUnitDto =
        documentationUnitRepository.save(
            docUnit.toBuilder()
                .passiveUliCitations(
                    List.of(
                        PassiveCitationUliDTO.builder()
                            .rank(1)
                            .sourceCitation("Literature Reference Citation from Docunit")
                            .sourceLegalPeriodicalRawValue("A")
                            .sourceAuthor("author 1")
                            .sourceDocumentType(
                                DocumentTypeDTO.builder()
                                    .id(UUID.fromString("f718a7ee-f419-46cf-a96a-29227927850c"))
                                    .abbreviation("Ean")
                                    .build())
                            .target(
                                DecisionDTO.builder()
                                    .id(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build(),
                        PassiveCitationUliDTO.builder()
                            .id(null) // Generierung triggern
                            .rank(2)
                            .sourceCitation("Original Literature Reference Citation from Docunit")
                            .sourceAuthor("author 2")
                            .sourceDocumentType(
                                DocumentTypeDTO.builder()
                                    .id(UUID.fromString("198b276e-8e6d-4df6-8692-44d74ed4fcba"))
                                    .abbreviation("Ebs")
                                    .build())
                            .sourceLegalPeriodicalRawValue("B")
                            .target(
                                DecisionDTO.builder()
                                    .id(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build()))
                .caselawReferences(
                    List.of(
                        ReferenceDTO.builder()
                            .id(null) // Generierung triggern
                            .documentationUnitRank(1)
                            .citation("Caselaw Reference Citation from Docunit")
                            .legalPeriodicalRawValue("A")
                            .type("amtlich")
                            .documentationUnit(
                                DecisionDTO.builder()
                                    .id(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build(),
                        ReferenceDTO.builder()
                            .id(null) // Generierung triggern
                            .citation("Original Caselaw Reference Citation from Docunit")
                            .legalPeriodicalRawValue("B")
                            .type("amtlich")
                            .documentationUnitRank(2)
                            .documentationUnit(
                                DecisionDTO.builder()
                                    .id(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .build())
                            .build()))
                .build());

    // 2. Extrahiere die generierten IDs für den Abgleich
    // Wir nehmen den jeweils zweiten Eintrag (Index 1), da diese im Test "geupdated" werden
    UUID existingLiteratureCitationId = savedDocUnitDto.getPassiveUliCitations().get(1).getId();
    UUID existingReferenceId = savedDocUnitDto.getCaselawReferences().get(1).getId();

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_NUMBER")
            .build();
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
                            .referenceType(ReferenceType.CASELAW)
                            .citation("New Caselaw Reference Citation from Edition")
                            .legalPeriodicalRawValue("D")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build(),
                        Reference.builder()
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
    assertThat(editionResponse.references())
        .hasSize(4)
        .satisfiesExactly(
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("Updated Caselaw Reference Citation from Edition"),
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("Updated Literature Reference Citation from Edition"),
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("New Caselaw Reference Citation from Edition"),
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("New Literature Reference Citation from Edition"));

    // first, caselaw references
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .hasSize(3)
        .satisfiesExactly(
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("Caselaw Reference Citation from Docunit"),
            reference -> {
              assertThat(reference.citation())
                  .isEqualTo("Updated Caselaw Reference Citation from Edition");
              assertThat(reference.id()).isEqualTo(existingReferenceId);
            },
            reference -> {
              assertThat(reference.citation())
                  .isEqualTo("New Caselaw Reference Citation from Edition");
            });

    // then, literature references
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").literatureReferences())
        .hasSize(3)
        .satisfiesExactly(
            reference ->
                assertThat(reference.citation())
                    .isEqualTo("Literature Reference Citation from Docunit"),
            reference -> {
              assertThat(reference.citation())
                  .isEqualTo("Updated Literature Reference Citation from Edition");
              assertThat(reference.id()).isEqualTo(existingLiteratureCitationId);
            },
            reference -> {
              assertThat(reference.citation())
                  .isEqualTo("New Literature Reference Citation from Edition");
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testDeleteCaselawReference_fromEdition_shouldSucceed_shouldAlsoDeleteFromDocunit()
      throws DocumentationUnitNotExistsException {
    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_NUMBER")
            .build();

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
                            .referenceType(ReferenceType.CASELAW)
                            .citation("Citation from Edition")
                            .legalPeriodicalRawValue("A")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build()))
                .build());

    UUID referenceIdToDelete = edition.references().getFirst().id();

    var updatedEdition = edition.toBuilder().references(List.of()).build();

    assertThat(referenceRepository.findById(referenceIdToDelete)).isPresent();
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .hasSize(1);

    var editionResponse =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri(EDITION_ENDPOINT)
            .bodyValue(updatedEdition)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .isEmpty();

    Assertions.assertEquals("2024 Sonderheft 1", updatedEdition.name());
    assertThat(editionResponse.references()).isEmpty();

    assertThat(referenceRepository.findById(referenceIdToDelete)).isEmpty();
  }

  @Test
  void testDeleteLiteratureReference_fromEdition_shouldSucceed()
      throws DocumentationUnitNotExistsException {
    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_NUMBER")
            .build();

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
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("Citation")
                            .author("author 1")
                            .documentType(EBS)
                            .legalPeriodicalRawValue("B")
                            .primaryReference(true)
                            .documentationUnit(relatedDocUnit)
                            .build()))
                .build());

    UUID referenceIdToDelete = edition.references().getFirst().id();

    var updatedEdition = edition.toBuilder().references(List.of()).build();

    assertThat(passiveCitationUliRepository.findById(referenceIdToDelete)).isPresent();
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").literatureReferences())
        .hasSize(1);

    var editionResponse =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri(EDITION_ENDPOINT)
            .bodyValue(updatedEdition)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    Assertions.assertEquals("2024 Sonderheft 1", updatedEdition.name());
    assertThat(editionResponse.references()).isEmpty();

    assertThat(passiveCitationUliRepository.findById(referenceIdToDelete)).isEmpty();
    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .isEmpty();
  }

  @Test
  void testDeleteMixedReferences_fromEdition_shouldSucceed()
      throws DocumentationUnitNotExistsException {
    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_MIXED");

    RelatedDocumentationUnit relatedDocUnit =
        RelatedDocumentationUnit.builder()
            .uuid(docUnit.getId())
            .documentNumber("DOC_MIXED")
            .build();

    var edition =
        repository.save(
            LegalPeriodicalEdition.builder()
                .id(UUID.randomUUID())
                .legalPeriodical(legalPeriodical)
                .name("Mixed Edition")
                .references(
                    List.of(
                        Reference.builder()
                            .referenceType(ReferenceType.CASELAW)
                            .citation("Caselaw Citation")
                            .legalPeriodicalRawValue("A")
                            .documentationUnit(relatedDocUnit)
                            .primaryReference(true)
                            .build(),
                        Reference.builder()
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("Literature Citation")
                            .author("Author")
                            .documentType(EBS)
                            .legalPeriodicalRawValue("B")
                            .documentationUnit(relatedDocUnit)
                            .build()))
                .build());

    UUID caselawId = edition.references().get(0).id();
    UUID literatureId = edition.references().get(1).id();

    var updatedEdition =
        edition.toBuilder().references(List.of(edition.references().get(1))).build();

    assertThat(referenceRepository.findById(caselawId)).isPresent();
    assertThat(passiveCitationUliRepository.findById(literatureId)).isPresent();
    assertThat(documentationUnitService.getByDocumentNumber("DOC_MIXED").caselawReferences())
        .hasSize(1);

    // 3. PUT Request
    var editionResponse =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri(EDITION_ENDPOINT)
            .bodyValue(updatedEdition)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LegalPeriodicalEdition.class)
            .returnResult()
            .getResponseBody();

    // 4. Assertions
    // In der Edition darf nur noch Literatur sein
    assertThat(editionResponse.references()).hasSize(1);
    assertThat(editionResponse.references().get(0).referenceType())
        .isEqualTo(ReferenceType.LITERATURE);

    // In der Datenbank: Caselaw weg, Literature da
    assertThat(referenceRepository.findById(caselawId)).isEmpty();
    assertThat(passiveCitationUliRepository.findById(literatureId)).isPresent();

    // In der DocUnit: Caselaw weg, Literature da
    var updatedDocUnit = documentationUnitService.getByDocumentNumber("DOC_MIXED");
    assertThat(updatedDocUnit.caselawReferences()).isEmpty();
    assertThat(updatedDocUnit.literatureReferences()).hasSize(1);
  }

  @Test
  void testGetEdition_withDocUnitCreatedByReference_shouldSucceed()
      throws DocumentationUnitNotExistsException {

    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    var reference =
        ReferenceDTO.builder()
            .documentationUnitRank(1)
            .legalPeriodicalRawValue("ABC")
            .citation("ABC 2024, 3")
            .documentationUnit(docUnit)
            .legalPeriodical(LegalPeriodicalTransformer.transformToDTO(legalPeriodical))
            .build();

    // Reference needs to be saved manually as the source has no full cascading.
    var saveReference = referenceRepository.save(reference);

    // add status and source
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .reference(reference)
                            .value(SourceValue.Z)
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
                            .id(saveReference.getId())
                            .referenceType(ReferenceType.CASELAW)
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
                            .primaryReference(true)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .createdByReference(saveReference.getId())
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
        saveReference.getId(),
        firstEditionReferences.get(0).documentationUnit().getCreatedByReference());

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .hasSize(1)
        .satisfies(
            list -> {
              assertThat(list.get(0).id()).isEqualTo(saveReference.getId());
              assertThat(list.get(0).citation()).isEqualTo("ABC 2024, 3");
              assertThat(list.get(0).documentationUnit().getCreatedByReference())
                  .isEqualTo(saveReference.getId());
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testGetEdition_withDocUnitCreatedByLiteratureReference_shouldSucceed()
      throws DocumentationUnitNotExistsException {

    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    var literatureReference =
        PassiveCitationUliDTO.builder()
            .rank(1)
            .sourceLegalPeriodicalRawValue("ABC")
            .sourceCitation("ABC 2024, 3")
            .sourceAuthor("author")
            .target(docUnit)
            .sourceLegalPeriodical(LegalPeriodicalTransformer.transformToDTO(legalPeriodical))
            .build();

    // Reference needs to be saved manually as the source has no full cascading.
    var saveReference = passiveCitationUliRepository.save(literatureReference);

    // add status and source
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .literatureReference(literatureReference)
                            .value(SourceValue.Z)
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
                            .id(saveReference.getId())
                            .referenceType(ReferenceType.LITERATURE)
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .createdByReference(saveReference.getId())
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
        saveReference.getId(),
        firstEditionReferences.get(0).documentationUnit().getCreatedByReference());

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").literatureReferences())
        .hasSize(1)
        .satisfies(
            list -> {
              assertThat(list.get(0).id()).isEqualTo(saveReference.getId());
              assertThat(list.get(0).citation()).isEqualTo("ABC 2024, 3");
              assertThat(list.get(0).documentationUnit().getCreatedByReference())
                  .isEqualTo(saveReference.getId());
            });

    // clean up
    repository.save(edition.toBuilder().references(List.of()).build());
  }

  @Test
  void testDeleteReference_shouldCleanupDocUnitSource() throws DocumentationUnitNotExistsException {
    // create skeleton doc unit to retrieve ID
    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()),
            "DOC_NUMBER");

    var reference =
        ReferenceDTO.builder()
            .documentationUnitRank(1)
            .legalPeriodicalRawValue("ABC")
            .citation("ABC 2024, 3")
            .documentationUnit(docUnit)
            .legalPeriodical(LegalPeriodicalTransformer.transformToDTO(legalPeriodical))
            .build();

    // Reference needs to be saved manually as the source has no full cascading.
    var savedReference = referenceRepository.save(reference);

    // add status and source
    FileNumberDTO fileNumber = EntityBuilderTestUtil.createTestFileNumberDTO();
    fileNumber.setDocumentationUnit(docUnit);
    documentationUnitRepository.save(
        docUnit.toBuilder()
            .fileNumbers(List.of(fileNumber))
            .source(
                new ArrayList<>(
                    List.of(
                        SourceDTO.builder()
                            .rank(1)
                            .reference(reference)
                            .value(SourceValue.Z)
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
                            .id(savedReference.getId())
                            .referenceType(ReferenceType.CASELAW)
                            .citation("ABC 2024, 3")
                            .legalPeriodicalRawValue("ABC")
                            .primaryReference(true)
                            .documentationUnit(
                                RelatedDocumentationUnit.builder()
                                    .uuid(docUnit.getId())
                                    .documentNumber("DOC_NUMBER")
                                    .createdByReference(savedReference.getId())
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
                    false,
                    Pageable.ofSize(1))
                .iterator()
                .next()
                .getCreatedByReference())
        .isNull();

    assertThat(documentationUnitService.getByDocumentNumber("DOC_NUMBER").caselawReferences())
        .isEmpty();
  }
}
