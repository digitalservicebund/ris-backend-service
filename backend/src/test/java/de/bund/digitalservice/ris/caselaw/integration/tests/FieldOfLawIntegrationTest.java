package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import tools.jackson.core.type.TypeReference;

@Sql(
    scripts = {"classpath:fields_of_law_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = {"classpath:fields_of_law_cleanup.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class FieldOfLawIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseFieldOfLawRepository repository;

  @Test
  void testGetAllFieldsOfLaw() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?pg=0&sz=20")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("identifier")
        .containsExactly(
            "AB-01",
            "AB-01-01",
            "CD",
            "CD-01",
            "CD-02",
            "FL",
            "FL-01",
            "FL-01-01",
            "FL-02",
            "FL-03",
            "FL-04",
            "FO");
  }

  @Test
  void testGetFieldsOfLawByIdentifier() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?identifier=FL-01&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01", "FL-01-01");
  }

  @Test
  void testGetFieldsOfLawBySearchTerms() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=other text&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("CD");
  }

  @Test
  void testGetFieldsOfLawByNormsQuery_OnlyNormText() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=abc&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactlyInAnyOrder("FL", "AB-01");
  }

  @ParameterizedTest
  @ValueSource(strings = {"aber hallo § 12", "aber", "ABER HALLO"})
  void shouldCallExactNormPredicateForWildcardNorm(String wildcardNormSearch) {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + wildcardNormSearch + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactlyInAnyOrder("CD-02");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "§ 123", // paragraph
      })
  void testGetFieldsOfLawByNormsQuery_withoutAbbreviation(String query) {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + query + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("AB-01", "AB-01-01", "CD-02");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "abc § 123", // norm followed by paragraph
        "abc § 12", // norm followed by incomplete paragraph
      })
  void testGetFieldsOfLawByNormsQuery_withAbbreviatios(String query) {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=" + query + "&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL");
  }

  @Test
  void testGetFieldsOfLawByNormAndIdentifier() {
    Slice<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?norm=def&identifier=fl&pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01");
  }

  @Test
  void testFindByIdentifierAndDescription() {
    SliceTestImpl<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?identifier=FL&q=multiple cats&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01-01");
  }

  @Test
  void testFindByDescriptionAndNorm() {
    SliceTestImpl<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw?q=some text&norm=§ 123&pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("AB-01");
  }

  @Test
  void testGetFieldsOfLawByIdentifierSearch() {
    List<FieldOfLaw> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/fieldsoflaw/search-by-identifier?q=FL-01&sz=200&pg=0")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<FieldOfLaw>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody).extracting("identifier").containsExactly("FL-01", "FL-01-01");
  }

  @Test
  void testGetParentlessChildrenForFieldOfLawByNorms() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/root/children")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<FieldOfLaw>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactlyInAnyOrder("AB-01", "CD", "FL", "FO"));
  }

  @Test
  void testGetChildrenForFieldOfLawNumber() {
    // TODO: order by rank
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/FL/children")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<FieldOfLaw>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("identifier")
                    .containsExactlyInAnyOrder("FL-01", "FL-02", "FL-03", "FL" + "-04"));
  }

  @Test
  void testGetParentForFieldOfLaw() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/FL-01-01/tree")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FieldOfLaw.class)
        .consumeWith(
            response -> {
              FieldOfLaw field = response.getResponseBody();
              assertThat(field).isNotNull();
              assertThat(field.identifier()).isEqualTo("FL");
              assertThat(field.children()).hasSize(1);
              FieldOfLaw child = field.children().get(0);
              assertThat(child.identifier()).isEqualTo("FL-01");
              assertThat(child.children()).hasSize(1);
              child = child.children().get(0);
              assertThat(child.identifier()).isEqualTo("FL-01-01");
              assertThat(child.children()).isEmpty();
            });
  }
}
