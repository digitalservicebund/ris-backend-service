package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepMissingException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentationUnitProcessStepService {

  private final DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final ProcessStepRepository processStepRepository;

  public DocumentationUnitProcessStepService(
      DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository,
      DocumentationUnitRepository documentationUnitRepository,
      ProcessStepRepository processStepRepository) {
    this.documentationUnitProcessStepRepository = documentationUnitProcessStepRepository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.processStepRepository = processStepRepository;
  }

  /**
   * Retrieves the current (most recent) process step for a given documentation unit.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A DocumentationUnitProcessStep representing the current step, or null if no steps
   *     exist.
   * @throws DocumentationUnitNotExistsException if the documentation unit is not found.
   */
  @Transactional(readOnly = true)
  public DocumentationUnitProcessStep getCurrentProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    return documentationUnitProcessStepRepository
        .findTopByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnit.uuid())
        .orElseThrow(
            () ->
                new ProcessStepMissingException(
                    "Für Dokeinheit mit ID: "
                        + documentationUnitId
                        + " wurde kein Prozessschritt gefunden, obwohl einer erwartet wurde."));
  }

  /**
   * Retrieves the last completed process step for a given documentation unit. This is defined as
   * the step that was active immediately before the current one, or the last completed step if the
   * current one is also completed.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A ProcessStepDto representing the last step, or null if only one or no steps exist.
   */
  @Transactional(readOnly = true)
  public DocumentationUnitProcessStep getLastProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    List<DocumentationUnitProcessStep> allSteps =
        documentationUnitProcessStepRepository.findByDocumentationUnitOrderByCreatedAtDesc(
            documentationUnit.uuid());

    if (allSteps.size() < 2) {
      return null;
    }
    return allSteps.get(1);
  }

  /**
   * Retrieves the last completed process step for a given documentation unit. This is defined as
   * the step that was active immediately before the current one, or the last completed step if the
   * current one is also completed.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A ProcessStepDto representing the last step, or null if only one or no steps exist.
   */
  @Transactional(readOnly = true)
  public List<DocumentationUnitProcessStep> getAllProcessSteps(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    return documentationUnitProcessStepRepository.findByDocumentationUnitOrderByCreatedAtDesc(
        documentationUnit.uuid());
    ;
  }
  //
  //  /**
  //   * Determines the next logical process step for a documentation unit based on its
  //   * current step and the process flow defined for its documentation office.
  //   *
  //   * @param documentationUnitId The ID of the documentation unit.
  //   * @return A ProcessStepDto representing the next step, or null if no next step is defined.
  //   * @throws ResourceNotFoundException if the documentation unit or its current step is not
  // found,
  //   * or if the documentation unit is not associated with an office.
  //   */
  //  @Transactional(readOnly = true)
  //  public ProcessStep getNextProcessStep(UUID documentationUnitId) {
  //    DocumentationUnit documentationUnit =
  // documentationUnitRepository.findById(documentationUnitId)
  //            .orElseThrow(() -> new ResourceNotFoundException("Documentation Unit not found with
  // ID: " + documentationUnitId));
  //
  //    if (documentationUnit.getDocumentationOffice() == null) {
  //      throw new ResourceNotFoundException("Documentation Unit with ID: " + documentationUnitId +
  // " is not associated with a Documentation Office.");
  //    }
  //
  //    // Get the current process step for the documentation unit
  //    Optional<DocumentationUnitProcessStep> currentDuProcessStepOpt =
  // duProcessStepRepository.findTopByDocumentationUnitOrderByStartedAtDesc(documentationUnit);
  //
  //    if (currentDuProcessStepOpt.isEmpty()) {
  //      // If no current step, the "next" step would be the first step in the office's process
  //      List<ProcessStepDocumentationOffice> officeSteps =
  // psdoRepository.findByDocumentationOfficeOrderByRankAsc(documentationUnit.getDocumentationOffice());
  //      return officeSteps.stream()
  //              .filter(psdo -> psdo.getRank() == 1)
  //              .map(ProcessStepDocumentationOffice::getProcessStep)
  //              .map(processStepTransformer::toDto)
  //              .findFirst()
  //              .orElse(null); // No first step defined for this office
  //    }
  //
  //    ProcessStep currentProcessStep = currentDuProcessStepOpt.get().getProcessStep();
  //    DocumentationOffice office = documentationUnit.getDocumentationOffice();
  //
  //    // Find the rank of the current process step within its documentation office's flow
  //    ProcessStepDocumentationOffice currentPsdo =
  // psdoRepository.findByProcessStepAndDocumentationOffice(currentProcessStep, office)
  //            .orElseThrow(() -> new ResourceNotFoundException("Current process step (" +
  // currentProcessStep.getName() + ") not found in the process flow for office: " +
  // office.getAbbreviation()));
  //
  //    Integer currentRank = currentPsdo.getRank();
  //    Integer nextRank = currentRank + 1;
  //
  //    // Find the next step in the sequence for this documentation office
  //    return psdoRepository.findByRankAndDocumentationOffice(nextRank, office)
  //            .map(ProcessStepDocumentationOffice::getProcessStep)
  //            .map(processStepTransformer::toDto)
  //            .orElse(null); // No next step defined (e.g., current is the last step)
  //  }
  //
  //  /**
  //   * Saves a new process step for a documentation unit, marking the previous step as completed.
  //   *
  //   * @param documentationUnitId The ID of the documentation unit.
  //   * @param request The request DTO containing the new process step ID and optional remarks.
  //   * @return A ProcessStepDto representing the newly saved process step.
  //   * @throws ResourceNotFoundException if the documentation unit or the new process step is not
  // found.
  //   */
  //  @Transactional
  //  public ProcessStep saveProcessStep(UUID documentationUnitId, UUID processStepId) {
  //    DocumentationUnit documentationUnit =
  // documentationUnitRepository.findById(documentationUnitId)
  //            .orElseThrow(() -> new ResourceNotFoundException("Documentation Unit not found with
  // ID: " + documentationUnitId));
  //
  //    ProcessStep newProcessStep = processStepRepository.findById(processStepId)
  //            .orElseThrow(() -> new ResourceNotFoundException("Process Step not found with ID: "
  // + processStepId));
  //
  //    // Mark the last (current) process step as completed (if exists and not already completed)
  //    duProcessStepRepository.findTopByDocumentationUnitOrderByStartedAtDesc(documentationUnit)
  //            .ifPresent(lastStep -> {
  //              if (lastStep.getCompletedAt() == null) {
  //                lastStep.setCompletedAt(LocalDateTime.now());
  //                duProcessStepRepository.save(lastStep);
  //              }
  //            });
  //
  //    // Create and save the new process step entry
  //    DocumentationUnitProcessStep newDuProcessStep = new DocumentationUnitProcessStep();
  //    newDuProcessStep.setId(UUID.randomUUID()); // Generate a new UUID for this history entry
  //    newDuProcessStep.setDocumentationUnit(documentationUnit);
  //    newDuProcessStep.setProcessStep(newProcessStep);
  //    newDuProcessStep.setStartedAt(LocalDateTime.now());
  //    newDuProcessStep.setRemarks(request.getRemarks());
  //
  //    DocumentationUnitProcessStep savedDuProcessStep =
  // duProcessStepRepository.save(newDuProcessStep);
  //
  //    // Return the DTO of the newly saved ProcessStep
  //    return processStepTransformer.toDto(savedDuProcessStep.getProcessStep());
  //  }
  //
  //  /**
  //   * Retrieves a list of all possible process steps for a given documentation office,
  //   * ordered by their defined rank.
  //   *
  //   * @param docOfficeId The ID of the documentation office.
  //   * @return A list of ProcessStep objects.
  //   * @throws ResourceNotFoundException if the documentation office is not found.
  //   */
  //  @Transactional(readOnly = true)
  //  public List<ProcessStep> getAllPossibleProcessStepsByDocOffice(UUID docOfficeId) {
  //    DocumentationOffice documentationOffice =
  // documentationOfficeRepository.findById(docOfficeId)
  //            .orElseThrow(() -> new ResourceNotFoundException("Documentation Office not found
  // with ID: " + docOfficeId));
  //
  //    return psdoRepository.findByDocumentationOfficeOrderByRankAsc(documentationOffice)
  //            .stream()
  //            .map(ProcessStepDocumentationOffice::getProcessStep)
  //            .collect(Collectors.toList());
  //  }
  //
  //  /**
  //   * Retrieves the complete history of process steps for a given documentation unit,
  //   * ordered by the time each step was started (creation time).
  //   *
  //   * @param documentationUnitId The ID of the documentation unit.
  //   * @return A list of DocumentationUnitProcessStep objects representing the history.
  //   * @throws ResourceNotFoundException if the documentation unit is not found.
  //   */
  //  @Transactional(readOnly = true)
  //  public List<ProcessStep> getProcessHistoryForDocumentationUnit(UUID documentationUnitId) {
  //    DocumentationUnit documentationUnit =
  // documentationUnitRepository.findById(documentationUnitId)
  //            .orElseThrow(() -> new ResourceNotFoundException("Documentation Unit not found with
  // ID: " + documentationUnitId));
  //
  //    // The repository method already orders by startedAtAsc, which serves as creation order
  //    return
  // duProcessStepRepository.findByDocumentationUnitOrderByStartedAtAsc(documentationUnit);
  //  }
}
