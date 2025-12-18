package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DocumentationOfficeIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationOfficeRepository repository;

  @Test
  void testGetAllOffices() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentationoffices")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentationOffice>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("abbreviation")
                    .containsExactly(
                        "BAG", "BFH", "BGH", "BMJ", "BPatG", "BSG", "BVerfG", "BVerwG", "BZSt",
                        "CC-RIS", "DS", "juris", "OVGNW", "VVBund"));
  }

  @Test
  void testGetFilteredOffices() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentationoffices?q=B")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentationOffice>>() {})
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("abbreviation")
                    .containsExactly(
                        "BVerfG", "BZSt", "BVerwG", "BFH", "BAG", "BSG", "BMJ", "BPatG", "BGH"));
  }
}
