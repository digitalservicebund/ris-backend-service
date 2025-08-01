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

  @InjectMocks private ProcessStepService service;

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
    Optional<ProcessStep> result = service.getNextProcessStepForDocOffice(docUnitId);

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
    Optional<ProcessStep> result = service.getNextProcessStepForDocOffice(docUnitId);

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
    Optional<ProcessStep> result = service.getNextProcessStepForDocOffice(docUnitId);

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
    Optional<ProcessStep> result = service.getNextProcessStepForDocOffice(docUnitId);

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
    assertThatThrownBy(() -> service.getNextProcessStepForDocOffice(nonExistentDocUnitId))
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
    assertThatThrownBy(() -> service.getNextProcessStepForDocOffice(docUnitId))
        .isInstanceOf(DocumentationOfficeNotExistsException.class)
        .hasMessageContaining("Office not found for next step calculation");
    verify(documentationUnitRepository, times(1)).findByUuid(docUnitId);
    verify(documentationUnitProcessStepRepository, times(1)).getCurrentProcessStep(docUnitId);
    verify(documentationOfficeService, times(1))
        .getProcessStepsForDocumentationOffice(nonExistentOfficeId);
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
