package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentationUnitProcessStepService {

  private final DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final DocumentationOfficeService documentationOfficeService;

  public DocumentationUnitProcessStepService(
      DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository,
      DocumentationUnitRepository documentationUnitRepository,
      DocumentationOfficeService documentationOfficeService) {
    this.documentationUnitProcessStepRepository = documentationUnitProcessStepRepository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.documentationOfficeService = documentationOfficeService;
  }

  /**
   * Retrieves the current (most recent) process step for a given documentation unit.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A DocumentationUnitProcessStep representing the current step.
   * @throws DocumentationUnitNotExistsException if the documentation unit is not found.
   * @throws ProcessStepNotFoundException if the current process step associated with that
   *     documentation unit cannot be found.
   */
  public Optional<DocumentationUnitProcessStep> getCurrentProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    return documentationUnitProcessStepRepository.getCurrentProcessStep(documentationUnit.uuid());
  }

  /**
   * Determines the next logical process step for a documentation unit based on its current step and
   * the process flow defined for its documentation office.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return An optional ProcessStep representing the next step or empty if no next step found.
   * @throws DocumentationUnitNotExistsException if the documentation unit is not found.
   * @throws DocumentationOfficeNotExistsException if the documentation office associated with the
   *     documentation unit is not found.
   */
  @Transactional(readOnly = true)
  public Optional<ProcessStep> getNextProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    return documentationUnitProcessStepRepository
        .getCurrentProcessStep(documentationUnit.uuid())
        .flatMap(
            currentProcessStep -> {
              List<ProcessStep> orderedOfficeProcessSteps =
                  documentationOfficeService.getProcessStepsForDocumentationOffice(
                      documentationUnit.coreData().documentationOffice().id());

              int currentIndex =
                  orderedOfficeProcessSteps.indexOf(currentProcessStep.getProcessStep());

              // If the current step was found in the list and it's not the last one
              if (currentIndex != -1 && currentIndex < orderedOfficeProcessSteps.size() - 1) {
                return Optional.of(orderedOfficeProcessSteps.get(currentIndex + 1));
              } else {
                return Optional.empty(); // No next step
              }
            });
  }

  /**
   * Retrieves the last process step for a given documentation unit. This is defined as the step
   * that was active immediately before the current one.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return An Optional containing the last DocumentationUnitProcessStep if found, or an empty
   *     Optional.
   * @throws DocumentationUnitNotExistsException if the documentation unit itself does not exist.
   * @throws ProcessStepNotFoundException if the current process step for the documentation unit
   *     cannot be found.
   */
  @Transactional(readOnly = true)
  public Optional<DocumentationUnitProcessStep> getLastProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    List<DocumentationUnitProcessStep> allSteps =
        documentationUnitProcessStepRepository.findAllByDocumentationUnitId(
            documentationUnit.uuid());

    if (allSteps.size() < 2) {
      return Optional.empty();
    }
    return Optional.of(allSteps.get(1));
  }

  /**
   * Retrieves the process step history for a given documentation unit, ordered by createdAt in
   * descending order.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A list of DocumentationUnitProcessStep representing the process step history. Returns
   *     an empty list if no history is found.
   * @throws DocumentationUnitNotExistsException if the documentation unit itself does not exist.
   */
  @Transactional(readOnly = true)
  public List<DocumentationUnitProcessStep> getProcessStepHistoryForDocumentationUnit(
      UUID documentationUnitId) throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    return documentationUnitProcessStepRepository.findAllByDocumentationUnitId(
        documentationUnit.uuid());
  }

  /**
   * Saves the chosen process step for the given documentation unit as the new current process step.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @param processStepId The ID of the process step to save.
   * @return The DocumentationUnitProcessStep representing the newly saved current process step.
   * @throws DocumentationUnitNotExistsException if the documentation unit itself does not exist.
   * @throws ProcessStepNotFoundException if the process step with the given ID does not exist.
   */
  @Transactional
  public DocumentationUnitProcessStep saveProcessStep(
      UUID documentationUnitId, UUID processStepId, UUID userId)
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    return saveProcessStep(documentationUnit, processStepId, userId);
  }

  public DocumentationUnitProcessStep saveProcessStep(
      DocumentationUnit documentationUnit, UUID processStepId, UUID userId)
      throws ProcessStepNotFoundException {
    return documentationUnitProcessStepRepository.saveProcessStep(
        documentationUnit.uuid(), processStepId, userId);
  }

  /**
   * Retrieves a list of all process steps associated with the given documentation office, ordered
   * by rank.
   *
   * @param docOfficeId The ID of the documentation office.
   * @return A list of ProcessSteps associated with the given documentation office, ordered by rank.
   * @throws DocumentationOfficeNotExistsException if the documentation office with the given ID is
   *     not found.
   */
  public List<ProcessStep> getAllProcessStepsForDocOffice(UUID docOfficeId)
      throws DocumentationOfficeNotExistsException {
    return documentationOfficeService.getProcessStepsForDocumentationOffice(docOfficeId);
  }
}
