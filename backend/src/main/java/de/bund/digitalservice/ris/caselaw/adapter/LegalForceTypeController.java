package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LegalForceTypeService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/legalforcetype")
@Slf4j
public class LegalForceTypeController {
  private final LegalForceTypeService service;

  public LegalForceTypeController(LegalForceTypeService service) {
    this.service = service;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<LegalForceType>> getLegalForceTypes(
      @RequestParam(value = "q", required = false) String searchStr) {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(service.getLegalForceTypes(searchStr));
  }
}
