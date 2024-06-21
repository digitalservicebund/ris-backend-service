package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/normabbreviation")
public class NormAbbreviationController {
  private final NormAbbreviationService service;

  public NormAbbreviationController(NormAbbreviationService service) {
    this.service = service;
  }

  @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public NormAbbreviation getNormAbbreviationById(@PathVariable("uuid") UUID uuid) {
    return service.getNormAbbreviationById(uuid);
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<NormAbbreviation> getAllNormAbbreviationsContaining(
      @RequestParam(value = "q", required = false, defaultValue = "") String query,
      @RequestParam(value = "sz", required = false, defaultValue = "30") Integer size,
      @RequestParam(value = "pg", required = false, defaultValue = "0") Integer page) {
    return service.findAllNormAbbreviationsContaining(query, size, page);
  }
}
