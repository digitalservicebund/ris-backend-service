package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import tools.jackson.core.type.TypeReference;

@Sql(
    scripts = {"classpath:document_types.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:document_types_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class DocumentTypeIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DocumentTypeService documentTypeService;
  @Autowired private DatabaseDocumentTypeRepository documentTypeRepository;

  @Test
  void testGetAllDocumentTypes() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentType>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("label", "jurisShortcut")
                  .containsExactly(
                      Tuple.tuple("Amtsrechtliche Anordnung", "AmA"),
                      Tuple.tuple("Anordnung", "Ao"),
                      Tuple.tuple("Beschluss", "Bes"),
                      Tuple.tuple("Urteil", "Ur"));
            });
  }

  @Test
  void testGetAllCaselawPendingProceedingDocumentTypes() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?category=CASELAW_PENDING_PROCEEDING")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentType>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("label", "jurisShortcut")
                  .containsExactly(
                      Tuple.tuple("Amtsrechtliche Anordnung", "AmA"),
                      Tuple.tuple("Anhängiges Verfahren", "Anh"),
                      Tuple.tuple("Anordnung", "Ao"),
                      Tuple.tuple("Beschluss", "Bes"),
                      Tuple.tuple("Urteil", "Ur"));
            });
  }

  @Test
  void testGetAllDependantLiteratureDocumentTypes() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?category=DEPENDENT_LITERATURE")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentType>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("label", "jurisShortcut")
                  .containsExactly(
                      Tuple.tuple("Anmerkung", "Ean"),
                      Tuple.tuple("Entscheidungsbesprechung", "Ebs"));
            });
  }

  @Test
  void testGetDocumentTypesWithQuery() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?q=Anord")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentType>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("label", "jurisShortcut")
                  .containsExactly(
                      Tuple.tuple("Amtsrechtliche Anordnung", "AmA"),
                      Tuple.tuple("Anordnung", "Ao"));
            });
  }

  @Test
  void testGetDependantLiteratureDocumentTypesWithQuery() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?q=Ea&category=DEPENDENT_LITERATURE")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentType>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("label", "jurisShortcut")
                  .containsExactly(Tuple.tuple("Anmerkung", "Ean"));
            });
  }

  @Test
  void testGetPendingProceedingType_shouldReturnPendingProceedingType() {
    // Arrange
    List<DocumentTypeDTO> documentTypes = documentTypeRepository.findAll();
    assertThat(documentTypes).hasSize(8);

    DocumentTypeDTO anhDocumentType =
        documentTypes.stream()
            .filter(type -> "Anh".equals(type.getAbbreviation()))
            .findFirst()
            .get();

    var expectedPendingProceedingType = DocumentTypeTransformer.transformToDomain(anhDocumentType);

    // Act
    var actualPendingProceedingType = documentTypeService.getPendingProceedingType();

    // Assert
    assertThat(actualPendingProceedingType).isEqualTo(expectedPendingProceedingType);
  }
}
