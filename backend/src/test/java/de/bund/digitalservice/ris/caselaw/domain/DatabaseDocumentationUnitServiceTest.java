package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DatabaseDocumentationUnitService.class})
class DatabaseDocumentationUnitServiceTest {
  private static final UUID TEST_DOC_UNIT_ID_1 = UUID.randomUUID();
  private static final UUID TEST_DOC_UNIT_ID_2 = UUID.randomUUID();
  private static final UUID TEST_USER_ID = UUID.randomUUID();
  private static final UUID TEST_USER_EXTERNAL_ID = UUID.randomUUID();
  private static final UUID TEST_PROCESS_STEP_ID = UUID.randomUUID();
  private static final String TEST_PROCESS_STEP_NAME = "TestStep";

  @Autowired private DatabaseDocumentationUnitService service;

  // Mock the repositories the service talks to
  @MockitoBean private DatabaseDocumentationUnitRepository repository;
  @MockitoBean private DatabaseProcessStepRepository processStepRepository;
  @MockitoBean private DatabaseUserRepository userRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final ProcessStepDTO processStepDTO =
      ProcessStepDTO.builder().id(TEST_PROCESS_STEP_ID).name(TEST_PROCESS_STEP_NAME).build();
  private final UserDTO userDTO =
      UserDTO.builder().id(TEST_USER_ID).externalId(TEST_USER_EXTERNAL_ID).build();

  private final DecisionDTO docUnitDTO1 =
      DecisionDTO.builder().id(TEST_DOC_UNIT_ID_1).documentNumber("doc-1").build();
  private final DecisionDTO docUnitDTO2 =
      DecisionDTO.builder().id(TEST_DOC_UNIT_ID_2).documentNumber("doc-2").build();

  private final DocumentationUnitProcessStep documentationUnitProcessStepWithUser =
      DocumentationUnitProcessStep.builder()
          .processStep(ProcessStep.builder().name(TEST_PROCESS_STEP_NAME).build())
          .user(User.builder().externalId(TEST_USER_EXTERNAL_ID).build())
          .build();

  private final DocumentationUnitProcessStep documentationUnitProcessStepWithoutUser =
      DocumentationUnitProcessStep.builder()
          .processStep(ProcessStep.builder().name(TEST_PROCESS_STEP_NAME).build())
          .user(null)
          .build();

  @BeforeEach
  void setUp() {
    Mockito.reset(repository, processStepRepository, userRepository, documentationOfficeRepository);
  }

  @Nested
  class BulkAssignProcessStep {
    @Test
    void testBulkAssignProcessStep_withValidDataAndUser_shouldSucceed() throws Exception {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(userRepository.findByExternalId(any(UUID.class))).thenReturn(Optional.of(userDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO1));
      when(repository.findById(TEST_DOC_UNIT_ID_2)).thenReturn(Optional.of(docUnitDTO2));
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO1, docUnitDTO2);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1, TEST_DOC_UNIT_ID_2), documentationUnitProcessStepWithUser);

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, times(1)).findByExternalId(TEST_USER_EXTERNAL_ID);
      verify(repository, times(2)).findById(any(UUID.class));

      // Capture the arguments passed to the save method
      ArgumentCaptor<DocumentationUnitDTO> documentationUnitCaptor =
          ArgumentCaptor.forClass(DocumentationUnitDTO.class);
      verify(repository, times(2)).save(documentationUnitCaptor.capture());

      // Get ALL captured arguments
      List<DocumentationUnitDTO> savedDocUnits = documentationUnitCaptor.getAllValues();

      // Assert that the list contains two elements
      assertEquals(2, savedDocUnits.size());

      // Assert that each captured DTO contains the new process step and user
      for (DocumentationUnitDTO savedDocUnit : savedDocUnits) {
        assertNotNull(savedDocUnit.getCurrentProcessStep());
        assertEquals(
            processStepDTO.getName(),
            savedDocUnit.getCurrentProcessStep().getProcessStep().getName());
        assertNotNull(savedDocUnit.getCurrentProcessStep().getUser());
        assertEquals(userDTO.getId(), savedDocUnit.getCurrentProcessStep().getUser().getId());
      }
    }

    @Test
    void testBulkAssignProcessStep_withValidDataWithoutUser_shouldSucceed() throws Exception {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO1));
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO1);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), documentationUnitProcessStepWithoutUser);

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, never()).findByExternalId(any());
      verify(repository, times(1)).findById(any(UUID.class));

      // Capture the arguments passed to the save method
      ArgumentCaptor<DocumentationUnitDTO> documentationUnitCaptor =
          ArgumentCaptor.forClass(DocumentationUnitDTO.class);
      verify(repository, times(1)).save(documentationUnitCaptor.capture());
      DocumentationUnitDTO savedDocUnit = documentationUnitCaptor.getValue();

      // Assert that the captured DTO contain the new process step and user
      assertNotNull(savedDocUnit.getCurrentProcessStep());
      assertEquals(
          processStepDTO.getName(),
          savedDocUnit.getCurrentProcessStep().getProcessStep().getName());
      assertNull(savedDocUnit.getCurrentProcessStep().getUser());
    }

    @Test
    void testBulkAssignProcessStep_withProcessStepNotFound_shouldThrowException() {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME)).thenReturn(Optional.empty());
      List<UUID> docunitList = List.of(TEST_DOC_UNIT_ID_1);

      // Act & Assert
      assertThrows(
          ProcessStepNotFoundException.class,
          () -> service.bulkAssignProcessStep(docunitList, documentationUnitProcessStepWithUser));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(repository, never()).findById(any(UUID.class));
    }

    @Test
    void testBulkAssignProcessStep_withUserNotFound_shouldThrowException() {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(userRepository.findByExternalId(any())).thenReturn(Optional.empty());

      List<UUID> docunitList = List.of(TEST_DOC_UNIT_ID_1);

      // Act & Assert
      assertThrows(
          DocumentationUnitException.class,
          () -> service.bulkAssignProcessStep(docunitList, documentationUnitProcessStepWithUser));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, times(1)).findByExternalId(any());
      verify(repository, never()).findById(any(UUID.class));
    }

    @Test
    void testBulkAssignProcessStep_withUnsupportedDocumentationUnitType_shouldThrowException() {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(repository.findById(any(UUID.class)))
          .thenReturn(Optional.of(PendingProceedingDTO.builder().build()));

      List<UUID> docunitList = List.of(TEST_DOC_UNIT_ID_1);

      // Act & Assert
      assertThrows(
          BadRequestException.class,
          () ->
              service.bulkAssignProcessStep(docunitList, documentationUnitProcessStepWithoutUser));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(repository, times(1)).findById(any(UUID.class));
      verify(repository, never()).save(any());
    }
  }
}
