package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class DocumentationUnitProcessStepIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcessStepRepository processStepRepository;

  @Autowired
  private DatabaseDocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;

  private DocumentationOfficeDTO documentationOfficeDS;
  private DocumentationOfficeDTO documentationOfficeBGH;
  private DocumentationUnitDTO testDocumentationUnitDS;
  private DocumentationUnitDTO testDocumentationUnitBGH;
  private ProcessStepDTO neuProcessStep;
  private ProcessStepDTO ersterfassungProcessStep;
  private ProcessStepDTO qsformalProcessStep;
  private static final String PROCESS_STEP_ENDPOINT = "/api/v1/caselaw/processsteps/";

  @BeforeEach
  void setUp() {
    documentationOfficeDS =
        documentationOfficeRepository.findByAbbreviation(buildDSDocOffice().abbreviation());
    documentationOfficeBGH =
        documentationOfficeRepository.findByAbbreviation(buildBGHDocOffice().abbreviation());
    testDocumentationUnitDS =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository, documentationOfficeDS, "TESTDOC001");
    testDocumentationUnitBGH =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository, documentationOfficeBGH, "TESTDOC002");
    neuProcessStep =
        processStepRepository
            .findByName("Neu")
            .orElseThrow(() -> new AssertionError("Process step 'Neu' not found in repository."));
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
  }

  @AfterEach
  void cleanUp() {
    documentationUnitProcessStepRepository.deleteAll();
    documentationUnitRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /new - Should add a new process step to a documentation unit")
  void saveProcessStep_shouldAddStepSuccessfully() {
    LocalDateTime now = LocalDateTime.now();

    // Act
    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri(PROCESS_STEP_ENDPOINT + testDocumentationUnitDS.getId() + "/new")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(neuProcessStep.getId().toString())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(DocumentationUnitProcessStep.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().getProcessStep().uuid())
                  .isEqualTo(neuProcessStep.getId());
              assertThat(response.getResponseBody().getCreatedAt())
                  .isCloseTo(now, byLessThan(60, ChronoUnit.SECONDS));
            });

    // Assert that it's in the database
    List<DocumentationUnitProcessStepDTO> stepsInDb =
        documentationUnitProcessStepRepository.findByDocumentationUnitIdOrderByCreatedAtDesc(
            testDocumentationUnitDS.getId());
    assertThat(stepsInDb).hasSize(1);
    assertThat(stepsInDb.getFirst().getProcessStepId()).isEqualTo(neuProcessStep.getId());
  }

  @Test
  @DisplayName("GET /current - Should retrieve the current process step of a documentation unit")
  void getCurrentProcessStep_shouldReturnCurrentStep() {
    LocalDateTime now = LocalDateTime.now();
    // Arrange: Add a current step for the test documentation unit
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(neuProcessStep.getId())
            .createdAt(now.minusSeconds(10))
            .build());
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(ersterfassungProcessStep.getId())
            .createdAt(now)
            .build());

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(PROCESS_STEP_ENDPOINT + testDocumentationUnitDS.getId() + "/current")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnitProcessStep.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().getProcessStep().uuid())
                  .isEqualTo(ersterfassungProcessStep.getId());
              assertThat(response.getResponseBody().getProcessStep().name())
                  .isEqualTo("Ersterfassung");
            });
  }

  @Test
  @DisplayName("GET /next - Should retrieve the next logical process step")
  void getNextProcessStep_shouldReturnNextStep() {
    LocalDateTime now = LocalDateTime.now();
    // Arrange: Set current step to "Neu"
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitBGH.getId())
            .processStepId(ersterfassungProcessStep.getId())
            .createdAt(now.minusSeconds(10))
            .build());

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(PROCESS_STEP_ENDPOINT + testDocumentationUnitBGH.getId() + "/next")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(ProcessStep.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().uuid()).isEqualTo(qsformalProcessStep.getId());
              assertThat(response.getResponseBody().name()).isEqualTo("QS formal");
            });
  }

  @Test
  @DisplayName("GET /last - Should retrieve the last (previous) process step")
  void getLastProcessStep_shouldReturnLastStep() {
    LocalDateTime now = LocalDateTime.now();

    // Arrange
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(neuProcessStep.getId())
            .createdAt(now.minusSeconds(10))
            .build());
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(ersterfassungProcessStep.getId())
            .createdAt(now)
            .build());

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(PROCESS_STEP_ENDPOINT + testDocumentationUnitDS.getId() + "/last")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnitProcessStep.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().getProcessStep().uuid())
                  .isEqualTo(neuProcessStep.getId());
              assertThat(response.getResponseBody().getProcessStep().name()).isEqualTo("Neu");
            });
  }

  @Test
  @DisplayName("GET /history - Should retrieve the complete history of process steps")
  void getProcessStepHistory_shouldReturnFullHistory() {
    LocalDateTime now = LocalDateTime.now();
    // Arrange
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(neuProcessStep.getId())
            .createdAt(now.minusSeconds(20))
            .build());
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(ersterfassungProcessStep.getId())
            .createdAt(now.minusSeconds(10))
            .build());
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnitId(testDocumentationUnitDS.getId())
            .processStepId(qsformalProcessStep.getId())
            .createdAt(now)
            .build());

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(PROCESS_STEP_ENDPOINT + testDocumentationUnitDS.getId() + "/history")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<DocumentationUnitProcessStep>>() {})
        .consumeWith(
            response -> {
              List<DocumentationUnitProcessStep> history = response.getResponseBody();
              assertThat(history).isNotNull().hasSize(3);
              assertThat(history.get(0).getProcessStep().name()).isEqualTo("QS formal");
              assertThat(history.get(1).getProcessStep().name()).isEqualTo("Ersterfassung");
              assertThat(history.get(2).getProcessStep().name()).isEqualTo("Neu");
            });
  }

  @Test
  @DisplayName("GET /all - Should retrieve all possible process steps for a documentation office")
  void getAllPossibleProcessStepsForDocOffice_shouldReturnAllSteps() {

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(PROCESS_STEP_ENDPOINT + documentationOfficeBGH.getId() + "/all")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<ProcessStep>>() {})
        .consumeWith(
            response -> {
              List<ProcessStep> possibleSteps = response.getResponseBody();
              assertThat(possibleSteps).isNotNull().hasSize(7);
              assertThat(possibleSteps.get(0).name()).isEqualTo("Ersterfassung");
              assertThat(possibleSteps.get(1).name()).isEqualTo("QS formal");
              assertThat(possibleSteps.get(2).name()).isEqualTo("Fachdokumentation");
              assertThat(possibleSteps.get(3).name()).isEqualTo("QS fachlich");
              assertThat(possibleSteps.get(4).name()).isEqualTo("Fertig");
              assertThat(possibleSteps.get(5).name()).isEqualTo("Wiedervorlage");
              assertThat(possibleSteps.get(6).name()).isEqualTo("Blockiert");
            });
  }
}
