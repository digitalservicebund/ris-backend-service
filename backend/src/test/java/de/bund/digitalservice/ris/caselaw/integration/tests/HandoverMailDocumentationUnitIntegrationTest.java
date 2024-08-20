package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationOfficeUserGroupService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.HandoverMailService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseHandoverReportRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseXmlHandoverMailRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverMailDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HandoverReportDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EventRecord;
import de.bund.digitalservice.ris.caselaw.domain.EventType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverReport;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
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
      DocumentationUnitService.class,
      de.bund.digitalservice.ris.caselaw.domain.HandoverService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      KeycloakUserService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresHandoverRepositoryImpl.class,
      PostgresHandoverReportRepositoryImpl.class,
      HandoverMailService.class,
      DatabaseDocumentationUnitStatusService.class,
      MockXmlExporter.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentationUnitController.class})
@TestPropertySource(
    properties = {
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.recipientAddress=neuris@example.com"
    })
class HandoverMailDocumentationUnitIntegrationTest {
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
  @Autowired private DatabaseXmlHandoverMailRepository xmlHandoverRepository;
  @Autowired private DatabaseStatusRepository statusRepository;
  @Autowired private DatabaseHandoverReportRepository databaseHandoverReportRepository;

  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean
  private DatabaseDocumentationOfficeUserGroupService databaseDocumentationOfficeUserGroupService;

  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private HttpMailSender mailSender;
  @MockBean DocxConverterService docxConverterService;
  @MockBean AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;

  @MockBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private DocumentationOfficeDTO docOffice;

  @BeforeEach
  void setUp() {
    docOffice = documentationOfficeRepository.findByAbbreviation("DS");
    doReturn(
            List.of(
                DocumentationOfficeUserGroup.builder()
                    .docOffice(
                        DocumentationOffice.builder()
                            .abbreviation(docOffice.getAbbreviation())
                            .build())
                    .userGroupPathName("/DS")
                    .build()))
        .when(databaseDocumentationOfficeUserGroupService)
        .getUserGroups();
  }

  @AfterEach
  void cleanUp() {
    xmlHandoverRepository.deleteAll();
    repository.deleteAll();
    statusRepository.deleteAll();
    databaseHandoverReportRepository.deleteAll();
  }

