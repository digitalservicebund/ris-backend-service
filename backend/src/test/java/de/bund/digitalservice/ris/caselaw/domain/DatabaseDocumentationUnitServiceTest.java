package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.time.LocalDateTime;
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
  private static final UUID TEST_PROCESS_STEP_ID_OLD = UUID.randomUUID();
  private static final String TEST_PROCESS_STEP_NAME = "TestStep";
  private static final String TEST_PROCESS_STEP_NAME_OLD = "OldStep";
  private static final UUID TEST_CURRENT_USER_ID = UUID.randomUUID();
  private static final User CURRENT_USER = User.builder().id(TEST_CURRENT_USER_ID).build();

  @Autowired private DatabaseDocumentationUnitService service;

  // Mock the repositories the service talks to
  @MockitoBean private DatabaseDocumentationUnitRepository repository;
  @MockitoBean private DatabaseProcessStepRepository processStepRepository;
  @MockitoBean private DatabaseUserRepository userRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  @MockitoBean
  private DatabaseDocumentationUnitProcessStepRepository
      databaseDocumentationUnitProcessStepRepository;

  private final ProcessStepDTO processStepDTO =
      ProcessStepDTO.builder().id(TEST_PROCESS_STEP_ID).name(TEST_PROCESS_STEP_NAME).build();
  private final ProcessStepDTO processStepDTO_OLD =
      ProcessStepDTO.builder()
          .id(TEST_PROCESS_STEP_ID_OLD)
          .name(TEST_PROCESS_STEP_NAME_OLD)
          .build();
  private final UserDTO userDTO =
      UserDTO.builder().id(TEST_USER_ID).externalId(TEST_USER_EXTERNAL_ID).build();

  private final DecisionDTO docUnitDTO =
      DecisionDTO.builder().id(TEST_DOC_UNIT_ID_1).documentNumber("doc-1").build();
  private final DecisionDTO docUnitDTO2 =
      DecisionDTO.builder().id(TEST_DOC_UNIT_ID_2).documentNumber("doc-2").build();

  private final DocumentationUnitProcessStep documentationUnitProcessStepWithUser =
      DocumentationUnitProcessStep.builder()
          .processStep(ProcessStep.builder().name(TEST_PROCESS_STEP_NAME).build())
          .user(User.builder().id(TEST_USER_ID).build())
          .build();

  private final DocumentationUnitProcessStep documentationUnitProcessStepWithoutUser =
      DocumentationUnitProcessStep.builder()
          .processStep(ProcessStep.builder().name(TEST_PROCESS_STEP_NAME).build())
          .user(null)
          .build();

  // Helper for DTO being saved (since the service saves it first and then assigns it)
  private DocumentationUnitProcessStepDTO createNewProcessStepDTO(
      DocumentationUnitDTO docUnitDTO, UserDTO userDTO, ProcessStepDTO processStepDTO) {
    return DocumentationUnitProcessStepDTO.builder()
        .id(UUID.randomUUID()) // Assign a new ID to simulate a saved entity
        .user(userDTO)
        .createdAt(LocalDateTime.now())
        .processStep(processStepDTO)
        .documentationUnit(docUnitDTO)
        .build();
  }

  @BeforeEach
  void setUp() {
    Mockito.reset(
        repository,
        processStepRepository,
        userRepository,
        documentationOfficeRepository,
        historyLogService,
        databaseDocumentationUnitProcessStepRepository);

    // Default mock for the new ProcessStepDTO being created and saved
    // This is essential because, to simulate this successful persistence
    // behavior for the service logic to run correctly.
    when(databaseDocumentationUnitProcessStepRepository.save(
            any(DocumentationUnitProcessStepDTO.class)))
        .thenAnswer(
            invocation -> {
              DocumentationUnitProcessStepDTO dto = invocation.getArgument(0);
              // Simulate saving by assigning an ID if not present (although usually set in builder,
              // safer to ensure)
              if (dto.getId() == null) {
                return dto.toBuilder().id(UUID.randomUUID()).build();
              }
              return dto;
            });
  }

  @Nested
  class BulkAssignProcessStep {

    @Test
    void testBulkAssignProcessStep_withValidDataAndUser_shouldSucceed() throws Exception {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));

      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_2)).thenReturn(Optional.of(docUnitDTO2));
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO, docUnitDTO2);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1, TEST_DOC_UNIT_ID_2),
          documentationUnitProcessStepWithUser,
          CURRENT_USER);

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, times(1)).findById(TEST_USER_ID);
      verify(repository, times(2)).findById(any(UUID.class));

      // Capture the arguments passed to the save method
      ArgumentCaptor<DocumentationUnitDTO> documentationUnitCaptor =
          ArgumentCaptor.forClass(DocumentationUnitDTO.class);
      verify(repository, times(2)).save(documentationUnitCaptor.capture());

      // Get ALL captured arguments
      List<DocumentationUnitDTO> savedDocUnits = documentationUnitCaptor.getAllValues();
      assertEquals(2, savedDocUnits.size());

      // Verify history logs (2 docs * 2 events each = 4 logs)
      verify(historyLogService, times(4))
          .saveProcessStepHistoryLog(
              any(UUID.class),
              eq(CURRENT_USER),
              any(),
              any(HistoryLogEventType.class),
              any(),
              any(),
              any());
    }

    @Test
    void testBulkAssignProcessStep_withValidDataWithoutUser_shouldSucceed() throws Exception {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), documentationUnitProcessStepWithoutUser, CURRENT_USER);

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, never()).findById(any()); // **ASSERTION: No user lookup**
      verify(repository, times(1)).findById(any(UUID.class));

      // Verify history logs (1 log for initial step assignment)
      verify(historyLogService, times(1))
          .saveProcessStepHistoryLog(
              eq(TEST_DOC_UNIT_ID_1),
              eq(CURRENT_USER),
              any(),
              eq(HistoryLogEventType.PROCESS_STEP),
              eq("Schritt gesetzt: " + TEST_PROCESS_STEP_NAME),
              eq(null),
              any(DocumentationUnitProcessStep.class));
    }

    @Test
    void testBulkAssignProcessStep_withProcessStepNotFound_shouldThrowException() {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME)).thenReturn(Optional.empty());
      List<UUID> docunitList = List.of(TEST_DOC_UNIT_ID_1);

      // Act & Assert
      assertThrows(
          ProcessStepNotFoundException.class,
          () ->
              service.bulkAssignProcessStep(
                  docunitList, documentationUnitProcessStepWithUser, CURRENT_USER));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(repository, never()).findById(any(UUID.class));
      verify(userRepository, never()).findById(any());
      verify(historyLogService, never())
          .saveProcessStepHistoryLog(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testBulkAssignProcessStep_withUserNotFound_shouldThrowException() {
      // Arrange
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      // **ADAPTED MOCK: findById returns empty**
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

      List<UUID> docunitList = List.of(TEST_DOC_UNIT_ID_1);

      // Act & Assert
      assertThrows(
          DocumentationUnitException.class,
          () ->
              service.bulkAssignProcessStep(
                  docunitList, documentationUnitProcessStepWithUser, CURRENT_USER));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(userRepository, times(1)).findById(TEST_USER_ID); // **ASSERTION: Verify findById**
      verify(repository, never()).findById(any(UUID.class));
      verify(historyLogService, never())
          .saveProcessStepHistoryLog(any(), any(), any(), any(), any(), any(), any());
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
              service.bulkAssignProcessStep(
                  docunitList, documentationUnitProcessStepWithoutUser, CURRENT_USER));

      // Assert
      verify(processStepRepository, times(1)).findByName(TEST_PROCESS_STEP_NAME);
      verify(repository, times(1)).findById(any(UUID.class));
      verify(repository, never()).save(any());
      verify(historyLogService, never())
          .saveProcessStepHistoryLog(any(), any(), any(), any(), any(), any(), any());
    }

    // Tests for history logs

    @Test
    void testBulkAssignProcessStep_initialAssignmentWithUser_shouldSucceedAndLogBothStepAndUser()
        throws Exception {
      // Arrange
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userDTO));
      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO));
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));

      // We need to simulate the saved DTO to return it from the captor later
      DocumentationUnitProcessStepDTO newProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, userDTO, processStepDTO);
      when(databaseDocumentationUnitProcessStepRepository.save(
              any(DocumentationUnitProcessStepDTO.class)))
          .thenReturn(newProcessStepDTO);
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), documentationUnitProcessStepWithUser, CURRENT_USER);

      // Assert
      verify(userRepository, times(1)).findById(TEST_USER_ID);
      verify(repository, times(1)).save(any(DecisionDTO.class));
      verify(databaseDocumentationUnitProcessStepRepository, times(1))
          .save(any(DocumentationUnitProcessStepDTO.class));

      // Assert history logs
      verify(historyLogService, times(1))
          .saveProcessStepHistoryLog(
              eq(TEST_DOC_UNIT_ID_1),
              eq(CURRENT_USER),
              eq(null),
              eq(HistoryLogEventType.PROCESS_STEP),
              eq("Schritt gesetzt: " + TEST_PROCESS_STEP_NAME),
              eq(null),
              any(DocumentationUnitProcessStep.class));

      verify(historyLogService, times(1))
          .saveProcessStepHistoryLog(
              eq(TEST_DOC_UNIT_ID_1),
              eq(CURRENT_USER),
              eq(null),
              eq(HistoryLogEventType.PROCESS_STEP_USER),
              eq(null),
              eq(null),
              any(DocumentationUnitProcessStep.class));
    }

    @Test
    void testBulkAssignProcessStep_changeStepOnly_shouldLogStepChangeOnly() throws Exception {
      // Arrange
      // DocUnit has a currrent step and no user
      DocumentationUnitProcessStepDTO currentProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, null, processStepDTO_OLD);
      docUnitDTO.setCurrentProcessStep(currentProcessStepDTO);

      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO)); // New step
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));

      // New documentationUnitProcessStep is without user
      DocumentationUnitProcessStep newProcessStepDomain = documentationUnitProcessStepWithoutUser;

      // Simulate the saved DTO
      DocumentationUnitProcessStepDTO newProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, null, processStepDTO);
      when(databaseDocumentationUnitProcessStepRepository.save(
              any(DocumentationUnitProcessStepDTO.class)))
          .thenReturn(newProcessStepDTO);
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), newProcessStepDomain, CURRENT_USER);

      // Assert
      verify(userRepository, never()).findById(any());

      // Only step changed, user stayed null
      verify(historyLogService, times(1))
          .saveProcessStepHistoryLog(
              eq(TEST_DOC_UNIT_ID_1),
              eq(CURRENT_USER),
              eq(null),
              eq(HistoryLogEventType.PROCESS_STEP),
              eq(
                  "Schritt geändert: "
                      + TEST_PROCESS_STEP_NAME_OLD
                      + " → "
                      + TEST_PROCESS_STEP_NAME),
              any(DocumentationUnitProcessStep.class),
              any(DocumentationUnitProcessStep.class));

      verify(historyLogService, never())
          .saveProcessStepHistoryLog(
              any(UUID.class),
              any(User.class),
              any(),
              eq(HistoryLogEventType.PROCESS_STEP_USER),
              any(),
              any(),
              any());
    }

    @Test
    void testBulkAssignProcessStep_changeUserOnly_shouldLogUserChangeOnly() throws Exception {
      // Arrange
      // DocUnit has TEST_PROCESS_STEP_NAME as current process step and no user
      DocumentationUnitProcessStepDTO currentProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, null, processStepDTO);
      docUnitDTO.setCurrentProcessStep(currentProcessStepDTO);

      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO)); // Same step

      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userDTO)); // New user
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));

      // New documentationUnitProcessStep is with user
      DocumentationUnitProcessStep newProcessStepDomain = documentationUnitProcessStepWithUser;

      // Simulate the saved DTO
      DocumentationUnitProcessStepDTO newProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, userDTO, processStepDTO);
      when(databaseDocumentationUnitProcessStepRepository.save(
              any(DocumentationUnitProcessStepDTO.class)))
          .thenReturn(newProcessStepDTO);
      when(repository.save(any(DecisionDTO.class))).thenReturn(docUnitDTO);

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), newProcessStepDomain, CURRENT_USER);

      // Assert
      verify(userRepository, times(1)).findById(TEST_USER_ID);

      // Only user changed, step is the same
      verify(historyLogService, never())
          .saveProcessStepHistoryLog(
              any(UUID.class),
              any(User.class),
              any(),
              eq(HistoryLogEventType.PROCESS_STEP),
              any(),
              any(),
              any());

      verify(historyLogService, times(1))
          .saveProcessStepHistoryLog(
              eq(TEST_DOC_UNIT_ID_1),
              eq(CURRENT_USER),
              eq(null),
              eq(HistoryLogEventType.PROCESS_STEP_USER),
              eq(null),
              any(DocumentationUnitProcessStep.class),
              any(DocumentationUnitProcessStep.class));
    }

    @Test
    void testBulkAssignProcessStep_noChangeInStepOrUser_shouldNotSaveNewStepOrLogHistory()
        throws Exception {
      // Arrange
      // DocUnit has TEST_PROCESS_STEP_NAME as current process step and a userDTO as currently
      // assigned user
      DocumentationUnitProcessStepDTO currentProcessStepDTO =
          createNewProcessStepDTO(docUnitDTO, userDTO, processStepDTO);
      docUnitDTO.setCurrentProcessStep(currentProcessStepDTO);

      when(processStepRepository.findByName(TEST_PROCESS_STEP_NAME))
          .thenReturn(Optional.of(processStepDTO)); // Same step

      // **ADAPTED MOCK: Use findById with TEST_USER_ID**
      when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userDTO)); // Same user
      when(repository.findById(TEST_DOC_UNIT_ID_1)).thenReturn(Optional.of(docUnitDTO));

      // Act
      service.bulkAssignProcessStep(
          List.of(TEST_DOC_UNIT_ID_1), documentationUnitProcessStepWithUser, CURRENT_USER);

      // Assert
      verify(userRepository, times(1)).findById(TEST_USER_ID);

      // No change, so no new process step or history log should be saved
      verify(databaseDocumentationUnitProcessStepRepository, never())
          .save(any(DocumentationUnitProcessStepDTO.class));
      verify(repository, never()).save(any(DecisionDTO.class));
      verify(historyLogService, never())
          .saveProcessStepHistoryLog(
              any(UUID.class),
              any(User.class),
              any(),
              any(HistoryLogEventType.class),
              any(),
              any(),
              any());
    }
  }
}
