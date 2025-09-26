package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessStepService {

  private final DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final ProcessStepRepository processStepRepository;
  private final DocumentationOfficeService documentationOfficeService;

  public ProcessStepService(
      DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository,
      DocumentationUnitRepository documentationUnitRepository,
      ProcessStepRepository processStepRepository,
      DocumentationOfficeService documentationOfficeService) {
    this.documentationUnitProcessStepRepository = documentationUnitProcessStepRepository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.processStepRepository = processStepRepository;
    this.documentationOfficeService = documentationOfficeService;
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
  public Optional<ProcessStep> getNextProcessStepForDocOffice(UUID documentationUnitId)
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
              if (currentIndex != -1
                  && orderedOfficeProcessSteps.size() > currentIndex + 1
                  && orderedOfficeProcessSteps.get(currentIndex + 1) != null) {
                return Optional.of(orderedOfficeProcessSteps.get(currentIndex + 1));
              } else {
                return Optional.empty(); // No next step
              }
            });
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

  public Optional<ProcessStep> getProcessStepForName(String name) {
    return processStepRepository.findByName(name);
  }
}
