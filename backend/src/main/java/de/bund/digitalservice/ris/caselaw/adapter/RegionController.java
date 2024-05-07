package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.RegionService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/region")
@Slf4j
public class RegionController {
  private final RegionService service;

  public RegionController(RegionService service) {
    this.service = service;
  }

  @GetMapping("/applicable")
  @PreAuthorize("isAuthenticated()")
  public Flux<Region> getApplicableRegions(
      @RequestParam(value = "q", required = false) String searchStr) {
    return Flux.fromIterable(service.getApplicableRegions(searchStr));
  }
}
