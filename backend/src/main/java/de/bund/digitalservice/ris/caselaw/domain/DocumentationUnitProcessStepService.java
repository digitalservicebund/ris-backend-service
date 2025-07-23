package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentationUnitProcessStepService {

  private final DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final ProcessStepDocumentationOfficeRepository processStepDocumentationOfficeRepository;

  public DocumentationUnitProcessStepService(
      DocumentationUnitProcessStepRepository documentationUnitProcessStepRepository,
      DocumentationUnitRepository documentationUnitRepository,
      ProcessStepDocumentationOfficeRepository processStepDocumentationOfficeRepository) {
    this.documentationUnitProcessStepRepository = documentationUnitProcessStepRepository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.processStepDocumentationOfficeRepository = processStepDocumentationOfficeRepository;
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

    return documentationUnitProcessStepRepository.findTopByDocumentationUnitIdOrderByCreatedAtDesc(
        documentationUnit.uuid());
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
  public List<DocumentationUnitProcessStep> getProcessStepHistoryForDocumentationUnit(
      UUID documentationUnitId) throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    return documentationUnitProcessStepRepository.findByDocumentationUnitOrderByCreatedAtDesc(
        documentationUnit.uuid());
  }

  /**
   * Determines the next logical process step for a documentation unit based on its current step and
   * the process flow defined for its documentation office.
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return A ProcessStepDto representing the next step, or null if no next step is defined. found,
   *     or if the documentation unit is not associated with an office.
   */
  @Transactional(readOnly = true)
  public ProcessStep getNextProcessStep(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    // Todo: Rename here to findCurrentProcessStepForDocumentationUnit
    DocumentationUnitProcessStep currentProcessStep =
        documentationUnitProcessStepRepository.findTopByDocumentationUnitIdOrderByCreatedAtDesc(
            documentationUnit.uuid());
    UUID docOfficeId = documentationUnit.coreData().documentationOffice().id();

    return processStepDocumentationOfficeRepository
        .findNextProcessStepForDocumentationOffice(currentProcessStep, docOfficeId)
        .orElse(null);
  }

  @Transactional
  public DocumentationUnitProcessStep saveProcessStep(UUID documentationUnitId, UUID processStepId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);

    return documentationUnitProcessStepRepository.saveProcessStep(
        documentationUnit.uuid(), processStepId);
  }

  public List<ProcessStep> getAllProcessStepsForDocOffice(UUID docOfficeId) {
    return processStepDocumentationOfficeRepository.findAllProcessStepsForDocOffice(docOfficeId);
  }
}
