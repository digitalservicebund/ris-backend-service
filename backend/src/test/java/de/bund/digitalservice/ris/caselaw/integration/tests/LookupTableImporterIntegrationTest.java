package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterController;
import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      LookupTableImporterService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresJPAConfig.class
    },
    controllers = {LookupTableImporterController.class})
class LookupTableImporterIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private WebTestClient webClient;
  @Autowired private JPADocumentTypeRepository jpaDocumentTypeRepository;
  @Autowired private CourtRepository courtRepository;
  @Autowired private StateRepository stateRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    jpaDocumentTypeRepository.deleteAll();
    courtRepository.deleteAll().block();
    stateRepository.deleteAll().block();
  }

  @Test
  void shouldImportDocumentTypeLookupTableCorrectly() {
    String doktypXml =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <juris-table>
          <juris-doktyp id="7" aendkz="N" version="1.0">
            <jurisabk>ÄN</jurisabk>
            <dokumentart>N</dokumentart>
            <mehrfach>Ja</mehrfach>
            <bezeichnung>Änderungsnorm</bezeichnung>
          </juris-doktyp>
        </juris-table>""";

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/doktyp")
        .bodyValue(doktypXml)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .isEqualTo("Successfully imported the document type lookup table"));

    List<JPADocumentTypeDTO> list = jpaDocumentTypeRepository.findAll();
    assertThat(list).hasSize(1);
    JPADocumentTypeDTO documentTypeDTO = list.get(0);
    assertThat(documentTypeDTO.getId()).isEqualTo(7L);
    assertThat(documentTypeDTO.getJurisShortcut()).isEqualTo("ÄN");
    assertThat(documentTypeDTO.getDocumentType()).isEqualTo('N');
    assertThat(documentTypeDTO.getMultiple()).isEqualTo("Ja");
    assertThat(documentTypeDTO.getLabel()).isEqualTo("Änderungsnorm");
  }

  @Test
  void shouldImportCourtLookupTableCorrectly() {
    String gerichtdataXml =
        """
        <?xml version="1.0"?>
        <juris-table>
          <juris-gericht id="5" aenddatum_client="2022-01-01" aendkz="J" version="1.0">
            <gertyp>Gertyp123</gertyp>
            <gerort>Ort</gerort>
            <buland>BW</buland>
          </juris-gericht>
        </juris-table>""";

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/gerichtdata")
        .bodyValue(gerichtdataXml)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .isEqualTo("Successfully imported the court lookup table"));

    List<CourtDTO> courtDTOs = courtRepository.findAll().collectList().block();
    assertThat(courtDTOs).hasSize(1);
    CourtDTO courtDTO = courtDTOs.get(0);
    assertThat(courtDTO.getId()).isEqualTo(5L);
    assertThat(courtDTO.getChangedateclient()).isEqualTo("2022-01-01");
    assertThat(courtDTO.getChangeindicator()).isEqualTo('J');
    assertThat(courtDTO.getCourttype()).isEqualTo("Gertyp123");
    assertThat(courtDTO.getCourtlocation()).isEqualTo("Ort");
    assertThat(courtDTO.getFederalstate()).isEqualTo("BW");
  }

  @Test
  void shouldImportStateLookupTableCorrectly() {
    String bulandXml =
        """
        <?xml version="1.0"?>
        <juris-table>
          <juris-buland id="4" aendkz="N" version="1.0">
            <jurisabk>AB</jurisabk>
            <bezeichnung>Bezeichnung123</bezeichnung>
          </juris-buland>
        </juris-table>""";

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/buland")
        .bodyValue(bulandXml)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .isEqualTo("Successfully imported the state lookup table"));

    List<StateDTO> stateDTOS = stateRepository.findAll().collectList().block();
    assertThat(stateDTOS).hasSize(1);
    StateDTO stateDTO = stateDTOS.get(0);
    assertThat(stateDTO.getId()).isEqualTo(4L);
    assertThat(stateDTO.getChangeindicator()).isEqualTo('N');
    assertThat(stateDTO.getJurisshortcut()).isEqualTo("AB");
    assertThat(stateDTO.getLabel()).isEqualTo("Bezeichnung123");
  }
}
