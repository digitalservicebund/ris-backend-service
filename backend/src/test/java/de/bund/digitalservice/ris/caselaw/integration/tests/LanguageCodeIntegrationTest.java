package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLanguageCodeRepository;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LanguageCodeIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @Autowired private DatabaseLanguageCodeRepository databaseLanguageCodeRepository;

  @Test
  void testGetAllLanguageCodes() {
    assertThat(databaseLanguageCodeRepository.findAll().size()).isEqualTo(195);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/languagecodes")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LanguageCode[].class)
        .consumeWith(
            response -> {
              LanguageCode[] body = response.getResponseBody();
              assertThat(body).hasSize(195);
            });
  }

  @Test
  void testGetLanguageCodesWithSearchParam() {
    assertThat(databaseLanguageCodeRepository.findAll().size()).isEqualTo(195);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/languagecodes?q=deu")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LanguageCode[].class)
        .consumeWith(
            response -> {
              LanguageCode[] body = response.getResponseBody();
              assertThat(body).hasSize(1);
              assertThat(body[0].label()).isEqualTo("Deutsch");
            });
  }

  @Test
  void testGetLanguageCodesWithSizeLimit() {
    assertThat(databaseLanguageCodeRepository.findAll().size()).isEqualTo(195);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/languagecodes?sz=5")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LanguageCode[].class)
        .consumeWith(
            response -> {
              LanguageCode[] body = response.getResponseBody();
              assertThat(body).hasSize(5);
            });
  }
}
