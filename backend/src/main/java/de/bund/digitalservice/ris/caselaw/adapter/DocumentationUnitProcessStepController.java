package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepMissingException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/processsteps")
@Slf4j
public class DocumentationUnitProcessStepController {

  private final DocumentationUnitProcessStepService processStepService;

  public DocumentationUnitProcessStepController(
      DocumentationUnitProcessStepService processStepService) {
    this.processStepService = processStepService;
  }

  /**
   * Advances the process step for a documentation unit. Marks the current step as completed and
   * starts the new specified step. POST /api/v1/caselaw/processsteps/{documentationUnitId}/new
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @param processStepId The request body containing the ID of the new process step.
   * @return ResponseEntity with ProcessStepDto representing the newly saved (current) step, and
   *     HTTP Status 201 Created.
   */
  @PostMapping("/{documentationUnitId}/new")
  public ResponseEntity<DocumentationUnitProcessStep> saveProcessStep(
      @PathVariable UUID documentationUnitId, @RequestBody UUID processStepId) {
    try {
      DocumentationUnitProcessStep newCurrentStep =
          processStepService.saveProcessStep(documentationUnitId, processStepId);
      return new ResponseEntity<>(newCurrentStep, HttpStatus.CREATED);
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the current (most recent) process step for a given documentation unit. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/current
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with ProcessStep if found.
   */
  @GetMapping("/{documentationUnitId}/current")
  public ResponseEntity<DocumentationUnitProcessStep> getCurrentProcessStep(
      @PathVariable UUID documentationUnitId) {
    try {
      DocumentationUnitProcessStep currentStep =
          processStepService.getCurrentProcessStep(documentationUnitId);
      return ResponseEntity.ok(currentStep);
    } catch (DocumentationUnitNotExistsException | ProcessStepMissingException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the last (previous) process step for a given documentation unit. This is the step
   * that was active immediately before the current one. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/last
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with ProcessStepDto if found, or 404 Not Found.
   */
  @GetMapping("/{documentationUnitId}/last")
  public ResponseEntity<DocumentationUnitProcessStep> getLastProcessStep(
      @PathVariable UUID documentationUnitId) {
    try {
      DocumentationUnitProcessStep lastStep =
          processStepService.getLastProcessStep(documentationUnitId);
      return ResponseEntity.ok(lastStep);
    } catch (DocumentationUnitNotExistsException | EntityNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the complete history of process steps for a given documentation unit, ordered by the
   * time each step was started. GET /api/v1/caselaw/processsteps/{documentationUnitId}/history
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with a list of DocumentationUnitProcessStep objects representing the
   *     history. Returns 404 if the documentation unit is not found.
   */
  @GetMapping("/{documentationUnitId}/history")
  public ResponseEntity<List<DocumentationUnitProcessStep>>
      getProcessStepHistoryForDocumentationUnit(@PathVariable UUID documentationUnitId) {
    try {
      List<DocumentationUnitProcessStep> history =
          processStepService.getProcessStepHistoryForDocumentationUnit(documentationUnitId);
      return ResponseEntity.ok(history);
    } catch (DocumentationUnitNotExistsException | EntityNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the next logical process step for a given documentation unit, based on its current
   * step and the defined process flow for its office. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/next
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with ProcessStepDto if a next step is defined, or 404 Not Found.
   */
  @GetMapping("/{documentationUnitId}/next")
  public ResponseEntity<ProcessStep> getNextProcessStep(@PathVariable UUID documentationUnitId) {
    try {
      return ResponseEntity.ok(processStepService.getNextProcessStep(documentationUnitId));
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves a list of all possible process steps for a given documentation office, ordered by
   * their defined rank. GET /api/v1/caselaw/processsteps/{docOfficeId}/all
   *
   * @param docOfficeId The ID of the documentation office.
   * @return ResponseEntity with a list of ProcessStep objects. Returns 404 if the documentation
   *     office is not found.
   */
  @GetMapping("/{docOfficeId}/all")
  public ResponseEntity<List<ProcessStep>> getAllPossibleProcessStepsForDocOffice(
      @PathVariable UUID docOfficeId) {
    List<ProcessStep> possibleSteps =
        processStepService.getAllProcessStepsForDocOffice(docOfficeId);
    return ResponseEntity.ok(possibleSteps);
  }
}
