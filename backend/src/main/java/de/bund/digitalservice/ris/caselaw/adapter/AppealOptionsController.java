package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/appealoptions")
@Slf4j
public class AppealOptionsController {

  private final AppealOptionsService appealOptionsService;

  public AppealOptionsController(AppealOptionsService appealOptionsService) {
    this.appealOptionsService = appealOptionsService;
  }

  /**
   * Retrieves a list of possible options for appellants (Rechtsmittelführer) GET
   * /api/v1/caselaw/appeal/appellants
   *
   * @return a list of all possible appellant options
   */
  @GetMapping("/appellants")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<Appellant>> getAppellants() {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(appealOptionsService.getAppellantOptions());
  }

  /**
   * Retrieves a list of possible statuses for various appeal dropdown options GET
   * /api/v1/caselaw/appeal/statuses
   *
   * @return a list of all possible appeal status options
   */
  @GetMapping("/statuses")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<AppealStatus>> getStatuses() {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(appealOptionsService.getAppealStatusOptions());
  }
}
