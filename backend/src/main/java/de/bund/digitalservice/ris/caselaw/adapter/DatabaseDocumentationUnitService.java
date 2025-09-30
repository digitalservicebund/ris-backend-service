package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.BulkDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseDocumentationUnitService implements BulkDocumentationUnitService {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseProcessStepRepository processStepRepository;
  private final DatabaseUserRepository userRepository;
  private final DocumentationUnitHistoryLogService historyLogService;
  private final DatabaseDocumentationUnitProcessStepRepository
      databaseDocumentationUnitProcessStepRepository;

  public DatabaseDocumentationUnitService(
      DatabaseDocumentationUnitRepository repository,
      DatabaseProcessStepRepository processStepRepository,
      DatabaseUserRepository userRepository,
      DocumentationUnitHistoryLogService historyLogService,
      DatabaseDocumentationUnitProcessStepRepository
          databaseDocumentationUnitProcessStepRepository) {
    super();
    this.repository = repository;
    this.processStepRepository = processStepRepository;
    this.userRepository = userRepository;
    this.historyLogService = historyLogService;
    this.databaseDocumentationUnitProcessStepRepository =
        databaseDocumentationUnitProcessStepRepository;
  }

  @Transactional(rollbackFor = BadRequestException.class)
  @Override
  public void bulkAssignProcessStep(
      @NotNull List<UUID> documentationUnitIds,
      DocumentationUnitProcessStep documentationUnitProcessStep,
      @Nullable User currentUser)
      throws BadRequestException, ProcessStepNotFoundException, DocumentationUnitException {

    // Extracted initial validation/lookups to reduce complexity
    ProcessStepDTO processStepDTO = findProcessStep(documentationUnitProcessStep);
    UserDTO userDTO = findUser(documentationUnitProcessStep);

    for (UUID documentationUnitId : documentationUnitIds) {
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnitId).orElse(null);
      if (documentationUnitDTO instanceof DecisionDTO) {

        DocumentationUnitProcessStepDTO currentDocumentationUnitProcessStepDTOFromDB =
            documentationUnitDTO.getCurrentProcessStep();

        boolean stepChanged =
            stepChanged(currentDocumentationUnitProcessStepDTOFromDB, processStepDTO);
        boolean userChanged = userChanged(currentDocumentationUnitProcessStepDTOFromDB, userDTO);

        if (stepChanged || userChanged) {
          handleProcessStepUpdateAndLog(
              documentationUnitDTO,
              processStepDTO,
              userDTO,
              currentUser,
              currentDocumentationUnitProcessStepDTOFromDB,
              stepChanged,
              userChanged);
        }
      } else {
        throw new BadRequestException("Can only assign process steps to decisions.");
      }
    }
  }

  private ProcessStepDTO findProcessStep(DocumentationUnitProcessStep documentationUnitProcessStep)
      throws ProcessStepNotFoundException {
    return processStepRepository
        .findByName(documentationUnitProcessStep.getProcessStep().name())
        .orElseThrow(
            () ->
                new ProcessStepNotFoundException(
                    "Process step with name "
                        + documentationUnitProcessStep.getProcessStep().name()
                        + " not found"));
  }

  private @Nullable UserDTO findUser(DocumentationUnitProcessStep documentationUnitProcessStep)
      throws DocumentationUnitException {
    UserDTO userDTO = null;
    if (documentationUnitProcessStep.getUser() != null) {
      userDTO =
          userRepository
              .findById(documentationUnitProcessStep.getUser().id())
              .orElseThrow(
                  () ->
                      new DocumentationUnitException(
                          "User with id "
                              + documentationUnitProcessStep.getUser().id()
                              + " not found"));
    }
    return userDTO;
  }

  private void handleProcessStepUpdateAndLog(
      DocumentationUnitDTO documentationUnitDTO,
      ProcessStepDTO processStepDTO,
      UserDTO userDTO,
      @Nullable User currentUser,
      @Nullable DocumentationUnitProcessStepDTO currentDocumentationUnitProcessStepDTOFromDB,
      boolean stepChanged,
      boolean userChanged) {

    DocumentationUnitProcessStepDTO newDocumentationUnitProcessStepDTO =
        createAndSaveNewProcessStep(documentationUnitDTO, processStepDTO, userDTO);

    if (stepChanged) {
      String description =
          getProcessStepHistoryLogDescription(
              currentDocumentationUnitProcessStepDTOFromDB, newDocumentationUnitProcessStepDTO);

      historyLogService.saveProcessStepHistoryLog(
          documentationUnitDTO.getId(),
          currentUser,
          null,
          HistoryLogEventType.PROCESS_STEP,
          description,
          DocumentationUnitProcessStepTransformer.toDomain(
              currentDocumentationUnitProcessStepDTOFromDB),
          DocumentationUnitProcessStepTransformer.toDomain(newDocumentationUnitProcessStepDTO));
    }
    if (userChanged) {
      historyLogService.saveProcessStepHistoryLog(
          documentationUnitDTO.getId(),
          currentUser,
          null,
          HistoryLogEventType.PROCESS_STEP_USER,
          null, // description will be set dynamically in transformer.toDomain
          DocumentationUnitProcessStepTransformer.toDomain(
              currentDocumentationUnitProcessStepDTOFromDB),
          DocumentationUnitProcessStepTransformer.toDomain(newDocumentationUnitProcessStepDTO));
    }
  }

  private static String getProcessStepHistoryLogDescription(
      DocumentationUnitProcessStepDTO currentDocumentationUnitProcessStepDTOFromDB,
      DocumentationUnitProcessStepDTO newDocumentationUnitProcessStepDTO) {
    Optional<ProcessStepDTO> fromProcess =
        Optional.ofNullable(currentDocumentationUnitProcessStepDTOFromDB)
            .map(DocumentationUnitProcessStepDTO::getProcessStep);
    Optional<ProcessStepDTO> toProcess =
        Optional.ofNullable(newDocumentationUnitProcessStepDTO)
            .map(DocumentationUnitProcessStepDTO::getProcessStep);

    if (toProcess.isEmpty()) {
      throw new IllegalStateException(
          "Could not save history log because new process step is null");
    }

    return fromProcess
        .map(
            processStepDTO ->
                String.format(
                    "Schritt geändert: %s → %s",
                    processStepDTO.getName(), toProcess.get().getName()))
        .orElseGet(() -> "Schritt gesetzt: " + toProcess.get().getName());
  }

  private boolean stepChanged(
      DocumentationUnitProcessStepDTO currentDocumentationUnitProcessStepDTOFromDB,
      ProcessStepDTO newProcessStepDTO) {
    if (currentDocumentationUnitProcessStepDTOFromDB == null) {
      return true;
    }
    return !currentDocumentationUnitProcessStepDTOFromDB.getProcessStep().equals(newProcessStepDTO);
  }

  private boolean userChanged(
      DocumentationUnitProcessStepDTO currentDocumentationUnitProcessStepDTOFromDB,
      UserDTO newUserDTO) {
    if (currentDocumentationUnitProcessStepDTOFromDB == null) {
      return true;
    }
    UserDTO currentUserInDb = currentDocumentationUnitProcessStepDTOFromDB.getUser();
    return !Objects.equals(currentUserInDb, newUserDTO);
  }

  private DocumentationUnitProcessStepDTO createAndSaveNewProcessStep(
      DocumentationUnitDTO documentationUnitDTO, ProcessStepDTO processStepDTO, UserDTO userDTO) {

    DocumentationUnitProcessStepDTO newDocumentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .user(userDTO)
            .createdAt(LocalDateTime.now())
            .processStep(processStepDTO)
            .documentationUnit(documentationUnitDTO)
            .build();

    newDocumentationUnitProcessStepDTO =
        databaseDocumentationUnitProcessStepRepository.save(newDocumentationUnitProcessStepDTO);

    documentationUnitDTO.getProcessSteps().add(newDocumentationUnitProcessStepDTO);
    documentationUnitDTO.setCurrentProcessStep(newDocumentationUnitProcessStepDTO);
    repository.save(documentationUnitDTO);

    return newDocumentationUnitProcessStepDTO;
  }
}
