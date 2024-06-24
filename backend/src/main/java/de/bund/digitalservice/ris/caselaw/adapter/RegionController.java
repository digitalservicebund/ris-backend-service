package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.RegionService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller class responsible for handling HTTP requests related to regions. */
@RestController
@RequestMapping("api/v1/caselaw/region")
@Slf4j
public class RegionController {
  private final RegionService service;

  public RegionController(RegionService service) {
    this.service = service;
  }

  /**
   * Retrieves a flux of applicable regions based on the provided search string. The 'applicability'
   * value is true, if the region is a "Geltungsbereich" for "Gesetzeskraft einer Norm".
   *
   * @param searchStr The search string used to filter applicable regions (optional).
   * @return a list of regions, where applicability is true (with the given filter applied, if
   *     given).
   */
  @GetMapping(value = "/applicable", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<Region> getApplicableRegions(
      @RequestParam(value = "q", required = false) String searchStr) {
    return service.getApplicableRegions(searchStr);
  }
}
