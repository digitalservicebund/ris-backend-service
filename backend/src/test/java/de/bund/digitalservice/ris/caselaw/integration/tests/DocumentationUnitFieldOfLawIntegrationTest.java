package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@Sql(
    scripts = {"classpath:fields_of_law_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:fields_of_law_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class DocumentationUnitFieldOfLawIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @Test
  void testGetAllFieldsOfLawForDocumentationUnit_withoutFieldOfLawLinked_shouldReturnEmptyList() {
    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .isEmpty());
  }

  @Test
  void
      testGetAllFieldsOfLawForDocumentationUnit_withFirstFieldOfLawLinked_shouldReturnListWithLinkedFieldOfLaw() {

    var documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
            .build());
    documentationUnitFieldOfLawDTO.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO.setRank(1);
    documentationUnitDTO.setDocumentationUnitFieldsOfLaw(List.of(documentationUnitFieldOfLawDTO));

    documentationUnitRepository.save(documentationUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactly("FL-01"));
  }

  @Test
  void testGetAllFieldsOfLawForDocumentationUnit_shouldReturnSortedList() {
    var documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO1 =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO1.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
            .build());
    documentationUnitFieldOfLawDTO1.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO1.setRank(1);
    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO2 =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO2.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("6959af10-7355-4e22-858d-29a485189957"))
            .build());
    documentationUnitFieldOfLawDTO2.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO2.setRank(2);
    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO3 =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO3.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("93393410-0ab0-48ab-a61d-5056e440174a"))
            .build());
    documentationUnitFieldOfLawDTO3.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO3.setRank(3);
    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO4 =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO4.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("b4f9ee05-38ed-49c3-89d6-50141f031017"))
            .build());
    documentationUnitFieldOfLawDTO4.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO4.setRank(4);
    documentationUnitDTO.setDocumentationUnitFieldsOfLaw(
        List.of(
            documentationUnitFieldOfLawDTO1,
            documentationUnitFieldOfLawDTO2,
            documentationUnitFieldOfLawDTO3,
            documentationUnitFieldOfLawDTO4));

    documentationUnitRepository.save(documentationUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactly("FL-01", "AB-01", "FL-02", "CD-01"));
  }

  @Test
  void testAddFieldsOfLawForDocumentationUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    var documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    Decision decision =
        Decision.builder()
            .uuid(documentationUnitDTO.getId())
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

    assertThat(documentationUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .extracting("identifier")
                    .containsExactly("FL-01", "FL-02"));
  }

  @Test
  void
      testAddFieldsOfLawForDocumentationUnit_withNotExistingFieldOfLaw_shouldReturnListWithAllLinkedFieldOfLaw() {
    var documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    Decision decision =
        Decision.builder()
            .uuid(documentationUnitDTO.getId())
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

    assertThat(documentationUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void testRemoveFieldsOfLawForDocumentationUnit_shouldReturnListWithAllLinkedFieldOfLaw() {
    var documentationUnitDTO =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            documentationUnitRepository, documentationOfficeDTO, "docnr12345678");

    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO =
        new DocumentationUnitFieldOfLawDTO();
    documentationUnitFieldOfLawDTO.setFieldOfLaw(
        FieldOfLawDTO.builder()
            .id(UUID.fromString("71defe05-cd4d-43e5-a07e-06c611b81a26"))
            .build());
    documentationUnitFieldOfLawDTO.setDocumentationUnit(documentationUnitDTO);
    documentationUnitFieldOfLawDTO.setRank(1);
    documentationUnitDTO.setDocumentationUnitFieldsOfLaw(List.of(documentationUnitFieldOfLawDTO));

    documentationUnitRepository.save(documentationUnitDTO);

    Decision decision =
        Decision.builder()
            .uuid(documentationUnitDTO.getId())
            .documentNumber("docnr12345678")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().fieldsOfLaw(Collections.emptyList()).build())
            .build();

    assertThat(documentationUnitDTO).isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().fieldsOfLaw())
                    .isEmpty());
  }
}
