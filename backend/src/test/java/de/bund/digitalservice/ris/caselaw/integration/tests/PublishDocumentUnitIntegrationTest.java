package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.XmlEMailPublishService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseXmlMailRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresPublishReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresXmlMailRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PublicationReportDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.XmlMailDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationLogEntryType;
import de.bund.digitalservice.ris.caselaw.domain.PublishState;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.hamcrest.Matchers;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresXmlMailRepositoryImpl.class,
      PostgresPublishReportRepositoryImpl.class,
      XmlEMailPublishService.class,
      DatabaseDocumentUnitStatusService.class,
      MockXmlExporter.class,
      FlywayConfig.class,
      PostgresConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class
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

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseXmlMailRepository xmlMailRepository;
  @Autowired private DatabaseDocumentUnitStatusRepository documentUnitStatusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabasePublicationReportRepository databasePublishReportRepository;

  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private HttpMailSender mailSender;
  @MockBean DocxConverterService docxConverterService;

  private UUID docOfficeUuid;

  @BeforeEach
  void setUp() {
    docOfficeUuid = documentationOfficeRepository.findByLabel("DigitalService").block().getId();
  }

  @AfterEach
  void cleanUp() {
    xmlMailRepository.deleteAll().block();
    repository.deleteAll().block();
    documentUnitStatusRepository.deleteAll().block();
    databasePublishReportRepository.deleteAll().block();
  }

  @Test
  void testPublishDocumentUnit() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentationOfficeId(docOfficeUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .decisionDate(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO).block();

    assertThat(repository.findAll().collectList().block()).hasSize(1);

    XmlMailDTO expectedXmlMailDTO =
        new XmlMailDTO(
            1L,
            savedDocumentUnitDTO.getId(),
            "neuris@example.com",
            "id=juris name=NeuRIS da=R df=X dt=N mod=T ld="
                + DELIVER_DATE
                + " vg="
                + savedDocumentUnitDTO.getDocumentnumber(),
            "xml",
            "200",
            "message 1|message 2",
            "test.xml",
            null,
            PublishState.SENT);
    XmlMail expectedXmlMail =
        new XmlMail(
            documentUnitUuid1,
            "neuris@example.com",
            "id=juris name=NeuRIS da=R df=X dt=N mod=T ld="
                + DELIVER_DATE
                + " vg="
                + savedDocumentUnitDTO.getDocumentnumber(),
            "xml",
            "200",
            List.of("message 1", "message 2"),
            "text.xml",
            null,
            PublishState.SENT);
    XmlMailResponse expectedXmlResultObject =
        new XmlMailResponse(documentUnitUuid1, expectedXmlMail);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1 + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlMailResponse.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("publishDate")
                    .isEqualTo(expectedXmlResultObject));

    List<XmlMailDTO> xmlMailList = xmlMailRepository.findAll().collectList().block();
    assertThat(xmlMailList).hasSize(1);
    XmlMailDTO xmlMail = xmlMailList.get(0);
    assertThat(xmlMail)
        .usingRecursiveComparison()
        .ignoringFields("publishDate", "id")
        .isEqualTo(expectedXmlMailDTO);

    List<DocumentUnitStatusDTO> statusList =
        documentUnitStatusRepository.findAll().collectList().block();
    DocumentUnitStatusDTO status = statusList.get(statusList.size() - 1);
    assertThat(status.getStatus()).isEqualTo(PUBLISHED);
    assertThat(status.getDocumentUnitId()).isEqualTo(documentUnitDTO.getUuid());
    assertThat(status.getCreatedAt()).isEqualTo(xmlMail.publishDate());
    assertThat(status.getIssuerAddress()).isEqualTo("test@test.com");
  }

  @Test
  void testPublishDocumentUnitWithNotAllMandatoryFieldsFilled_shouldNotUpdateStatus() {
    UUID documentUnitUuid = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid)
            .documentationOfficeId(docOfficeUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO).block();
    assertThat(repository.findAll().collectList().block()).hasSize(1);

    documentUnitStatusRepository
        .save(
            DocumentUnitStatusDTO.builder()
                .newEntry(true)
                .id(UUID.randomUUID())
                .documentUnitId(savedDocumentUnitDTO.getUuid())
                .issuerAddress("test1@test.com")
                .status(DocumentUnitStatus.UNPUBLISHED)
                .build())
        .block();
    assertThat(documentUnitStatusRepository.findAll().collectList().block()).hasSize(1);

    XmlMail expectedXmlMail =
        new XmlMail(
            documentUnitUuid,
            null,
            null,
            null,
            "400",
            List.of("message 1", "message 2"),
            "text.xml",
            null,
            PublishState.UNKNOWN);
    XmlMailResponse expectedXmlResultObject =
        new XmlMailResponse(documentUnitUuid, expectedXmlMail);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlMailResponse.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("publishDate")
                    .isEqualTo(expectedXmlResultObject));

    List<XmlMailDTO> xmlMailList = xmlMailRepository.findAll().collectList().block();
    assertThat(xmlMailList).isEmpty();

    List<DocumentUnitStatusDTO> statusList =
        documentUnitStatusRepository.findAll().collectList().block();
    assertThat(statusList).hasSize(1);
    assertThat(statusList.get(0).getStatus()).isEqualTo(DocumentUnitStatus.UNPUBLISHED);
  }

  @Test
  void testGetLastPublishedXml() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentationOfficeId(docOfficeUuid)
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
    XmlMailResponse[] expectedXmlResultObject = {
      new XmlMailResponse(documentUnitUuid1, expectedXmlMail)
    };

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1 + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlMailResponse[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("publishDate")
                  .isEqualTo(expectedXmlResultObject);
            });
  }

  @Test
  void testPublishLogWithXmlAndReport() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentationOfficeId(docOfficeUuid)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO).block();

    Instant publishDate = Instant.now();
    xmlMailRepository
        .save(
            new XmlMailDTO(
                null,
                savedDocumentUnitDTO.getId(),
                "exporter@neuris.de",
                "mailSubject",
                "xml",
                "200",
                "message 1|message 2",
                "test.xml",
                publishDate,
                PublishState.SENT))
        .block();

    Instant receivedDate = publishDate.plus(1, ChronoUnit.HOURS);
    databasePublishReportRepository
        .save(
            new PublicationReportDTO(
                UUID.randomUUID(),
                savedDocumentUnitDTO.getUuid(),
                "<HTML>success!</HTML>",
                receivedDate,
                true))
        .block();

    System.out.println(receivedDate.truncatedTo(ChronoUnit.MICROS).toString());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1 + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$")
        .isArray()
        .jsonPath("$[0].content")
        .isEqualTo("<HTML>success!</HTML>")
        .jsonPath("$[0].date")
        .value(o -> Matchers.containsString(receivedDate.toString().substring(0, 20)).matches(o))
        .jsonPath("$[0].type")
        .isEqualTo(PublicationLogEntryType.HTML.name())
        .jsonPath("$[1].xml")
        .isEqualTo("xml")
        .jsonPath("$[1].date")
        .value(o -> Matchers.containsString(publishDate.toString().substring(0, 20)).matches(o))
        .jsonPath("$[1].type")
        .isEqualTo(PublicationLogEntryType.XML.name())
        .consumeWith(System.out::println);
  }
}
