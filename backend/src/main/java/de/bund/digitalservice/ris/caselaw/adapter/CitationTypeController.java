package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CitationTypeService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/citationtypes")
@Slf4j
public class CitationTypeController {
  private final CitationTypeService service;

  public CitationTypeController(CitationTypeService service) {
    this.service = service;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<CitationType>> getCitationTypes(
      @RequestParam(value = "q") Optional<String> searchStr) {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(service.getCitationStyles(searchStr));
  }
}
