package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterController;
import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
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
  @Autowired private DatabaseFieldOfLawRepository subjectFieldRepository;
  @Autowired private FieldOfLawKeywordRepository fieldOfLawKeywordRepository;
  @Autowired private NormRepository normRepository;
  @Autowired private FieldOfLawLinkRepository fieldOfLawLinkRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    jpaDocumentTypeRepository.deleteAll();
    courtRepository.deleteAll().block();
    stateRepository.deleteAll().block();
    subjectFieldRepository.deleteAll().block(); // will cascade delete the other 3 repo-contents
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

  @Test
  void shouldImportSubjectFieldLookupTableCorrectly() {
    NormDTO expectedNorm1 =
        NormDTO.builder()
            .subjectFieldId(2L)
            .abbreviation("normabk 2.1")
            .singleNormDescription("§ 2.1")
            .build();
    NormDTO expectedNorm2 =
        NormDTO.builder().subjectFieldId(2L).abbreviation("normabk 2.2").build();

    FieldOfLawKeywordDTO expectedKeyword1 =
        FieldOfLawKeywordDTO.builder().subjectFieldId(2L).value("schlagwort 2.1").build();
    FieldOfLawKeywordDTO expectedKeyword2 =
        FieldOfLawKeywordDTO.builder().subjectFieldId(2L).value("schlagwort 2.2").build();

    FieldOfLawDTO expectedLinkedField1 =
        FieldOfLawDTO.builder()
            .id(3L)
            .childrenCount(0)
            .changeIndicator('N')
            .subjectFieldNumber("ÄB-01-02")
            .build();
    FieldOfLawDTO expectedLinkedField2 =
        FieldOfLawDTO.builder()
            .id(4L)
            .childrenCount(0)
            .changeIndicator('N')
            .subjectFieldNumber("CD-01")
            .build();

    FieldOfLawDTO expectedParent =
        FieldOfLawDTO.builder()
            .id(1L)
            .childrenCount(1)
            .subjectFieldNumber("TS-01")
            .subjectFieldText("stext 1")
            .changeIndicator('N')
            .build();

    FieldOfLawDTO expectedChild =
        new FieldOfLawDTO(
            2L,
            0,
            1L,
            "2022-12-22",
            "2022-12-24",
            'J',
            "1.0",
            "TS-01-01",
            "Linked fields, valid: ÄB-01-02, CD-01, invalid: EF01, Gh-01, IJ-01-023, KL-01a",
            "navbez 2",
            List.of(expectedLinkedField1, expectedLinkedField2),
            Arrays.asList(expectedKeyword1, expectedKeyword2),
            Arrays.asList(expectedNorm1, expectedNorm2),
            false);

    String subjectFieldXml =
        """
            <?xml version="1.0"?>
            <juris-table>

                <juris-sachg id="2" aenddatum_mail="2022-12-22" aenddatum_client="2022-12-24" aendkz="J" version="1.0">
                    <sachgebiet>TS-01-01</sachgebiet>
                    <stext>Linked fields, valid: ÄB-01-02, CD-01, invalid: EF01, Gh-01, IJ-01-023, KL-01a</stext>
                    <navbez>navbez 2</navbez>
                    <norm>
                        <normabk>normabk 2.1</normabk>
                        <enbez>§ 2.1</enbez>
                    </norm>
                    <norm>
                        <normabk>normabk 2.2</normabk>
                    </norm>
                    <schlagwort>schlagwort 2.1</schlagwort>
                    <schlagwort>schlagwort 2.2</schlagwort>
                </juris-sachg>

                <juris-sachg id="1" aendkz="N">
                    <sachgebiet>TS-01-</sachgebiet>
                    <stext>stext 1</stext>
                </juris-sachg>

                <juris-sachg id="3" aendkz="N">
                    <sachgebiet>ÄB-01-02</sachgebiet>
                </juris-sachg>

                <juris-sachg id="4" aendkz="N">
                    <sachgebiet>CD-01</sachgebiet>
                </juris-sachg>

                <juris-sachg id="5" aendkz="N">
                    <sachgebiet>IJ-01-02</sachgebiet>
                </juris-sachg>

                <juris-sachg id="6" aendkz="N">
                    <sachgebiet>KL-01</sachgebiet>
                </juris-sachg>

            </juris-table>
            """;

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/lookuptableimporter/subjectField")
        .bodyValue(subjectFieldXml)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .isEqualTo("Successfully imported the subject field lookup table"));

    List<FieldOfLawDTO> fieldOfLawDTOS =
        subjectFieldRepository
            .findAllByOrderBySubjectFieldNumberAsc(Pageable.unpaged())
            .collectList()
            .block();
    List<FieldOfLawKeywordDTO> keywordDTOs =
        fieldOfLawKeywordRepository
            .findAllByOrderBySubjectFieldIdAscValueAsc()
            .collectList()
            .block();
    List<NormDTO> normDTOs =
        normRepository.findAllByOrderBySubjectFieldIdAscAbbreviationAsc().collectList().block();

    assertThat(fieldOfLawDTOS).hasSize(6);
    assertThat(keywordDTOs).hasSize(2);
    assertThat(normDTOs).hasSize(2);

    FieldOfLawDTO parent = fieldOfLawDTOS.get(4); // index due to alphabetical sorting
    FieldOfLawDTO child = fieldOfLawDTOS.get(5);

    List<FieldOfLawLinkDTO> linksRaw =
        fieldOfLawLinkRepository.findAllByFieldId(child.getId()).collectList().block();
    List<FieldOfLawDTO> linkedFields =
        linksRaw.stream()
            .map(
                fieldOfLawLinkDTO ->
                    subjectFieldRepository.findById(fieldOfLawLinkDTO.getLinkedFieldId()).block())
            .toList();

    child.setKeywords(keywordDTOs);
    child.setNorms(normDTOs);
    child.setLinkedFields(linkedFields);

    assertThat(parent).usingRecursiveComparison().isEqualTo(expectedParent);
    assertThat(child)
        .usingRecursiveComparison()
        .ignoringFields("norms.id", "keywords.id")
        .isEqualTo(expectedChild);
  }
}
