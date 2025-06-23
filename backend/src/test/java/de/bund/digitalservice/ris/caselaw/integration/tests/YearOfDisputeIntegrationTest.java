package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class YearOfDisputeIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890123";

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testDuplicatedYearsAreNotAllowed() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, DEFAULT_DOCUMENT_NUMBER);

    List<Year> years = List.of(Year.now(), Year.now());
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
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
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(1);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .hasToString(Year.now().toString());
            });
  }

  @Test
  void testYearsSorting() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, DEFAULT_DOCUMENT_NUMBER);

    var firstYear = Year.parse("2022");
    var secondYear = Year.parse("2010");
    var lastYear = Year.parse("2009");

    List<Year> years = List.of(firstYear, secondYear, lastYear);
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(years).build())
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
              assertThat(response.getResponseBody().coreData().yearsOfDispute()).hasSize(3);
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(0).toString())
                  .hasToString(firstYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(1).toString())
                  .hasToString(secondYear.toString());
              assertThat(response.getResponseBody().coreData().yearsOfDispute().get(2).toString())
                  .hasToString(lastYear.toString());
            });
  }

  @Test
  void testFutureYearsAreNotAllowed() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, DEFAULT_DOCUMENT_NUMBER);

    var futureYear = Year.now().plusYears(1);
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().yearsOfDispute(List.of(futureYear)).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
}
