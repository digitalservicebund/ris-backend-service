package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class DocumentationUnitHistoryLogIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationUnitHistoryLogRepository databaseHistoryLogRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DocumentationUnitService documentationUnitService;
  @MockitoSpyBean private UserService userService;
  private final DocumentationOffice docOfficeDS = buildDSDocOffice();
  private final DocumentationOffice docOfficeBGH = buildBGHDocOffice();
  // Define user IDs for mocking
  private final UUID userIdDS = UUID.randomUUID();
  private final UUID userIdBGH = UUID.randomUUID();
  private DocumentationOfficeDTO documentationOffice;
  private static final String HISTORY_LOG_ENDPOINT = "/api/v1/caselaw/documentunits/";

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOfficeDS.abbreviation());
    // Mock the userService to return fake User objects
    when(userService.getUser(userIdDS))
        .thenReturn(
            Optional.of(
                User.builder()
                    .id(userIdDS)
                    .name("testUserDS")
                    .documentationOffice(docOfficeDS)
                    .build()));
    when(userService.getUser(userIdBGH))
        .thenReturn(
            Optional.of(
                User.builder()
                    .id(userIdBGH)
                    .name("testUserBGH")
                    .documentationOffice(docOfficeBGH)
                    .build()));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void getHistoryLogs_fromExistingDocumentationUnit() {
    UUID entityId =
        EntityBuilderTestUtil.createAndSaveDecision(
                repository,
                DecisionDTO.builder()
                    .documentationOffice(documentationOffice)
                    .documentNumber("docnr12345678"))
            .getId();

    assertThat(repository.findById(entityId)).isPresent();

    HistoryLogDTO dto1 = saveHistoryLog(entityId, userIdDS, null);
    HistoryLogDTO dto2 = saveHistoryLog(entityId, userIdBGH, null);
    HistoryLogDTO dto3 = saveHistoryLog(entityId, null, "migration");

    assertThat(databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(entityId))
        .hasSize(3);

    List<HistoryLog> historyLogs =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(HISTORY_LOG_ENDPOINT + entityId + "/historylogs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<HistoryLog>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(historyLogs).isNotEmpty();
    HistoryLog log1 = historyLogs.getFirst();
    HistoryLog log2 = historyLogs.get(1);
    HistoryLog log3 = historyLogs.get(2);

    // Order is now descending, by created at
    // log 1 is dto3
    assertThat(log1.createdAt().truncatedTo(ChronoUnit.MILLIS))
        .isEqualTo(dto3.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    // no user, no docoffice, only system name
    assertThat(log1.createdBy()).isEqualTo(dto3.getSystemName());
    assertThat(log1.documentationOffice()).isNull();
    assertThat(log1.description()).isEqualTo(dto3.getDescription());
    assertThat(log1.eventType()).isEqualTo(dto3.getEventType());

    // log 2 is dto2
    assertThat(log2.createdAt().truncatedTo(ChronoUnit.MILLIS))
        .isEqualTo(dto2.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    // usernames from other doc offices are not visible in logs
    assertThat(log2.createdBy()).isNull();
    // doc office derived from user API via userId
    assertThat(log2.documentationOffice()).isEqualTo("BGH");
    assertThat(log1.description()).isEqualTo(dto2.getDescription());
    assertThat(log2.eventType()).isEqualTo(dto2.getEventType());

    // log 3 is dto1
    assertThat(log3.createdAt().truncatedTo(ChronoUnit.MILLIS))
        .isEqualTo(dto1.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    // user from same doc office is allowed to see username
    assertThat(log3.createdBy()).isEqualTo("testUserDS");
    // doc office derived from user API via userId
    assertThat(log3.documentationOffice()).isEqualTo("DS");
    assertThat(log3.description()).isEqualTo(dto1.getDescription());
    assertThat(log3.eventType()).isEqualTo(dto1.getEventType());
  }

  @Test
  void saveHistoryLogs_multipleUpdateEventsAreMerged() throws DocumentationUnitNotExistsException {
    var docUnit =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber("docnr12345678"));

    docUnit.setScheduledPublicationDateTime(LocalDateTime.now());
    var user =
        User.builder()
            .id(UUID.randomUUID())
            .name("Al Nam")
            .documentationOffice(docOfficeDS)
            .build();
    documentationUnitService.updateDocumentationUnit(
        DecisionTransformer.transformToDomain((DecisionDTO) docUnit),
        DocumentationUnitService.DuplicateCheckStatus.DISABLED,
        user);

    var logsAfterFirstUpdate =
        databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(docUnit.getId());
    assertThat(logsAfterFirstUpdate).hasSize(2);

    docUnit.setScheduledPublicationDateTime(LocalDateTime.now());
    documentationUnitService.updateDocumentationUnit(
        DecisionTransformer.transformToDomain((DecisionDTO) docUnit),
        DocumentationUnitService.DuplicateCheckStatus.DISABLED,
        user);

    var logsAfterSecondUpdate =
        databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(docUnit.getId());
    assertThat(logsAfterSecondUpdate).hasSize(2);

    var firstLog = logsAfterFirstUpdate.getFirst();
    var secondLog = logsAfterSecondUpdate.getFirst();

    // Only date is different
    assertThat(firstLog.getCreatedAt()).isBefore(secondLog.getCreatedAt());

    // All other attributes are the same
    assertThat(firstLog.getEventType()).isEqualTo(secondLog.getEventType());
    assertThat(firstLog.getDocumentationUnitId()).isEqualTo(secondLog.getDocumentationUnitId());
    assertThat(firstLog.getUserId()).isEqualTo(secondLog.getUserId());
    assertThat(firstLog.getDescription()).isEqualTo(secondLog.getDescription());
    assertThat(firstLog.getSystemName()).isEqualTo(secondLog.getSystemName());
    assertThat(firstLog.getUserId()).isEqualTo(secondLog.getUserId());
    assertThat(firstLog.getId()).isEqualTo(secondLog.getId());
  }

  private HistoryLogDTO saveHistoryLog(UUID docUnitId, UUID userId, String systemName) {
    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitId(docUnitId)
            .eventType(HistoryLogEventType.UPDATE)
            .description("something updated")
            .systemName(systemName)
            .userId(userId)
            .build();

    return databaseHistoryLogRepository.save(dto);
  }

  @Test
  void getHistoryLogs_returnsEmptyList_whenNoLogsExist() {
    UUID entityId =
        EntityBuilderTestUtil.createAndSaveDecision(
                repository,
                DecisionDTO.builder()
                    .documentationOffice(documentationOffice)
                    .documentNumber("docnr_empty"))
            .getId();

    assertThat(repository.findById(entityId)).isPresent();

    List<HistoryLog> historyLogs =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(HISTORY_LOG_ENDPOINT + entityId + "/historylogs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<HistoryLog>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(historyLogs).isEmpty();
  }

  @Test
  void getHistoryLogs_returnsNotFound_whenDocUnitDoesNotExist() {
    UUID nonExistentId = UUID.randomUUID();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(HISTORY_LOG_ENDPOINT + nonExistentId + "/historylogs")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
