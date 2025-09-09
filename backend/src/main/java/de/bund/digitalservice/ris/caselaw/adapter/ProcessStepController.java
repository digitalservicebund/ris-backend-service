package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw")
@Slf4j
public class ProcessStepController {

  private final ProcessStepService processStepService;
  private final UserService userService;

  public ProcessStepController(ProcessStepService processStepService, UserService userService) {
    this.processStepService = processStepService;
    this.userService = userService;
  }

  /**
   * Retrieves the next logical process step for a given documentation unit, based on its current
   * step and the defined process flow for its office. GET
   * /api/v1/caselaw/documentationUnits/{documentationUnitId}/processsteps/next
   *
   * @param documentationUnitId The ID of the documentation unit.
   * @return ResponseEntity with ProcessStep if a next step is defined (200 OK), or 204 No Content
   *     if no next step is defined for the current state. Returns 404 Not Found if the
   *     documentation unit does not exist.
   */
  @GetMapping("/documentationUnits/{documentationUnitId}/processsteps/next")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProcessStep> getNextProcessStepForDocOffice(
      @PathVariable UUID documentationUnitId) {
    try {
      Optional<ProcessStep> nextStepOptional =
          processStepService.getNextProcessStepForDocOffice(documentationUnitId);
      return nextStepOptional
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.noContent().build());
    } catch (DocumentationUnitNotExistsException | ProcessStepNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves a list of all possible process steps for the user's documentation office, ordered by
   * their defined rank. GET /api/v1/caselaw/processsteps
   *
   * @param oidcUser the logged-in user, to retrieve the docoffice from
   * @return ResponseEntity with a list of ProcessStep objects (200 OK), potentially an empty list
   *     if the office exists but has no configured steps. Returns 404 Not Found if the
   *     documentation office is not found, or if an associated process step is not found (data
   *     inconsistency).
   */
  @GetMapping("/processsteps")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ProcessStep>> getAllPossibleProcessStepsForDocOffice(
      @AuthenticationPrincipal OidcUser oidcUser) {
    try {
      var documentationOffice = userService.getDocumentationOffice(oidcUser);
      if (documentationOffice.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      List<ProcessStep> possibleSteps =
          processStepService.getAssignableProcessStepsForDocOffice(documentationOffice.get().id());
      return ResponseEntity.ok(possibleSteps);
    } catch (DocumentationOfficeNotExistsException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
