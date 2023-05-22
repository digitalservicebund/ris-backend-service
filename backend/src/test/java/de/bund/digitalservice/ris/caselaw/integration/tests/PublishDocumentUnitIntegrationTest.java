package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.XmlEMailPublishService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseXmlMailRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresXmlMailRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.XmlMailDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublishState;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresXmlMailRepositoryImpl.class,
      XmlEMailPublishService.class,
      MockXmlExporter.class,
      FlywayConfig.class,
      PostgresConfig.class
    },
    controllers = {DocumentUnitController.class})
class PublishDocumentUnitIntegrationTest {
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

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String DELIVER_DATE =
      LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);

  @Autowired private WebTestClient webClient;

  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseXmlMailRepository xmlMailRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private HttpMailSender mailSender;

  @AfterEach
  void cleanUp() {
    xmlMailRepository.deleteAll().block();
    repository.deleteAll().block();
  }

  @Test
  void testPublishDocumentUnit() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO).block();
    XmlMailDTO expectedXmlMailDTO =
        new XmlMailDTO(
            1L,
            savedDocumentUnitDTO.getId(),
            "exporter@neuris.de",
            "id=juris name=NeuRIS da=R df=X dt=N mod=T ld=" + DELIVER_DATE + " vg=Testvorgang",
            "xml",
            "200",
            "message 1|message 2",
            "test.xml",
            null,
            PublishState.SENT);
    XmlMail expectedXmlMail =
        new XmlMail(
            documentUnitUuid1,
            "exporter@neuris.de",
            "id=juris name=NeuRIS da=R df=X dt=N mod=T ld=" + DELIVER_DATE + " vg=Testvorgang",
            "xml",
            "200",
            List.of("message 1", "message 2"),
            "text.xml",
            null,
            PublishState.SENT);
    XmlMailResponse expectedXmlResultObject =
        new XmlMailResponse(documentUnitUuid1, expectedXmlMail);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1 + "/publish")
        .contentType(MediaType.TEXT_PLAIN)
        .bodyValue("exporter@neuris.de")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlMailResponse.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("publishDate")
                  .isEqualTo(expectedXmlResultObject);
            });

    List<XmlMailDTO> xmlMailList = xmlMailRepository.findAll().collectList().block();
    assertThat(xmlMailList).hasSize(1);
    XmlMailDTO xmlMail = xmlMailList.get(0);
    assertThat(xmlMail)
        .usingRecursiveComparison()
        .ignoringFields("publishDate", "id")
        .isEqualTo(expectedXmlMailDTO);
  }

  @Test
  void testGetLastPublishedXml() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO).block();

    XmlMailDTO xmlMailDTO =
        new XmlMailDTO(
            null,
            savedDocumentUnitDTO.getId(),
            "exporter@neuris.de",
            "mailSubject",
            "xml",
            "200",
            "message 1|message 2",
            "test.xml",
            Instant.now(),
            PublishState.SENT);
    xmlMailRepository.save(xmlMailDTO).block();

    XmlMail expectedXmlMail =
        new XmlMail(
            documentUnitUuid1,
            "exporter@neuris.de",
            "mailSubject",
            "xml",
            "200",
            List.of("message 1", "message 2"),
            "text.xml",
            null,
            PublishState.SENT);
    XmlMailResponse expectedXmlResultObject =
        new XmlMailResponse(documentUnitUuid1, expectedXmlMail);

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1 + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlMailResponse.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("publishDate")
                  .isEqualTo(expectedXmlResultObject);
            });
  }
}
