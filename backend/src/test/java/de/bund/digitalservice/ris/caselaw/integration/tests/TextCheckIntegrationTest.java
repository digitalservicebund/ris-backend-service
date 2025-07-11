package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseIgnoredTextCheckWordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRequest;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:text_check_init.sql"})
@Sql(
    scripts = {"classpath:text_check_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class TextCheckIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseIgnoredTextCheckWordRepository repository;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  DocumentationUnitDTO documentationUnitDTO;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890";

  @BeforeEach
  void setUp() {
    DocumentationOfficeDTO documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    documentationUnitDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository,
            documentationOffice,
            DEFAULT_DOCUMENT_NUMBER + Math.random() * 1000);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testAddAndRemoveLocalIgnore() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("abc"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(IgnoredTextCheckWord.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().id()).isNotNull();
              assertThat(response.getResponseBody().word()).isEqualTo("abc");
              assertThat(response.getResponseBody().type())
                  .isEqualTo(IgnoredTextCheckType.DOCUMENTATION_UNIT);
            });

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word/abc")
        .exchange()
        .expectStatus()
        .isOk();
  }

  // 'xyz' is added as a globally ignored word via SQL script
  // this test verifies that it can be added locally at doc unit level, too
  @Test
  void testAddLocalIgnore_canAddGloballyIgnoredWordsLocally() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnitDTO.getId()
                + "/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("xyz"))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void testAddAndRemoveGlobalIgnore() {
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/api/v1/caselaw/text-check/ignored-word")
        .bodyValue(new IgnoredTextCheckWordRequest("def"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(IgnoredTextCheckWord.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().id()).isNotNull();
              assertThat(response.getResponseBody().word()).isEqualTo("def");
              assertThat(response.getResponseBody().type()).isEqualTo(IgnoredTextCheckType.GLOBAL);
            });

    // assert the global word has been saved and the documentation office is set correctly
    assertThat(
            repository
                .findByDocumentationUnitIdOrByGlobalWords(null, List.of("def"))
                .getFirst()
                .getDocumentationOffice()
                .getId())
        .isEqualTo(docOffice.id());

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/text-check/ignored-word/def")
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(repository.findByDocumentationUnitIdOrByGlobalWords(null, List.of("def"))).isEmpty();
  }

  @Test
  void testAddGlobalIgnore_shouldNotBeAbleToAddTwice() {
    for (int i = 0; i < 2; i++) {
      risWebTestClient
          .withDefaultLogin()
          .post()
          .uri("/api/v1/caselaw/text-check/ignored-word")
          .bodyValue(new IgnoredTextCheckWordRequest("hij"))
          .exchange()
          .expectStatus()
          .isOk();
    }

    assertThat(repository.findByDocumentationUnitIdOrByGlobalWords(null, List.of("hij")))
        .hasSize(1);
  }

  @Test
  void testGlobalJdvIgnore_cantBeDeleted() {
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/text-check/ignored-word/uvw")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
