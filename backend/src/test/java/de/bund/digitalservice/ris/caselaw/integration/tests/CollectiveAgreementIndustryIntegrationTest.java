package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCollectiveAgreementIndustryRepository;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CollectiveAgreementIndustryIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @Autowired
  private DatabaseCollectiveAgreementIndustryRepository
      databaseCollectiveAgreementIndustryRepository;

  @Test
  void testGetAllCollectiveAgreementIndustries() {
    assertThat(databaseCollectiveAgreementIndustryRepository.findAll()).hasSize(24);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/collective-agreement-industries")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CollectiveAgreementIndustry[].class)
        .consumeWith(
            response -> {
              CollectiveAgreementIndustry[] body = response.getResponseBody();
              assertThat(body).hasSize(24);
            });
  }

  @Test
  void testGetCollectiveAgreementIndustriesWithSearchParam() {
    assertThat(databaseCollectiveAgreementIndustryRepository.findAll()).hasSize(24);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/collective-agreement-industries?q=industrie")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CollectiveAgreementIndustry[].class)
        .consumeWith(
            response -> {
              CollectiveAgreementIndustry[] body = response.getResponseBody();
              assertThat(body).hasSize(5);
              assertThat(body)
                  .extracting(CollectiveAgreementIndustry::label)
                  .containsExactly(
                      "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
                      "Gebäude-, Straßen- und Industriereinigung, Schornsteinfeger",
                      "Holzverarbeitende Industrie",
                      "Papier- und Druckindustrie",
                      "Textil-, Bekleidungs- und Schuhindustrie");
            });
  }
}
