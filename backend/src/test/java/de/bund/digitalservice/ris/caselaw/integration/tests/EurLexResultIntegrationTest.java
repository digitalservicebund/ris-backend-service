package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.PageTestImpl;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@Sql(
    scripts = {"classpath:eurlex_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:eurlex_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings("java:S5976")
class EurLexResultIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @Test
  void getSearchResults() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults)
        .extracting("celex")
        .containsExactly("62017CA0578", "62017TA0577", "62017CB0576", "62017TB0575");
  }

  @Test
  void getSearchResults_withDocOfficeBGH() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withLogin("/BGH")
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withDocOfficeBFH() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withLogin("/BFH")
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017CB0576");
  }

  @Test
  void getSearchResults_withNotAllowedDocOffice() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withLogin("/CC-RIS")
            .get()
            .uri("/api/v1/caselaw/eurlex")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }

  @Test
  void getSearchResults_withParameterCourtTypeSetToEuG() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?court=EuG")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withParameterCourtTypeSetToEuGH() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?court=EuGH")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017CB0576");
  }

  @Test
  void getSearchResults_withParameterStartDate() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?start-date=2012-01-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017CA0578", "62017TA0577");
  }

  @Test
  void getSearchResults_withParameterStartDateAndEndDate() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?start-date=2012-01-01&end-date=2013-01-01")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterCelex() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017TA0577")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterCelexOnlyStartOf() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017T")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577", "62017TB0575");
  }

  @Test
  void getSearchResults_withParameterCelexForAnAssignedDecision() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?celex=62017CA0579")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }

  @Test
  void getSearchResults_withParameterFileNumberExactValue() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=T-88/25")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterFileNumberOnlyStartOf() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=T-88")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isTrue();
    List<SearchResult> searchResults = searchResultPage.getContent();
    assertThat(searchResults).extracting("celex").containsExactly("62017TA0577");
  }

  @Test
  void getSearchResults_withParameterFileNumberForAnAssignedDecision() {
    Page<SearchResult> searchResultPage =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/eurlex?file-number=C-12/25")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<PageTestImpl<SearchResult>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(searchResultPage.hasContent()).isFalse();
  }
}
