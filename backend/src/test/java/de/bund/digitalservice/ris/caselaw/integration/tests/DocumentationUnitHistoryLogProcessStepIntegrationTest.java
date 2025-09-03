package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseHistoryLogDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class DocumentationUnitHistoryLogProcessStepIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationUnitHistoryLogRepository databaseHistoryLogRepository;

  @Autowired
  private DatabaseHistoryLogDocumentationUnitProcessStepRepository
      databaseHistoryLogDocumentationUnitProcessStepRepository;

  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DocumentationUnitService documentationUnitService;
  @Autowired private DatabaseProcessStepRepository processStepRepository;

  @Autowired
  private DatabaseDocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;

  @MockitoSpyBean private UserService userService;
  private final DocumentationOffice docOfficeDS = buildDSDocOffice();
  private final DocumentationOffice docOfficeBGH = buildBGHDocOffice();
  // Define user IDs and user for mocking
  private final UUID creatorUserIdDS = UUID.randomUUID();
  private final User creatorUserDS =
      User.builder()
          .id(creatorUserIdDS)
          .name("testUserDS")
          .documentationOffice(docOfficeDS)
          .build();

  private final UUID creatorUserIdBGH = UUID.randomUUID();
  private final User creatorUserBGH =
      User.builder()
          .id(creatorUserIdBGH)
          .name("testUserBGH")
          .documentationOffice(docOfficeBGH)
          .build();

  private final UUID userNullId = UUID.randomUUID();

  private final UUID user1IdDS = UUID.randomUUID();
  private final User user1DS =
      User.builder().id(user1IdDS).name("user1").documentationOffice(docOfficeDS).build();

  private final UUID user2IdDS = UUID.randomUUID();
  private final User user2DS =
      User.builder().id(user2IdDS).name("user2").documentationOffice(docOfficeDS).build();

  private ProcessStepDTO ersterfassungProcessStep;
  private ProcessStepDTO qsformalProcessStep;
  private DocumentationUnitDTO testDocumentationUnitDS;
  private DocumentationUnitDTO testDocumentationUnitBGH;

  private DocumentationOfficeDTO documentationOfficeDS;
  private DocumentationOfficeDTO documentationOfficeBGH;
  private static final String HISTORY_LOG_ENDPOINT = "/api/v1/caselaw/documentunits/";

  @BeforeEach
  void setUp() {
    documentationOfficeDS =
        documentationOfficeRepository.findByAbbreviation(docOfficeDS.abbreviation());
    documentationOfficeBGH =
        documentationOfficeRepository.findByAbbreviation(docOfficeBGH.abbreviation());
    // Mock the userService to return fake User objects
    when(userService.getUser(creatorUserIdDS)).thenReturn(creatorUserDS);
    when(userService.getUser(creatorUserIdBGH)).thenReturn(creatorUserBGH);
    when(userService.getUser(user1IdDS)).thenReturn(user1DS);
    when(userService.getUser(user2IdDS)).thenReturn(user2DS);
    when(userService.getUser(user2IdDS)).thenReturn(user2DS);
    when(userService.getUser(userNullId)).thenReturn(null);

    ersterfassungProcessStep =
        processStepRepository
            .findByName("Ersterfassung")
            .orElseThrow(
                () -> new AssertionError("Process step 'Ersterfassung' not found in repository."));
    qsformalProcessStep =
        processStepRepository
            .findByName("QS formal")
            .orElseThrow(
                () -> new AssertionError("Process step 'QS formal' not found in repository."));

    testDocumentationUnitDS =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, documentationOfficeDS, "TESTDOC001");
    testDocumentationUnitBGH =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, documentationOfficeBGH, "TESTDOC002");
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    databaseHistoryLogRepository.deleteAll();
    databaseHistoryLogDocumentationUnitProcessStepRepository.deleteAll();
  }

  @Test
  void getProcessStepHistoryLogs_withSameDocOffice_shouldReturnHistoryLogs_withUserData() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    DocumentationUnitProcessStepDTO step1DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(ersterfassungProcessStep)
            .user(UserDTO.builder().id(user1IdDS).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step2DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(UserDTO.builder().id(user2IdDS).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step3DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(null)
            .createdAt(now)
            .build();
    documentationUnitProcessStepRepository.save(step1DTO);
    documentationUnitProcessStepRepository.save(step2DTO);
    documentationUnitProcessStepRepository.save(step3DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP,
        "Schritt gesetzt: Ersterfassung",
        null,
        step1DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        null,
        step1DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP,
        "Schritt geändert: Ersterfassung → QS formal",
        step1DTO,
        step2DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step1DTO,
        step2DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step2DTO,
        step3DTO);

    assertThat(
            databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(
                testDocumentationUnitDS.getId()))
        .hasSize(5);

    // Act
    List<HistoryLog> historyLogs =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(HISTORY_LOG_ENDPOINT + testDocumentationUnitDS.getId() + "/historylogs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<HistoryLog>>() {})
            .returnResult()
            .getResponseBody();

    // Assert
    assertThat(historyLogs).isNotEmpty();
    HistoryLog log1 = historyLogs.getFirst();
    HistoryLog log2 = historyLogs.get(1);
    HistoryLog log3 = historyLogs.get(2);
    HistoryLog log4 = historyLogs.get(3);
    HistoryLog log5 = historyLogs.get(4);

    // Order is now descending, by created at
    assertThat(log1.createdBy()).isEqualTo("testUserDS");
    assertThat(log1.documentationOffice()).isEqualTo("DS");
    assertThat(log1.description()).isEqualTo("Person entfernt: user2");
    assertThat(log1.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log2.createdBy()).isEqualTo("testUserDS");
    assertThat(log2.documentationOffice()).isEqualTo("DS");
    assertThat(log2.description()).isEqualTo("Person geändert: user1 → user2");
    assertThat(log2.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log3.createdBy()).isEqualTo("testUserDS");
    assertThat(log3.documentationOffice()).isEqualTo("DS");
    assertThat(log3.description()).isEqualTo("Schritt geändert: Ersterfassung → QS formal");
    assertThat(log3.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP);

    assertThat(log4.createdBy()).isEqualTo("testUserDS");
    assertThat(log4.documentationOffice()).isEqualTo("DS");
    assertThat(log4.description()).isEqualTo("Person gesetzt: user1");
    assertThat(log4.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log5.createdBy()).isEqualTo("testUserDS");
    assertThat(log5.documentationOffice()).isEqualTo("DS");
    assertThat(log5.description()).isEqualTo("Schritt gesetzt: Ersterfassung");
    assertThat(log5.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP);
  }

  @Test
  void getProcessStepHistoryLogs_withOtherDocOffice_shouldReturnHistoryLogs_withoutUserData() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    DocumentationUnitProcessStepDTO step1DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(ersterfassungProcessStep)
            .user(UserDTO.builder().id(user1IdDS).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step2DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(UserDTO.builder().id(user2IdDS).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step3DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(null)
            .createdAt(now)
            .build();
    documentationUnitProcessStepRepository.save(step1DTO);
    documentationUnitProcessStepRepository.save(step2DTO);
    documentationUnitProcessStepRepository.save(step3DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitBGH.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP,
        "Schritt gesetzt: Ersterfassung",
        null,
        step1DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitBGH.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        null,
        step1DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitBGH.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP,
        "Schritt geändert: Ersterfassung → QS formal",
        step1DTO,
        step2DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitBGH.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step1DTO,
        step2DTO);

    saveProcessStepHistoryLog(
        testDocumentationUnitBGH.getId(),
        creatorUserDS,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step2DTO,
        step3DTO);

    assertThat(
            databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(
                testDocumentationUnitBGH.getId()))
        .hasSize(5);

    // Act
    List<HistoryLog> historyLogs =
        risWebTestClient
            .withLogin("/BGH")
            .get()
            .uri(HISTORY_LOG_ENDPOINT + testDocumentationUnitBGH.getId() + "/historylogs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<HistoryLog>>() {})
            .returnResult()
            .getResponseBody();

    // Assert
    assertThat(historyLogs).isNotEmpty();
    HistoryLog log1 = historyLogs.getFirst();
    HistoryLog log2 = historyLogs.get(1);
    HistoryLog log3 = historyLogs.get(2);
    HistoryLog log4 = historyLogs.get(3);
    HistoryLog log5 = historyLogs.get(4);

    // Order is now descending, by created at
    assertThat(log1.createdBy()).isNull();
    assertThat(log1.documentationOffice()).isEqualTo("DS");
    // No 'entfernt' in generic description
    assertThat(log1.description()).isEqualTo("Person geändert");
    assertThat(log1.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log2.createdBy()).isNull();
    assertThat(log2.documentationOffice()).isEqualTo("DS");
    assertThat(log2.description()).isEqualTo("Person geändert");
    assertThat(log2.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log3.createdBy()).isNull();
    assertThat(log3.documentationOffice()).isEqualTo("DS");
    assertThat(log3.description()).isEqualTo("Schritt geändert: Ersterfassung → QS formal");
    assertThat(log3.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP);

    assertThat(log4.createdBy()).isNull();
    assertThat(log4.documentationOffice()).isEqualTo("DS");
    assertThat(log4.description()).isEqualTo("Person gesetzt");
    assertThat(log4.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log5.createdBy()).isNull();
    assertThat(log5.documentationOffice()).isEqualTo("DS");
    assertThat(log5.description()).isEqualTo("Schritt gesetzt: Ersterfassung");
    assertThat(log5.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP);
  }

  @Test
  void
      getProcessStepHistoryLogs_withSameDocOffice_couldNotRetrieveUserData_shouldReturnHistoryLogs_withoutUserData() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    DocumentationUnitProcessStepDTO step1DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(ersterfassungProcessStep)
            .user(UserDTO.builder().id(userNullId).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step2DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(UserDTO.builder().id(userNullId).build())
            .createdAt(now)
            .build();
    DocumentationUnitProcessStepDTO step3DTO =
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitDS)
            .processStep(qsformalProcessStep)
            .user(null)
            .createdAt(now)
            .build();

    documentationUnitProcessStepRepository.save(step1DTO);
    documentationUnitProcessStepRepository.save(step2DTO);
    documentationUnitProcessStepRepository.save(step3DTO);

    // only to-user set -> person set
    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        null,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        null,
        step2DTO);

    // from and to-user set -> person changed
    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        null,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step1DTO,
        step2DTO);

    // only from-user set -> person removed
    saveProcessStepHistoryLog(
        testDocumentationUnitDS.getId(),
        null,
        HistoryLogEventType.PROCESS_STEP_USER,
        null,
        step2DTO,
        step3DTO);

    assertThat(
            databaseHistoryLogRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(
                testDocumentationUnitDS.getId()))
        .hasSize(3);

    // Act
    List<HistoryLog> historyLogs =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(HISTORY_LOG_ENDPOINT + testDocumentationUnitDS.getId() + "/historylogs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<HistoryLog>>() {})
            .returnResult()
            .getResponseBody();

    // Assert
    assertThat(historyLogs).isNotEmpty();
    HistoryLog log1 = historyLogs.getFirst();
    HistoryLog log2 = historyLogs.get(1);
    HistoryLog log3 = historyLogs.get(2);

    // Order is now descending, by created at
    assertThat(log1.createdBy()).isNull();
    assertThat(log1.documentationOffice()).isNull();
    assertThat(log1.description()).isEqualTo("Person geändert");
    assertThat(log1.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log2.createdBy()).isNull();
    assertThat(log2.documentationOffice()).isNull();
    assertThat(log2.description()).isEqualTo("Person geändert");
    assertThat(log2.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);

    assertThat(log3.createdBy()).isNull();
    assertThat(log3.documentationOffice()).isNull();
    assertThat(log3.description()).isEqualTo("Person geändert");
    assertThat(log3.eventType()).isEqualTo(HistoryLogEventType.PROCESS_STEP_USER);
  }

  public void saveProcessStepHistoryLog(
      UUID documentationUnitId,
      @Nullable User creatorUser,
      HistoryLogEventType eventType,
      String description,
      @Nullable DocumentationUnitProcessStepDTO fromStepDto,
      @Nullable DocumentationUnitProcessStepDTO toStepDto) {

    HistoryLogDTO historyLogDTO =
        HistoryLogDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitId(documentationUnitId)
            .userId(creatorUser != null ? creatorUser.id() : null)
            .description(description)
            .eventType(eventType)
            .build();
    databaseHistoryLogRepository.save(historyLogDTO);

    HistoryLogDocumentationUnitProcessStepDTO historyLogDocumentationUnitProcessStepDTO =
        HistoryLogDocumentationUnitProcessStepDTO.builder()
            .createdAt(Instant.now())
            .historyLog(historyLogDTO)
            .toDocumentationUnitProcessStep(toStepDto)
            .fromDocumentationUnitProcessStep(fromStepDto)
            .build();

    databaseHistoryLogDocumentationUnitProcessStepRepository.save(
        historyLogDocumentationUnitProcessStepDTO);
  }
}
