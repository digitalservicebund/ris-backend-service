package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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
  public Flux<DocumentType> getDocumentTypes() {
    return service.getDocumentTypes();
  }

  @GetMapping(value = "courts")
  public Flux<Court> getCourts(@RequestParam Optional<String> searchStr) {
    return service.getCourts(searchStr);
  }
}
