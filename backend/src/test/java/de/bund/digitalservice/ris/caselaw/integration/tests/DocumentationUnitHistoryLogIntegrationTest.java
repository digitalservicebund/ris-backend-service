package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

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
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DocumentationUnitHistoryLogIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationUnitHistoryLogRepository databaseHistoryLogRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DocumentationUnitService documentationUnitService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;
  private DocumentationOfficeDTO otherDocumentationOffice;
  private static final String HISTORY_LOG_ENDPOINT = "/api/v1/caselaw/documentunits/";

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    otherDocumentationOffice = documentationOfficeRepository.findByAbbreviation("BGH");
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void getHistoryLogs_fromExistingDocumentationUnit() {
    UUID entityId =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
                repository,
                DecisionDTO.builder()
                    .documentationOffice(documentationOffice)
                    .documentNumber("docnr12345678"))
            .getId();

    assertThat(repository.findById(entityId)).isPresent();

    HistoryLogDTO historyLog1 = saveHistoryLog(entityId, documentationOffice, "testUser1", null);

    saveHistoryLog(entityId, otherDocumentationOffice, "testUser2", null);
    HistoryLogDTO historyLog3 =
        saveHistoryLog(entityId, otherDocumentationOffice, null, "migration");

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
    // log 1 is historyLog3
    // log 2 is historyLog2
    // log 3 is historyLog1
    assertThat(log1.createdAt().truncatedTo(ChronoUnit.MILLIS))
        .isEqualTo(historyLog3.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    assertThat(log1.documentationOffice())
        .isEqualTo(historyLog3.getDocumentationOffice().getAbbreviation());
    assertThat(log1.eventType()).isEqualTo(historyLog3.getEventType());
    assertThat(log1.description()).isEqualTo(historyLog3.getDescription());
    // user from other doc office is allowed to see system name
    assertThat(log1.createdBy()).isEqualTo(historyLog3.getSystemName());
    // usernames from other doc offices are not visible in logs
    assertThat(log2.createdBy()).isNull();
    // user from same doc office is allowed to see username
    assertThat(log3.createdBy()).isEqualTo(historyLog1.getUserName());
  }

  @Test
  void saveHistoryLogs_multipleUpdateEventsAreMerged() throws DocumentationUnitNotExistsException {
    var docUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository,
            DecisionDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber("docnr12345678"));

    docUnit.setScheduledPublicationDateTime(LocalDateTime.now());
    var user =
        User.builder().id(UUID.randomUUID()).name("Al Nam").documentationOffice(docOffice).build();
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
    assertThat(firstLog.getDocumentationOffice()).isEqualTo(secondLog.getDocumentationOffice());
    assertThat(firstLog.getDescription()).isEqualTo(secondLog.getDescription());
    assertThat(firstLog.getUserName()).isEqualTo(secondLog.getUserName());
    assertThat(firstLog.getSystemName()).isEqualTo(secondLog.getSystemName());
    assertThat(firstLog.getUserId()).isEqualTo(secondLog.getUserId());
    assertThat(firstLog.getId()).isEqualTo(secondLog.getId());
  }

  private HistoryLogDTO saveHistoryLog(
      UUID docUnitId, DocumentationOfficeDTO office, String userName, String systemName) {
    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitId(docUnitId)
            .documentationOffice(office)
            .eventType(HistoryLogEventType.UPDATE)
            .description("something updated")
            .systemName(systemName)
            .userName(userName)
            .userId(UUID.randomUUID())
            .build();

    return databaseHistoryLogRepository.save(dto);
  }

  @Test
  void getHistoryLogs_returnsEmptyList_whenNoLogsExist() {
    UUID entityId =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
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
