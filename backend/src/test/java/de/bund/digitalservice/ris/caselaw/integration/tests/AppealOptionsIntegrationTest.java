package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppealStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppellantRepository;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppealOptionsIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @Autowired private DatabaseAppellantRepository databaseAppellantRepository;
  @Autowired private DatabaseAppealStatusRepository databaseAppealStatusRepository;

  @Test
  void testGetAppellantOptions() {
    assertThat(databaseAppellantRepository.findAll()).hasSize(4);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/appeal/appellants")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Appellant[].class)
        .consumeWith(
            response -> {
              Appellant[] body = response.getResponseBody();
              assertThat(body)
                  .hasSize(4)
                  .extracting(Appellant::value)
                  .containsExactly("Kläger", "Beklagter", "Sonstiger", "Keine Angabe");
            });
  }

  @Test
  void testGetAppealStatuses() {
    assertThat(databaseAppealStatusRepository.findAll()).hasSize(8);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/appeal/statuses")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AppealStatus[].class)
        .consumeWith(
            response -> {
              AppealStatus[] body = response.getResponseBody();
              assertThat(body)
                  .hasSize(8)
                  .extracting(AppealStatus::value)
                  .containsExactly(
                      "unzulässig",
                      "teilweise unzulässig",
                      "zulässig",
                      "unbegründet",
                      "teilweise unbegründet",
                      "begründet",
                      "sonstiges",
                      "keine Angabe");
            });
  }
}
