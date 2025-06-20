package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseKeywordRepository;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:keyword_init.sql"})
@Sql(
    scripts = {"classpath:keyword_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class DocumentationUnitKeywordIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseKeywordRepository keywordRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();

  @Test
  void testGetAllKeywordsForDocumentationUnit_withoutKeywords_shouldReturnEmptyList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr003")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .isEmpty());
  }

  @Test
  void testGetAllKeywordsForDocumentationUnit_withKeywords_shouldReturnList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        // Todo replace InAnyOrder when ordered by rank
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1", "keyword2"));
  }

  @Test
  void testAddKeywordForDocumentationUnit_shouldReturnListWithAllKeywords() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .keywords(List.of("keyword1", "keyword2", "keyword3"))
                    .build())
            .build();

    assertThat(keywordRepository.findAll()).hasSize(2);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1", "keyword2", "keyword3"));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        // Todo replace InAnyOrder when ordered by rank
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1"));

    assertThat(keywordRepository.findAll()).hasSize(3);
  }

  @Test
  void
      testAddExistingKeywordForDocumentationUnit_shouldNotAddDuplicateKeywordAndReturnListWithAllKeywords() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        // Todo replace InAnyOrder when ordered by rank
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1", "keyword2"));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().keywords(List.of("keyword1", "keyword2")).build())
            .build();

    assertThat(keywordRepository.findAll()).hasSize(2);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1", "keyword2"));

    assertThat(keywordRepository.findAll()).hasSize(2);
  }

  @Test
  void testDeleteKeywordFromDocumentationUnit_shouldReturnListWithAllRemainingKeywords() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().keywords(List.of("keyword1")).build())
            .build();

    assertThat(keywordRepository.findAll()).hasSize(2);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().keywords())
                    .containsExactlyInAnyOrder("keyword1"));

    // Todo delete keywords, when no reference to any documentationunitId?
  }
}
