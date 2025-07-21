package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepMissingException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   * time each step was started. GET /api/v1/caselaw/processsteps/unit/{documentationUnitId}/history
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with a list of DocumentationUnitProcessStep objects representing the
   *     history. Returns 404 if the documentation unit is not found.
   */
  @GetMapping("/unit/{documentationUnitId}/history")
  public ResponseEntity<List<DocumentationUnitProcessStep>> getProcessHistoryForDocumentationUnit(
      @PathVariable UUID documentationUnitId) {
    try {
      List<DocumentationUnitProcessStep> history =
          processStepService.getAllProcessSteps(documentationUnitId);
      return ResponseEntity.ok(history);
    } catch (DocumentationUnitNotExistsException | EntityNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }
}
