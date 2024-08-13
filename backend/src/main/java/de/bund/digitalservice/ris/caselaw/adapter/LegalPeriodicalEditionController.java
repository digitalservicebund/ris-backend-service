package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
}
