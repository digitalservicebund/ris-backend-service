package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentationUnitProcessStepServiceTest {

  @Mock private DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;
  @Mock private DocumentationUnitRepository documentationUnitRepository;
  @Mock private DocumentationOfficeService documentationOfficeService;

  @InjectMocks private DocumentationUnitProcessStepService service;

  private UUID docUnitId;
  private UUID docOfficeId;
  private UUID processStep1Id;
  private UUID processStep2Id;
  private UUID processStep3Id;
  private DocumentationUnit testDocumentationUnit;
  private ProcessStep processStepNeu;
  private ProcessStep processStepErsterfassung;
  private ProcessStep processStepQsFormal;
  private ProcessStep processStepFertig; // Assuming this is a "final" step for some tests

  private DocumentationUnit createMockDocumentationUnit(UUID docUnitId, UUID officeId) {
    return Decision.builder()
        .uuid(docUnitId)
        .coreData(
            CoreData.builder()
                .documentationOffice(DocumentationOffice.builder().id(officeId).build())
                .build())
        .build();
  }

  private DocumentationUnitProcessStep createMockDocumentationUnitProcessStep(
      UUID stepId, String stepName, LocalDateTime createdAt) {
    return DocumentationUnitProcessStep.builder()
        .id(UUID.randomUUID())
        .processStep(createMockProcessStep(stepId, stepName))
        .createdAt(createdAt)
        .build();
  }

  private ProcessStep createMockProcessStep(UUID id, String name) {
    return new ProcessStep(id, name, "abbreviation");
  }

  @BeforeEach
  void setUp() throws DocumentationUnitNotExistsException {
    docUnitId = UUID.randomUUID();
    docOfficeId = UUID.randomUUID();
    processStep1Id = UUID.randomUUID();
    processStep2Id = UUID.randomUUID();
    processStep3Id = UUID.randomUUID();
    UUID processStepFertigId = UUID.randomUUID();

    testDocumentationUnit = createMockDocumentationUnit(docUnitId, docOfficeId);

    processStepNeu = createMockProcessStep(processStep1Id, "Neu");
    processStepErsterfassung = createMockProcessStep(processStep2Id, "Ersterfassung");
    processStepQsFormal = createMockProcessStep(processStep3Id, "QS formal");
    processStepFertig = createMockProcessStep(processStepFertigId, "Fertig");

    // Standard stubbing for methods that are frequently called and expected to succeed
    // Use lenient to prevent UnnecessaryStubbingException if a mock isn't called in a specific test
    lenient()
        .when(documentationUnitRepository.findByUuid(docUnitId))
        .thenReturn(testDocumentationUnit);
  }

  // --- Tests for saveProcessStep ---

  @Test
  @DisplayName("saveProcessStep - Should save a new process step successfully")
  void saveProcessStep_shouldSaveSuccessfully()
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    // Arrange
    DocumentationUnitProcessStep savedStep =
        createMockDocumentationUnitProcessStep(
            processStepNeu.uuid(), processStepNeu.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.saveProcessStep(docUnitId, processStepNeu.uuid()))
        .thenReturn(savedStep);

    // Act
    DocumentationUnitProcessStep result = service.saveProcessStep(docUnitId, processStepNeu.uuid());

    // Assert
    assertThat(result).isEqualTo(savedStep);
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .saveProcessStep(docUnitId, processStepNeu.uuid());
  }

  @Test
  @DisplayName(
      "saveProcessStep - Should throw DocumentationUnitNotExistsException if doc unit not found")
  void saveProcessStep_shouldThrowDocumentationUnitNotExistsException_ifDocUnitNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentDocUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(nonExistentDocUnitId))
        .thenThrow(new DocumentationUnitNotExistsException("Doc unit not found for save"));

    // Act & Assert
    assertThatThrownBy(() -> service.saveProcessStep(nonExistentDocUnitId, processStepNeu.uuid()))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Doc unit not found for save");
    verify(documentationUnitRepository, times(1)).findByUuid(nonExistentDocUnitId);
    verify(documentationUnitProcessStepRepository, never()).saveProcessStep(any(), any());
  }

  @Test
  @DisplayName(
      "saveProcessStep - Should throw ProcessStepNotFoundException if process step not found")
  void saveProcessStep_shouldThrowProcessStepNotFoundException_ifProcessStepNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentProcessStepId = UUID.randomUUID();
    when(documentationUnitProcessStepRepository.saveProcessStep(
            docUnitId, nonExistentProcessStepId))
        .thenThrow(new ProcessStepNotFoundException("Process step not found for save"));

    // Act & Assert
    assertThatThrownBy(() -> service.saveProcessStep(docUnitId, nonExistentProcessStepId))
        .isInstanceOf(ProcessStepNotFoundException.class)
        .hasMessageContaining("Process step not found for save");
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .saveProcessStep(docUnitId, nonExistentProcessStepId);
  }

  // --- Tests for getCurrentProcessStep ---

  @Test
  @DisplayName("getCurrentProcessStep - Should return current step if found")
  void getCurrentProcessStep_shouldReturnCurrentStep() throws DocumentationUnitNotExistsException {
    // Arrange
    DocumentationUnitProcessStep currentStep =
        createMockDocumentationUnitProcessStep(
            processStepErsterfassung.uuid(), processStepErsterfassung.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.of(currentStep));

    // Act
    Optional<DocumentationUnitProcessStep> result = service.getCurrentProcessStep(docUnitId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getProcessStep().uuid()).isEqualTo(processStepErsterfassung.uuid());
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
  }

  @Test
  @DisplayName("getCurrentProcessStep - Should return empty Optional if no current step found")
  void getCurrentProcessStep_shouldReturnEmptyOptional_ifNoCurrentStep()
      throws DocumentationUnitNotExistsException {
    // Arrange
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.empty());

    // Act
    Optional<DocumentationUnitProcessStep> result = service.getCurrentProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
  }

  @Test
  @DisplayName(
      "getCurrentProcessStep - Should throw DocumentationUnitNotExistsException if doc unit not found")
  void getCurrentProcessStep_shouldThrowDocumentationUnitNotExistsException_ifDocUnitNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentDocUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(nonExistentDocUnitId))
        .thenThrow(new DocumentationUnitNotExistsException("Doc unit not found"));

    // Act & Assert
    assertThatThrownBy(() -> service.getCurrentProcessStep(nonExistentDocUnitId))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Doc unit not found");
    verify(documentationUnitRepository, times(1)).findByUuid(nonExistentDocUnitId);
    verify(documentationUnitProcessStepRepository, never()).getCurrentProcessStep(any());
  }

  // --- Tests for getNextProcessStep ---

  @Test
  @DisplayName("getNextProcessStep - Should return next step if available")
  void getNextProcessStep_shouldReturnNextStep()
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    // Arrange
    DocumentationUnitProcessStep currentStep =
        createMockDocumentationUnitProcessStep(
            processStepErsterfassung.uuid(), processStepErsterfassung.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.of(currentStep));

    List<ProcessStep> officeProcessSteps =
        Arrays.asList(
            processStepNeu, processStepErsterfassung, processStepQsFormal, processStepFertig);
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId))
        .thenReturn(officeProcessSteps);

    // Act
    Optional<ProcessStep> result = service.getNextProcessStep(docUnitId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().uuid()).isEqualTo(processStepQsFormal.uuid());
    assertThat(result.get().name()).isEqualTo(processStepQsFormal.name());
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, times(1)).getProcessStepsForDocumentationOffice(docOfficeId);
  }

  @Test
  @DisplayName("getNextProcessStep - Should return empty Optional if no current step")
  void getNextProcessStep_shouldReturnEmptyOptional_ifNoCurrentStep()
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    // Arrange
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.empty());

    // Act
    Optional<ProcessStep> result = service.getNextProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, never())
        .getProcessStepsForDocumentationOffice(
            any()); // Should not call office service if no current step
  }

  @Test
  @DisplayName("getNextProcessStep - Should return empty Optional if current step is the last")
  void getNextProcessStep_shouldReturnEmptyOptional_ifCurrentStepIsLast()
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    // Arrange
    DocumentationUnitProcessStep currentStep =
        createMockDocumentationUnitProcessStep(
            processStepFertig.uuid(), processStepFertig.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.of(currentStep));

    List<ProcessStep> officeProcessSteps =
        Arrays.asList(
            processStepNeu,
            processStepErsterfassung,
            processStepQsFormal,
            processStepFertig // This is the last step
            );
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId))
        .thenReturn(officeProcessSteps);

    // Act
    Optional<ProcessStep> result = service.getNextProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, times(1)).getProcessStepsForDocumentationOffice(docOfficeId);
  }

  @Test
  @DisplayName(
      "getNextProcessStep - Should return empty Optional if current step not in office flow")
  void getNextProcessStep_shouldReturnEmptyOptional_ifCurrentStepNotInOfficeFlow()
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    // Arrange
    UUID unknownProcessStepId = UUID.randomUUID();
    ProcessStep unknownProcessStep = createMockProcessStep(unknownProcessStepId, "Unknown Step");
    DocumentationUnitProcessStep currentStep =
        createMockDocumentationUnitProcessStep(
            unknownProcessStep.uuid(), unknownProcessStep.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.of(currentStep));

    List<ProcessStep> officeProcessSteps =
        Arrays.asList( // Does not contain unknownProcessStep
            processStepNeu, processStepErsterfassung);
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId))
        .thenReturn(officeProcessSteps);

    // Act
    Optional<ProcessStep> result = service.getNextProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, times(1)).getProcessStepsForDocumentationOffice(docOfficeId);
  }

  @Test
  @DisplayName(
      "getNextProcessStep - Should throw DocumentationUnitNotExistsException if doc unit not found")
  void getNextProcessStep_shouldThrowDocumentationUnitNotExistsException_ifDocUnitNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentDocUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(nonExistentDocUnitId))
        .thenThrow(new DocumentationUnitNotExistsException("Doc unit not found for next step"));

    // Act & Assert
    assertThatThrownBy(() -> service.getNextProcessStep(nonExistentDocUnitId))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Doc unit not found for next step");
    verify(documentationUnitRepository, times(1)).findByUuid(nonExistentDocUnitId);
    verify(documentationUnitProcessStepRepository, never()).getCurrentProcessStep(any());
    verify(documentationOfficeService, never()).getProcessStepsForDocumentationOffice(any());
  }

  @Test
  @DisplayName(
      "getNextProcessStep - Should throw DocumentationOfficeNotExistsException if office not found")
  void getNextProcessStep_shouldThrowDocumentationOfficeNotExistsException_ifOfficeNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    DocumentationUnitProcessStep currentStep =
        createMockDocumentationUnitProcessStep(
            processStepErsterfassung.uuid(), processStepErsterfassung.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.getCurrentProcessStep(docUnitId))
        .thenReturn(Optional.of(currentStep));

    UUID nonExistentOfficeId = UUID.randomUUID();
    // Re-stub the documentation unit to point to a non-existent office ID for this test
    when(documentationUnitRepository.findByUuid(docUnitId))
        .thenReturn(createMockDocumentationUnit(docUnitId, nonExistentOfficeId));

    when(documentationOfficeService.getProcessStepsForDocumentationOffice(nonExistentOfficeId))
        .thenThrow(
            new DocumentationOfficeNotExistsException(
                "Office not found for next step calculation"));

    // Act & Assert
    assertThatThrownBy(() -> service.getNextProcessStep(docUnitId))
        .isInstanceOf(DocumentationOfficeNotExistsException.class)
        .hasMessageContaining("Office not found for next step calculation");
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, times(1))
        .getProcessStepsForDocumentationOffice(nonExistentOfficeId);
  }

  // --- Tests for getLastProcessStep ---

  @Test
  @DisplayName("getLastProcessStep - Should return last step if multiple steps exist")
  void getLastProcessStep_shouldReturnLastStep()
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    DocumentationUnitProcessStep step1 =
        createMockDocumentationUnitProcessStep(
            processStepNeu.uuid(), processStepNeu.name(), now.minusMinutes(2));
    DocumentationUnitProcessStep step2 =
        createMockDocumentationUnitProcessStep(
            processStepErsterfassung.uuid(), processStepErsterfassung.name(), now.minusMinutes(1));
    DocumentationUnitProcessStep step3 =
        createMockDocumentationUnitProcessStep(
            processStepQsFormal.uuid(), processStepQsFormal.name(), now); // Current step

    when(documentationUnitProcessStepRepository.findAllByDocumentationUnitId(docUnitId))
        .thenReturn(Arrays.asList(step3, step2, step1));

    // Act
    Optional<DocumentationUnitProcessStep> result = service.getLastProcessStep(docUnitId);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getProcessStep().uuid()).isEqualTo(processStepErsterfassung.uuid());
    assertThat(result.get().getProcessStep().name()).isEqualTo(processStepErsterfassung.name());
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .findAllByDocumentationUnitId(docUnitId);
  }

  @Test
  @DisplayName("getLastProcessStep - Should return empty Optional if only one step exists")
  void getLastProcessStep_shouldReturnEmptyOptional_ifOnlyOneStep()
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    // Arrange
    DocumentationUnitProcessStep step1 =
        createMockDocumentationUnitProcessStep(
            processStepNeu.uuid(), processStepNeu.name(), LocalDateTime.now());
    when(documentationUnitProcessStepRepository.findAllByDocumentationUnitId(docUnitId))
        .thenReturn(List.of(step1));

    // Act
    Optional<DocumentationUnitProcessStep> result = service.getLastProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .findAllByDocumentationUnitId(docUnitId);
  }

  @Test
  @DisplayName("getLastProcessStep - Should return empty Optional if no steps exist")
  void getLastProcessStep_shouldReturnEmptyOptional_ifNoSteps()
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    // Arrange
    when(documentationUnitProcessStepRepository.findAllByDocumentationUnitId(docUnitId))
        .thenReturn(List.of());

    // Act
    Optional<DocumentationUnitProcessStep> result = service.getLastProcessStep(docUnitId);

    // Assert
    assertThat(result).isNotPresent();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .findAllByDocumentationUnitId(docUnitId);
  }

  @Test
  @DisplayName(
      "getLastProcessStep - Should throw DocumentationUnitNotExistsException if doc unit not found")
  void getLastProcessStep_shouldThrowDocumentationUnitNotExistsException_ifDocUnitNotFound()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentDocUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(nonExistentDocUnitId))
        .thenThrow(new DocumentationUnitNotExistsException("Doc unit not found for last step"));

    // Act & Assert
    assertThatThrownBy(() -> service.getLastProcessStep(nonExistentDocUnitId))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Doc unit not found for last step");
    verify(documentationUnitRepository, times(1)).findByUuid(nonExistentDocUnitId);
    verify(documentationUnitProcessStepRepository, never()).findAllByDocumentationUnitId(any());
  }

  // --- Tests for getProcessStepHistoryForDocumentationUnit ---

  @Test
  @DisplayName("getProcessStepHistoryForDocumentationUnit - Should return full history if found")
  void getProcessStepHistoryForDocumentationUnit_shouldReturnFullHistory()
      throws DocumentationUnitNotExistsException {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    DocumentationUnitProcessStep step1 =
        createMockDocumentationUnitProcessStep(
            processStepNeu.uuid(), processStepNeu.name(), now.minusMinutes(2));
    DocumentationUnitProcessStep step2 =
        createMockDocumentationUnitProcessStep(
            processStepErsterfassung.uuid(), processStepErsterfassung.name(), now.minusMinutes(1));
    DocumentationUnitProcessStep step3 =
        createMockDocumentationUnitProcessStep(
            processStepQsFormal.uuid(), processStepQsFormal.name(), now);

    List<DocumentationUnitProcessStep> history =
        Arrays.asList(step3, step2, step1); // Ordered newest first
    when(documentationUnitProcessStepRepository.findAllByDocumentationUnitId(docUnitId))
        .thenReturn(history);

    // Act
    List<DocumentationUnitProcessStep> result =
        service.getProcessStepHistoryForDocumentationUnit(docUnitId);

    // Assert
    assertThat(result).hasSize(3).containsExactly(step3, step2, step1);
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .findAllByDocumentationUnitId(docUnitId);
  }

  @Test
  @DisplayName(
      "getProcessStepHistoryForDocumentationUnit - Should return empty list if no history found")
  void getProcessStepHistoryForDocumentationUnit_shouldReturnEmptyList_ifNoHistory()
      throws DocumentationUnitNotExistsException {
    // Arrange
    when(documentationUnitProcessStepRepository.findAllByDocumentationUnitId(docUnitId))
        .thenReturn(List.of());

    // Act
    List<DocumentationUnitProcessStep> result =
        service.getProcessStepHistoryForDocumentationUnit(docUnitId);

    // Assert
    assertThat(result).isEmpty();
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1))
        .findAllByDocumentationUnitId(docUnitId);
  }

  @Test
  @DisplayName(
      "getProcessStepHistoryForDocumentationUnit - Should throw DocumentationUnitNotExistsException if doc unit not found")
  void
      getProcessStepHistoryForDocumentationUnit_shouldThrowDocumentationUnitNotExistsException_ifDocUnitNotFound()
          throws DocumentationUnitNotExistsException {
    // Arrange
    UUID nonExistentDocUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(nonExistentDocUnitId))
        .thenThrow(new DocumentationUnitNotExistsException("Doc unit not found for history"));

    // Act & Assert
    assertThatThrownBy(
            () -> service.getProcessStepHistoryForDocumentationUnit(nonExistentDocUnitId))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Doc unit not found for history");
    verify(documentationUnitRepository, times(1)).findByUuid(nonExistentDocUnitId);
    verify(documentationUnitProcessStepRepository, never()).findAllByDocumentationUnitId(any());
  }

  // --- Tests for getAllProcessStepsForDocOffice ---

  @Test
  @DisplayName("getAllProcessStepsForDocOffice - Should return all steps for office")
  void getAllProcessStepsForDocOffice_shouldReturnAllSteps()
      throws DocumentationOfficeNotExistsException {
    // Arrange
    List<ProcessStep> officeSteps =
        Arrays.asList(processStepNeu, processStepErsterfassung, processStepQsFormal);
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId))
        .thenReturn(officeSteps);

    // Act
    List<ProcessStep> result = service.getAllProcessStepsForDocOffice(docOfficeId);

    // Assert
    assertThat(result)
        .hasSize(3)
        .containsExactly(processStepNeu, processStepErsterfassung, processStepQsFormal);
    verify(documentationOfficeService, times(1)).getProcessStepsForDocumentationOffice(docOfficeId);
  }

  @Test
  @DisplayName("getAllProcessStepsForDocOffice - Should return empty list if office has no steps")
  void getAllProcessStepsForDocOffice_shouldReturnEmptyList_ifNoSteps()
      throws DocumentationOfficeNotExistsException {
    // Arrange
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId))
        .thenReturn(List.of());

    // Act
    List<ProcessStep> result = service.getAllProcessStepsForDocOffice(docOfficeId);

    // Assert
    assertThat(result).isEmpty();
    verify(documentationOfficeService, times(1)).getProcessStepsForDocumentationOffice(docOfficeId);
  }

  @Test
  @DisplayName(
      "getAllProcessStepsForDocOffice - Should throw DocumentationOfficeNotExistsException if office not found")
  void
      getAllProcessStepsForDocOffice_shouldThrowDocumentationOfficeNotExistsException_ifOfficeNotFound()
          throws DocumentationOfficeNotExistsException {
    // Arrange
    UUID nonExistentOfficeId = UUID.randomUUID();
    when(documentationOfficeService.getProcessStepsForDocumentationOffice(nonExistentOfficeId))
        .thenThrow(new DocumentationOfficeNotExistsException("Office not found for process steps"));

    // Act & Assert
    assertThatThrownBy(() -> service.getAllProcessStepsForDocOffice(nonExistentOfficeId))
        .isInstanceOf(DocumentationOfficeNotExistsException.class)
        .hasMessageContaining("Office not found for process steps");
    verify(documentationOfficeService, times(1))
        .getProcessStepsForDocumentationOffice(nonExistentOfficeId);
  }
}