  @Test
  void testHandoverDocumentationUnit() {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .decisionDate(LocalDate.now())
            .build();
    DocumentationUnitDTO savedDocumentationUnitDTO = repository.saveAndFlush(documentationUnitDTO);

    assertThat(repository.findAll()).hasSize(1);

    var initialStatus =
        statusRepository.save(
            StatusDTO.builder()
                .publicationStatus(UNPUBLISHED)
                .documentationUnitDTO(documentationUnitDTO)
                .build());

    HandoverMailDTO expectedHandoverMailDTO =
        HandoverMailDTO.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .receiverAddress("neuris@example.com")
            .mailSubject(
                "id=juris name=test-user da=R df=X dt=N mod=T ld="
                    + DELIVER_DATE
                    + " vg="
                    + savedDocumentationUnitDTO.getDocumentNumber())
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .issuerAddress("test@test.com")
            .build();

    HandoverMail expectedXmlResultObject =
        HandoverMail.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .receiverAddress("neuris@example.com")
            .mailSubject(
                "id=juris name=test-user da=R df=X dt=N mod=T ld="
                    + DELIVER_DATE
                    + " vg="
                    + savedDocumentationUnitDTO.getDocumentNumber())
            .xml("xml")
            .success(true)
            .statusMessages(List.of("message 1", "message 2"))
            .fileName("test.xml")
            .issuerAddress("test@test.com") // set by AuthUtils
            .build();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId() + "/handover")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HandoverMail.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("handoverDate")
                    .isEqualTo(expectedXmlResultObject));

    List<HandoverMailDTO> xmlPublicationList = xmlHandoverRepository.findAll();
    assertThat(xmlPublicationList).hasSize(1);
    HandoverMailDTO handoverMailDTO = xmlPublicationList.get(0);
    assertThat(handoverMailDTO)
        .usingRecursiveComparison()
        .ignoringFields("sentDate", "id")
        .isEqualTo(expectedHandoverMailDTO);

    StatusDTO lastStatus =
        statusRepository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(
            savedDocumentationUnitDTO);

    // publication status should not change because handover does not change the status
    assertThat(lastStatus.getPublicationStatus()).isEqualTo(initialStatus.getPublicationStatus());
    assertThat(lastStatus.getCreatedAt()).isEqualTo(initialStatus.getCreatedAt());
  }

  @Test
  void testHandoverDocumentationUnitWithNotAllMandatoryFieldsFilled_shouldNotUpdateStatus() {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentationUnitDTO = repository.save(documentationUnitDTO);
    assertThat(repository.findAll()).hasSize(1);

    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(savedDocumentationUnitDTO)
            .publicationStatus(PublicationStatus.UNPUBLISHED)
            .build());
    assertThat(statusRepository.findAll()).hasSize(1);

    HandoverMail xmlPublication =
        HandoverMail.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .success(false)
            .statusMessages(List.of("message 1", "message 2"))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId() + "/handover")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HandoverMail.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .usingRecursiveComparison()
                    .ignoringFields("handoverDate")
                    .isEqualTo(xmlPublication));

    List<HandoverMailDTO> xmlPublicationList = xmlHandoverRepository.findAll();
    assertThat(xmlPublicationList).isEmpty();

    List<StatusDTO> statusList = statusRepository.findAll();
    assertThat(statusList).hasSize(1);
    assertThat(statusList.get(0).getPublicationStatus()).isEqualTo(PublicationStatus.UNPUBLISHED);
  }

  @Test
  void testGetLastXmlHandoverMail() {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentationUnitDTO = repository.save(documentationUnitDTO);

    HandoverMailDTO handoverMailDTO =
        HandoverMailDTO.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .sentDate(Instant.now())
            .build();
    xmlHandoverRepository.save(handoverMailDTO);

    HandoverMail expectedXmlPublication =
        HandoverMail.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .success(true)
            .statusMessages(List.of("message 1", "message 2"))
            .fileName("text.xml")
            .build();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId() + "/handover")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HandoverMail[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .usingRecursiveComparison()
                  .ignoringFields("handoverDate")
                  .isEqualTo(expectedXmlPublication);
            });
  }

  @Test
  void testPublicationHistoryWithXmlAndReport() {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .documentationOffice(docOffice)
            .documentNumber("docnr12345678")
            .build();
    DocumentationUnitDTO savedDocumentationUnitDTO = repository.save(documentationUnitDTO);

    Instant creationDate = Instant.now();
    xmlHandoverRepository.save(
        HandoverMailDTO.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .receiverAddress("exporter@neuris.de")
            .mailSubject("mailSubject")
            .xml("xml")
            .statusCode("200")
            .statusMessages("message 1|message 2")
            .fileName("test.xml")
            .sentDate(creationDate)
            .build());

    Instant receivedDate = creationDate.plus(1, ChronoUnit.HOURS);
    databaseHandoverReportRepository.save(
        HandoverReportDTO.builder()
            .documentationUnitId(savedDocumentationUnitDTO.getId())
            .content("<HTML>success!</HTML>")
            .receivedDate(receivedDate)
            .build());

    List<? extends EventRecord> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/" + savedDocumentationUnitDTO.getId() + "/handover")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<? extends EventRecord>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBody).hasSize(2);
    assertThat(responseBody.get(0)).isInstanceOf(HandoverReport.class);
    HandoverReport handoverReport = (HandoverReport) responseBody.get(0);
    assertThat(handoverReport.content()).isEqualTo("<HTML>success!</HTML>");
    assertThat(handoverReport.getDate()).isCloseTo(receivedDate, within(1, ChronoUnit.MILLIS));
    assertThat(handoverReport.getType()).isEqualTo(EventType.HANDOVER_REPORT);
    assertThat(responseBody.get(1)).isInstanceOf(HandoverMail.class);
    HandoverMail xmlPublication = (HandoverMail) responseBody.get(1);
    assertThat(xmlPublication.xml()).isEqualTo("xml");
    assertThat(xmlPublication.getDate()).isCloseTo(creationDate, within(1, ChronoUnit.MILLIS));
    assertThat(xmlPublication.getType()).isEqualTo(EventType.HANDOVER);
  }
}
