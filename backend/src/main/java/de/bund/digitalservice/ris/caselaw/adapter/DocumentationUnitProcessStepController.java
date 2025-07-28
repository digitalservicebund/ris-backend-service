package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
   * Adds a new process step link for a documentation unit POST
   * /api/v1/caselaw/processsteps/{documentationUnitId}/new
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @param processStepId The request body containing the ID of the new process step.
   * @return ResponseEntity with DocumentationUnitProcessStep representing the newly saved (current)
   *     step, and HTTP Status 201 Created. Returns 404 Not Found if the documentation unit does not
   *     exist.
   */
  @PostMapping("/{documentationUnitId}/new")
  @PreAuthorize("isAuthenticated() and @userHasWriteAccess.apply(#documentationUnitId)")
  public ResponseEntity<DocumentationUnitProcessStep> saveProcessStep(
      @PathVariable UUID documentationUnitId, @RequestBody UUID processStepId) {
    try {
      DocumentationUnitProcessStep newCurrentStep =
          processStepService.saveProcessStep(documentationUnitId, processStepId);
      return new ResponseEntity<>(newCurrentStep, HttpStatus.CREATED);
    } catch (DocumentationUnitNotExistsException | ProcessStepNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the current (most recent) process step for a given documentation unit. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/current
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with DocumentationUnitProcessStep if found (200 OK), or 404 Not Found if
   *     the documentation unit or the current process step is not found.
   */
  @GetMapping("/{documentationUnitId}/current")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DocumentationUnitProcessStep> getCurrentProcessStep(
      @PathVariable UUID documentationUnitId) {
    try {
      return ResponseEntity.ok(processStepService.getCurrentProcessStep(documentationUnitId));
    } catch (DocumentationUnitNotExistsException | ProcessStepNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the next logical process step for a given documentation unit, based on its current
   * step and the defined process flow for its office. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/next
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with ProcessStep if a next step is defined (200 OK), or 204 No Content
   *     if no next step is defined for the current state. Returns 404 Not Found if the
   *     documentation unit does not exist.
   */
  @GetMapping("/{documentationUnitId}/next")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProcessStep> getNextProcessStep(@PathVariable UUID documentationUnitId) {
    try {
      Optional<ProcessStep> nextStepOptional =
          processStepService.getNextProcessStep(documentationUnitId);
      return nextStepOptional
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.noContent().build());
    } catch (DocumentationUnitNotExistsException | ProcessStepNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the last (previous) process step for a given documentation unit. This is the step
   * that was active immediately before the current one. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/last
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with DocumentationUnitProcessStep if found (200 OK), or 204 No Content
   *     if no last process step found (i.e., less than two steps exist). Returns 404 Not Found if
   *     the documentation unit does not exist.
   */
  @GetMapping("/{documentationUnitId}/last")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DocumentationUnitProcessStep> getLastProcessStep(
      @PathVariable UUID documentationUnitId) {
    try {
      Optional<DocumentationUnitProcessStep> lastStepOptional =
          processStepService.getLastProcessStep(documentationUnitId);
      return lastStepOptional
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.noContent().build());
    } catch (DocumentationUnitNotExistsException | ProcessStepNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves the complete history of process steps for a given documentation unit, ordered by the
   * time each step was created, latest first. GET
   * /api/v1/caselaw/processsteps/{documentationUnitId}/history
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with a list of DocumentationUnitProcessStep objects representing the
   *     history (200 OK), potentially an empty list if no history. Returns 404 Not Found if the
   *     documentation unit was not found.
   */
  @GetMapping("/{documentationUnitId}/history")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<DocumentationUnitProcessStep>>
      getProcessStepHistoryForDocumentationUnit(@PathVariable UUID documentationUnitId) {
    try {
      List<DocumentationUnitProcessStep> history =
          processStepService.getProcessStepHistoryForDocumentationUnit(documentationUnitId);
      return ResponseEntity.ok(history);
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves a list of all possible process steps for a given documentation office, ordered by
   * their defined rank. GET /api/v1/caselaw/processsteps/{docOfficeId}/all
   *
   * @param docOfficeId The ID of the documentation office.
   * @return ResponseEntity with a list of ProcessStep objects (200 OK), potentially an empty list
   *     if the office exists but has no configured steps. Returns 404 Not Found if the
   *     documentation office is not found, or if an associated process step is not found (data
   *     inconsistency).
   */
  @GetMapping("/{docOfficeId}/all")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ProcessStep>> getAllPossibleProcessStepsForDocOffice(
      @PathVariable UUID docOfficeId) {
    try {
      List<ProcessStep> possibleSteps =
          processStepService.getAllProcessStepsForDocOffice(docOfficeId);
      return ResponseEntity.ok(possibleSteps);
    } catch (DocumentationOfficeNotExistsException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
