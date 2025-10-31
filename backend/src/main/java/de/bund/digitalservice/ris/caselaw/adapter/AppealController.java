package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/appeal")
@Slf4j
public class AppealController {

  private final AppealService appealService;

  public AppealController(AppealService appealService) {
    this.appealService = appealService;
  }

  /**
   * Retrieves a list of possible options for appellants (Rechtsmittelführer) GET
   * /api/v1/caselaw/appeal/appellants
   *
   * @return a list of all possible appellant options
   */
  @GetMapping("/appellants")
  @PreAuthorize("isAuthenticated()")
  public List<Appellant> getAppellants() {
    return appealService.getAppellantOptions();
  }

  /**
   * Retrieves a list of possible statuses for various appeal dropdown options GET
   * /api/v1/caselaw/appeal/statuses
   *
   * @return a list of all possible appeal status options
   */
  @GetMapping("/statuses")
  @PreAuthorize("isAuthenticated()")
  public List<AppealStatus> getStatuses() {
    return appealService.getAppealStatusOptions();
  }
}
