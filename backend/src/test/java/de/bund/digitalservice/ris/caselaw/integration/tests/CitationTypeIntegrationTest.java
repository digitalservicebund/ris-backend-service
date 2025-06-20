package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:citation_types_init.sql"})
@Sql(
    scripts = {"classpath:citation_types_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class CitationTypeIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseCitationTypeRepository citationTypeRepository;

  @AfterEach
  void cleanUp() {
    citationTypeRepository.deleteAll();
  }

  @Test
  void testGetAllCitationTypes() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/citationtypes")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CitationType[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("Abgrenzung");
              assertThat(response.getResponseBody()[1].label()).isEqualTo("Anschluss");
            });
  }

  @Test
  void testGetCitationTypeBySearchString() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/citationtypes?q=abg")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CitationType[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody()[0].label()).isEqualTo("Abgrenzung");
            });
  }
}
