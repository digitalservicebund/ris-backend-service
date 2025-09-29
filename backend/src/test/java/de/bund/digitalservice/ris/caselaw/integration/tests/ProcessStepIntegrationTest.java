package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

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
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Transactional
class ProcessStepIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcessStepRepository processStepRepository;

  @Autowired
  private DatabaseDocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;

  private DocumentationOfficeDTO documentationOfficeBGH;
  private DocumentationUnitDTO testDocumentationUnitBGH;
  private ProcessStepDTO ersterfassungProcessStep;
  private ProcessStepDTO qsformalProcessStep;
  private ProcessStepDTO blockiertProcessStep;
  private static final String PROCESS_STEP_ENDPOINT = "/api/v1/caselaw/";

  @BeforeEach
  void setUp() {
    documentationOfficeBGH =
        documentationOfficeRepository.findByAbbreviation(buildBGHDocOffice().abbreviation());
    testDocumentationUnitBGH =
        EntityBuilderTestUtil.createAndSaveDecision(
            documentationUnitRepository, documentationOfficeBGH, "TESTDOC002");
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
    blockiertProcessStep =
        processStepRepository
            .findByName("Blockiert")
            .orElseThrow(
                () -> new AssertionError("Process step 'Blockiert' not found in repository."));
  }

  @AfterEach
  void cleanUp() {
    documentationUnitProcessStepRepository.deleteAll();
    documentationUnitRepository.deleteAll();
  }

  @Test
  @DisplayName("GET /next - Should retrieve the next logical process step")
  void getNextProcessStep_shouldReturnNextStep() {
    LocalDateTime now = LocalDateTime.now();
    // Arrange: Set current step to "Neu"
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitBGH)
            .processStep(ersterfassungProcessStep)
            .createdAt(now)
            .build());

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(
            PROCESS_STEP_ENDPOINT
                + "documentationUnits/"
                + testDocumentationUnitBGH.getId()
                + "/processsteps/next")
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
  @DisplayName("GET /next - Should return 204 No Content if no next step is found")
  void getNextProcessStep_shouldReturnNoContent_ifNoNextStep() {

    LocalDateTime now = LocalDateTime.now();
    // Arrange: Set current step to the last possible step for the docoffice
    documentationUnitProcessStepRepository.save(
        DocumentationUnitProcessStepDTO.builder()
            .documentationUnit(testDocumentationUnitBGH)
            .processStep(blockiertProcessStep)
            .createdAt(now)
            .build());
    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(
            PROCESS_STEP_ENDPOINT
                + "documentationUnits/"
                + testDocumentationUnitBGH.getId()
                + "/processsteps/next")
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("GET /next - Should return 204 No Content if docunit has no current process step")
  void getNextProcessStep_shouldReturnNoContent_ifNoCurrentStep() {

    // Act & Assert
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(
            PROCESS_STEP_ENDPOINT
                + "documentationUnits/"
                + testDocumentationUnitBGH.getId()
                + "/processsteps/next")
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("GET /next - Should return 404 Not Found if docunit not found")
  void getNextProcessStep_withInvalidDocumentationUnitId_shouldReturnNotFound() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(
            PROCESS_STEP_ENDPOINT + "documentationUnits/" + UUID.randomUUID() + "/processteps/next")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  @DisplayName("GET /all - Should retrieve all possible process steps for a documentation office")
  void getAllPossibleProcessStepsForDocOffice_shouldReturnAllSteps() {

    // Act & Assert
    risWebTestClient
        .withLogin("/BGH")
        .get()
        .uri(PROCESS_STEP_ENDPOINT + "processsteps")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<ProcessStep>>() {})
        .consumeWith(
            response -> {
              List<ProcessStep> possibleSteps = response.getResponseBody();
              assertThat(possibleSteps).isNotNull().hasSize(8);
              assertThat(possibleSteps.get(0).name()).isEqualTo("Neu");
              assertThat(possibleSteps.get(1).name()).isEqualTo("Ersterfassung");
              assertThat(possibleSteps.get(2).name()).isEqualTo("QS formal");
              assertThat(possibleSteps.get(3).name()).isEqualTo("Fachdokumentation");
              assertThat(possibleSteps.get(4).name()).isEqualTo("QS fachlich");
              assertThat(possibleSteps.get(5).name()).isEqualTo("Fertig");
              assertThat(possibleSteps.get(6).name()).isEqualTo("Wiedervorlage");
              assertThat(possibleSteps.get(7).name()).isEqualTo("Blockiert");
            });
  }

  @Test
  @DisplayName("GET /all - Should retrieve all assignable process steps for a documentation office")
  void getAssignableProcessStepsForDocOffice_shouldReturnAllStepsButNeu() {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("assignableOnly", "true");
    URI uri =
        new DefaultUriBuilderFactory()
            .builder()
            .path(PROCESS_STEP_ENDPOINT + "processsteps")
            .queryParams(queryParams)
            .build();

    // Act & Assert
    risWebTestClient
        .withLogin("/BGH")
        .get()
        .uri(uri)
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

  @Test
  @DisplayName("GET /all - Should return empty list if docoffice has no associated process steps")
  void
      getAllPossibleProcessStepsForDocOffice_shouldReturnEmptyList_ifNoAssociatedProcessStepsFound() {

    // Act & Assert
    risWebTestClient
        .withLogin("/BZSt")
        .get()
        .uri(PROCESS_STEP_ENDPOINT + "processsteps")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<ProcessStep>>() {})
        .consumeWith(
            response -> {
              List<ProcessStep> possibleSteps = response.getResponseBody();
              assertThat(possibleSteps).isNotNull().isEmpty();
            });
  }

  @Test
  @DisplayName("GET /all - Should return 404 Not Found if docoffice not found")
  void getAllPossibleProcessStepsForDocOffice_withInvalidDocOfficeId_shouldReturnNotFound() {
    risWebTestClient
        .withLogin("/random")
        .get()
        .uri(PROCESS_STEP_ENDPOINT + "processsteps")
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
