package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/lookuptable")
@Slf4j
public class LookupTableController {

  private final LookupTableService service;

  public LookupTableController(LookupTableService service) {
    this.service = service;
  }

  @GetMapping(value = "documentTypes")
  @PreAuthorize("isAuthenticated()")
  public Flux<DocumentType> getCaselawDocumentTypes(
      @RequestParam(value = "q") Optional<String> searchStr) {
    return service.getCaselawDocumentTypes(searchStr);
  }

  @GetMapping(value = "courts")
  @PreAuthorize("isAuthenticated()")
  public Flux<Court> getCourts(@RequestParam(value = "q") Optional<String> searchStr) {
    return service.getCourts(searchStr);
  }

  @GetMapping(value = "zitart")
  @PreAuthorize("isAuthenticated()")
  public Flux<CitationStyle> getCitationStyles(
      @RequestParam(value = "q") Optional<String> searchStr) {
    return service.getCitationStyles(searchStr);
  }
}
