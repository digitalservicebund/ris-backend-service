package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SaveNormIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseNormReferenceRepository normRepository;
  @Autowired private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @AfterEach
  void cleanUp() {
    normRepository.deleteAll();
    repository.deleteAll();
    normAbbreviationRepository.deleteAll();
  }

  // TODO: write a test for add a document extension with a wrong shortcut

  @Test
  void testSaveNorm_withoutNorm() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOfficeDTO);

    Decision decisionFromFrontend = generateDocumentationUnit(dto.getId());

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).isEmpty();
            });
  }

  @Test
  void testSaveNorm_withOneNormAndNoChange() {
    NormAbbreviationDTO normAbbreviation = addNormToDB(2352);

    DocumentationUnitDTO savedDocumentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890124")
                .documentationOffice(documentationOfficeDTO)
                .normReferences(
                    List.of(
                        NormReferenceDTO.builder()
                            .rank(1)
                            .normAbbreviation(normAbbreviation)
                            .build())));

    Decision decisionFromFrontend = generateDocumentationUnit(savedDocumentationUnitDTO.getId());

    decisionFromFrontend
        .contentRelatedIndexing()
        .norms()
        .add(
            NormReference.builder()
                .normAbbreviation(NormAbbreviation.builder().id(normAbbreviation.getId()).build())
                .build());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(normAbbreviation.getId());
            });
  }

  /** Sorting by remove norm abbreviation of an existing norm reference */
  @Test
  void testSaveNorm_RISDEV2185() {
    var dbNormAbbreviation1 = addNormToDB(1);
    var dbNormAbbreviation2 = addNormToDB(2);

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOfficeDTO, "1234567890123");

    Decision decisionFromFrontend = generateDocumentationUnit(dto.getId());

    NormReference norm1 =
        NormReference.builder()
            .normAbbreviation(NormAbbreviation.builder().id(dbNormAbbreviation1.getId()).build())
            .build();
    NormReference norm2 =
        NormReference.builder()
            .normAbbreviation(NormAbbreviation.builder().id(dbNormAbbreviation2.getId()).build())
            .build();
    decisionFromFrontend.contentRelatedIndexing().norms().addAll(List.of(norm1, norm2));

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
              assertThat(response.getResponseBody().contentRelatedIndexing()).isNotNull();
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(dbNormAbbreviation1.getId());
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(1)
                          .normAbbreviation()
                          .id())
                  .isEqualTo(dbNormAbbreviation2.getId());
            });
  }

  private Decision generateDocumentationUnit(UUID uuid) {
    return Decision.builder()
        .uuid(uuid)
        .documentNumber("1234567890123")
        .coreData(CoreData.builder().documentationOffice(docOffice).build())
        .contentRelatedIndexing(ContentRelatedIndexing.builder().norms(new ArrayList<>()).build())
        .build();
  }

  private NormAbbreviationDTO addNormToDB(int index) {
    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .abbreviation("norm abbreviation " + index)
            .documentId((long) index)
            .build();
    return normAbbreviationRepository.saveAndFlush(normAbbreviationDTO);
  }
}
