package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/pendingproceeding")
@Slf4j
public class PendingProceedingController {
  private final DocumentationUnitService service;
  private final DuplicateCheckService duplicateCheckService;

  public PendingProceedingController(
      DocumentationUnitService service, DuplicateCheckService duplicateCheckService) {
    this.service = service;
    this.duplicateCheckService = duplicateCheckService;
  }

  private void checkDuplicates(String documentNumber) {
    try {
      duplicateCheckService.checkDuplicates(documentNumber);
    } catch (Exception e) {
      // Error in duplicate check should not affect program flow, logging in service
    }
  }

  @GetMapping(value = "/{documentNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public ResponseEntity<PendingProceeding> getByDocumentNumber(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable String documentNumber) {

    if (documentNumber.length() != 13 && documentNumber.length() != 14) {
      throw new DocumentationUnitException("Die Dokumentennummer unterstützt nur 13-14 Zeichen");
    }

    try {
      checkDuplicates(documentNumber);
      Documentable documentable = service.getByDocumentNumberWithUser(documentNumber, oidcUser);
      if (documentable instanceof PendingProceeding pendingProceeding) {
        return ResponseEntity.ok(pendingProceeding);
      }
      return ResponseEntity.ok(PendingProceeding.builder().build());
    } catch (DocumentationUnitNotExistsException e) {
      log.error("Documentation unit '{}' doesn't exist", documentNumber);
      return ResponseEntity.ok(PendingProceeding.builder().build());
    }
  }
}
