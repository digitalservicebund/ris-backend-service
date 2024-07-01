package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.XmlEMailPublishService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabasePublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseXmlPublicationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresXmlPublicationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublicationReportDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.XmlPublicationDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationHistoryRecord;
import de.bund.digitalservice.ris.caselaw.domain.PublicationHistoryRecordType;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      PostgresMigrationRepositoryImpl.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresXmlPublicationRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      XmlEMailPublishService.class,
      DatabaseDocumentUnitStatusService.class,
      MockXmlExporter.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
@TestPropertySource(
    properties = {
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.recipientAddress=neuris@example.com"
    })
class PublishDocumentUnitIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

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
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseXmlPublicationRepository xmlPublicationRepository;
  @Autowired private DatabaseStatusRepository documentUnitStatusRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabasePublicationReportRepository databasePublishReportRepository;

  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private HttpMailSender mailSender;
  @MockBean DocxConverterService docxConverterService;
  @MockBean AttachmentService attachmentService;

  private DocumentationOfficeDTO docOffice;

  @BeforeEach
  void setUp() {
    docOffice = documentationOfficeRepository.findByAbbreviation("DS");
  }

  @AfterEach
  void cleanUp() {
    xmlPublicationRepository.deleteAll();
    repository.deleteAll();
    documentUnitStatusRepository.deleteAll();
    databasePublishReportRepository.deleteAll();
  }

  @Test
  void testPublishDocumentUnit() {
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .decisionDate(LocalDate.now())
            .build();
    DocumentationUnitDTO savedDocumentUnitDTO = repository.saveAndFlush(documentUnitDTO);

    assertThat(repository.findAll()).hasSize(1);

    XmlPublicationDTO expectedXmlPublicationDTO =
        XmlPublicationDTO.builder()
            .documentUnitId(savedDocumentUnitDTO.getId())
            .receiverAddress("neuris@example.com")
            .mailSubject(
                "id=juris name=test-user da=R df=X dt=N mod=T ld="
                    + DELIVER_DATE
                    + " vg="
                    + savedDocumentUnitDTO.getDocumentNumber())
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .build();

    XmlPublication expectedXmlResultObject =
        XmlPublication.builder()
            .documentUnitUuid(savedDocumentUnitDTO.getId())
            .receiverAddress("neuris@example.com")
            .mailSubject(
                "id=juris name=test-user da=R df=X dt=N mod=T ld="
                    + DELIVER_DATE
                    + " vg="
                    + savedDocumentUnitDTO.getDocumentNumber())
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("message 1", "message 2"))
            .fileName("test.xml")
            .build();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentUnitDTO.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlPublication.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("publishDate")
                    .isEqualTo(expectedXmlResultObject));

    List<XmlPublicationDTO> xmlPublicationList = xmlPublicationRepository.findAll();
    assertThat(xmlPublicationList).hasSize(1);
    XmlPublicationDTO xmlPublicationDTO = xmlPublicationList.get(0);
    assertThat(xmlPublicationDTO)
        .usingRecursiveComparison()
        .ignoringFields("publishDate", "id")
        .isEqualTo(expectedXmlPublicationDTO);

    StatusDTO statusList =
        documentUnitStatusRepository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(
            savedDocumentUnitDTO);

    assertThat(statusList.getPublicationStatus()).isEqualTo(PUBLISHING);
    assertThat(statusList.getCreatedAt()).isEqualTo(xmlPublicationDTO.getPublishDate());
    assertThat(statusList.getIssuerAddress()).isEqualTo("test@test.com");
  }

  @Test
  void testPublishDocumentUnitWithNotAllMandatoryFieldsFilled_shouldNotUpdateStatus() {
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO);
    assertThat(repository.findAll()).hasSize(1);

    documentUnitStatusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(savedDocumentUnitDTO)
            .issuerAddress("test1@test.com")
            .publicationStatus(PublicationStatus.UNPUBLISHED)
            .build());
    assertThat(documentUnitStatusRepository.findAll()).hasSize(1);

    XmlPublication xmlPublication =
        XmlPublication.builder()
            .documentUnitUuid(savedDocumentUnitDTO.getId())
            .statusCode("400")
            .statusMessages(List.of("message 1", "message 2"))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentUnitDTO.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlPublication.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("publishDate")
                    .isEqualTo(xmlPublication));

    List<XmlPublicationDTO> xmlPublicationList = xmlPublicationRepository.findAll();
    assertThat(xmlPublicationList).isEmpty();

    List<StatusDTO> statusList = documentUnitStatusRepository.findAll();
    assertThat(statusList).hasSize(1);
    assertThat(statusList.get(0).getPublicationStatus()).isEqualTo(PublicationStatus.UNPUBLISHED);
  }

  @Test
  void testGetLastPublishedXml() {
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO);

    XmlPublicationDTO xmlPublicationDTO =
        XmlPublicationDTO.builder()
            .documentUnitId(savedDocumentUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .publishDate(Instant.now())
            .build();
    xmlPublicationRepository.save(xmlPublicationDTO);

    XmlPublication expectedXmlPublication =
        XmlPublication.builder()
            .documentUnitUuid(savedDocumentUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("message 1", "message 2"))
            .fileName("text.xml")
            .build();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentUnitDTO.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(XmlPublication[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("publishDate")
                  .isEqualTo(expectedXmlPublication);
            });
  }

  @Test
  void testPublicationHistoryWithXmlAndReport() {
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentUnitDTO = repository.save(documentUnitDTO);

    Instant publishDate = Instant.now();
    xmlPublicationRepository.save(
        XmlPublicationDTO.builder()
            .documentUnitId(savedDocumentUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .publishDate(publishDate)
            .build());

    Instant receivedDate = publishDate.plus(1, ChronoUnit.HOURS);
    databasePublishReportRepository.save(
        PublicationReportDTO.builder()
            .documentUnitId(savedDocumentUnitDTO.getId())
            .content("<HTML>success!</HTML>")
            .receivedDate(receivedDate)
            .build());

    List<? extends PublicationHistoryRecord> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/" + savedDocumentUnitDTO.getId() + "/publish")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<? extends PublicationHistoryRecord>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBody).hasSize(2);
    assertThat(responseBody.get(0)).isInstanceOf(PublicationReport.class);
    PublicationReport publicationReport = (PublicationReport) responseBody.get(0);
    assertThat(publicationReport.content()).isEqualTo("<HTML>success!</HTML>");
    assertThat(publicationReport.getDate()).isCloseTo(receivedDate, within(1, ChronoUnit.MILLIS));
    assertThat(publicationReport.getType())
        .isEqualTo(PublicationHistoryRecordType.PUBLICATION_REPORT);
    assertThat(responseBody.get(1)).isInstanceOf(XmlPublication.class);
    XmlPublication xmlPublication = (XmlPublication) responseBody.get(1);
    assertThat(xmlPublication.xml()).isEqualTo("xml");
    assertThat(xmlPublication.getDate()).isCloseTo(publishDate, within(1, ChronoUnit.MILLIS));
    assertThat(xmlPublication.getType()).isEqualTo(PublicationHistoryRecordType.PUBLICATION);
  }
}
