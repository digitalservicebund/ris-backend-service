package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/legalperiodicaledition")
@Slf4j
public class LegalPeriodicalEditionController {
  private final LegalPeriodicalEditionService service;

  public LegalPeriodicalEditionController(LegalPeriodicalEditionService service) {
    this.service = service;
  }

  @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<LegalPeriodicalEdition> getById(@NonNull @PathVariable UUID uuid) {
    try {
      return ResponseEntity.ok(service.getById(uuid).orElseThrow());
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<LegalPeriodicalEdition> getLegalPeriodicals(
      @RequestParam(value = "legal_periodical_id") UUID legalPeriodicalId) {
    return service.getLegalPeriodicalEditions(legalPeriodicalId);
  }

  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public LegalPeriodicalEdition save(
      @Valid @RequestBody LegalPeriodicalEdition legalPeriodicalEdition) {
    return service.saveLegalPeriodicalEdition(legalPeriodicalEdition);
  }

  @DeleteMapping(value = "/{editionId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> delete(@NonNull @PathVariable UUID editionId) {
    var deleted = service.delete(editionId);
    if (deleted) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.noContent().build();
  }
}
