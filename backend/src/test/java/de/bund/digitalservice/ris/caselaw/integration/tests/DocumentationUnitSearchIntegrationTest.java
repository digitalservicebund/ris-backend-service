package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Slice;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentUnitStatusService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentNumberGeneratorService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentationUnitSearchIntegrationTest {
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

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseStatusRepository statusRepository;

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;
  @MockBean DocxConverterService docxConverterService;
  @MockBean UserService userService;
  @MockBean ClientRegistrationRepository clientRegistrationRepository;
  @MockBean AttachmentService attachmentService;

  private DocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");

    doReturn(DocumentationOfficeTransformer.transformToDomain(docOfficeDTO))
        .when(userService)
        .getDocumentationOffice(any(OidcUser.class));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testForCorrectResponseWhenRequestingAll() {
    DocumentationUnitDTO migrationDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .id(UUID.randomUUID())
                .documentNumber("MIGR202200012")
                .documentationOffice(docOfficeDTO)
                .build());
    // TODO can't the file number be set in the first save()?
    migrationDto =
        repository.save(
            migrationDto.toBuilder()
                .fileNumbers(
                    List.of(
                        FileNumberDTO.builder()
                            .value("AkteM")
                            .documentationUnit(migrationDto)
                            .rank(0L)
                            .build()))
                .build());
    DocumentationUnitDTO newNeurisDto =
        repository.save(
            DocumentationUnitDTO.builder()
                .documentNumber("NEUR202300008")
                .documentationOffice(docOfficeDTO)
                .build());
    newNeurisDto =
        repository.save(
            newNeurisDto.toBuilder()
                .fileNumbers(
                    List.of(
                        FileNumberDTO.builder()
                            .value("AkteY")
                            .documentationUnit(newNeurisDto)
                            .rank(0L)
                            .build()))
                .build());

    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(migrationDto)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .build());
    statusRepository.save(
        StatusDTO.builder()
            .documentationUnitDTO(newNeurisDto)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .build());

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=3")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(responseBody)
        .extracting("documentNumber", "fileNumber", "status")
        .containsExactly(
            tuple(
                "MIGR202200012",
                "AkteM",
                Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build()),
            tuple(
                "NEUR202300008",
                "AkteY",
                Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build()));
    assertThat(responseBody.getNumberOfElements()).isEqualTo(2);
  }

  @Test
  void testOrderedByDateDescending() {
    List<LocalDate> dates =
        Arrays.asList(
            LocalDate.of(2022, 1, 23),
            LocalDate.of(2022, 1, 23),
            null,
            LocalDate.of(2023, 3, 15),
            LocalDate.of(2023, 6, 7));

    for (LocalDate date : dates) {
      repository.save(
          DocumentationUnitDTO.builder()
              .documentNumber(RandomStringUtils.randomAlphabetic(13))
              .decisionDate(date)
              .documentationOffice(docOfficeDTO)
              .build());
    }

    Slice<DocumentationUnitListItem> responseBody =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(responseBody)
        .extracting("decisionDate")
        .containsExactly(
            LocalDate.of(2023, 06, 07),
            LocalDate.of(2023, 03, 15),
            LocalDate.of(2022, 01, 23),
            LocalDate.of(2022, 01, 23),
            null);
  }

  @Test
  void testForCorrectPagination() {
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("1234567801")
            .documentationOffice(docOfficeDTO)
            .build());
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("1234567802")
            .documentationOffice(docOfficeDTO)
            .build());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<?>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().isLast()).isFalse();
            });
  }

  @Test
  void testForCompleteResultListWhenSearchingForFileNumberOrDocumentNumber() {
    for (int i = 0; i < 10; i++) {
      DocumentationUnitDTO doc =
          repository.save(
              DocumentationUnitDTO.builder()
                  // index 0-4 get a "AB" docNumber
                  .documentNumber((i <= 4 ? "AB" : "GE") + "123456780" + i)
                  .documentationOffice(docOfficeDTO)
                  .build());

      repository.save(
          doc.toBuilder()
              .fileNumbers(
                  // even indices get a fileNumber
                  i % 2 == 1
                      ? List.of()
                      : List.of(
                          FileNumberDTO.builder()
                              .value("AB 34/" + i)
                              .documentationUnit(doc)
                              .rank(0L)
                              .build()))
              // index 4+ get a deviating fileNumber
              .deviatingFileNumbers(
                  i < 4
                      ? List.of()
                      : List.of(
                          DeviatingFileNumberDTO.builder()
                              .value("ABC 34/" + i)
                              .documentationUnit(doc)
                              .rank(0L)
                              .build()))
              .build());
    }

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=4&documentNumberOrFileNumber=" + "AB")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              Slice<DocumentationUnitListItem> responseBodyFirstPage = response.getResponseBody();
              assertThat(responseBodyFirstPage.getNumberOfElements()).isEqualTo(4);
              // not to be the last page
              assertThat(responseBodyFirstPage.isFirst()).isTrue();
              assertThat(responseBodyFirstPage.isLast()).isFalse();
              // to have docNumbers "AB1234567800", "AB1234567801", "AB1234567802", "AB1234567803"
              assertThat(responseBodyFirstPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567800", "AB1234567801", "AB1234567802", "AB1234567803");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=1&sz=4&documentNumberOrFileNumber=" + "AB")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              // expect second page...
              Slice<DocumentationUnitListItem> responseBodySecondPage = response.getResponseBody();
              // to have 4 results
              assertThat(responseBodySecondPage.getNumberOfElements()).isEqualTo(4);
              // not to be the last or first page
              assertThat(responseBodySecondPage.isFirst()).isFalse();
              assertThat(responseBodySecondPage.isLast()).isFalse();
              // to have docNumbers "AB1234567804", "GE1234567805", "GE1234567806", "GE1234567807"
              assertThat(responseBodySecondPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567804", "GE1234567805", "GE1234567806", "GE1234567807");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=2&sz=4&documentNumberOrFileNumber=" + "AB")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              // expect third page...
              Slice<DocumentationUnitListItem> responseBodyThirdPage = response.getResponseBody();
              // to have 2 results
              assertThat(responseBodyThirdPage.getNumberOfElements()).isEqualTo(2);
              // not to be the first but to be the last page
              assertThat(responseBodyThirdPage.isFirst()).isFalse();
              assertThat(responseBodyThirdPage.isLast()).isTrue();
              // to have docNumbers "GE1234567808", "GE1234567809"
              assertThat(responseBodyThirdPage.getContent())
                  .extracting("documentNumber")
                  .containsExactly("GE1234567808", "GE1234567809");
            });
  }

  @Test
  void testTrim() {
    repository.save(
        DocumentationUnitDTO.builder()
            .documentNumber("AB1234567802")
            .documentationOffice(docOfficeDTO)
            .build());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10&documentNumberOrFileNumber=+++AB++")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().getContent())
                  .extracting("documentNumber")
                  .containsExactly("AB1234567802");
            });
  }
}
