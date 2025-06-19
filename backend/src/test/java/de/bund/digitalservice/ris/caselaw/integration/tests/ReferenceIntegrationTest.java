package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
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
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReferenceIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseLegalPeriodicalRepository legalPeriodicalRepository;
  @Autowired private DatabaseDocumentTypeRepository documentTypeRepository;
  @Autowired private LegalPeriodicalEditionRepository editionRepository;
  @Autowired private DatabaseReferenceRepository referenceRepository;
  @Autowired private DatabaseDependentLiteratureCitationRepository literatureCitationRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890126";
  private DocumentType eanDocumentType;
  private LegalPeriodical bverwgeLegalPeriodical;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

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
    Decision decisionFromFrontend =
        Decision.builder()
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
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
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
    Decision decisionFromFrontend =
        Decision.builder()
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
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
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

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .caselawReferences(List.of())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
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

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .caselawReferences(List.of())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
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
