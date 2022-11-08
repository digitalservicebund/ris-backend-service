package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
  // pass search query @param TODO
  public Flux<DocumentType> getDocumentTypes() {
    return service.getDocumentTypes();
  }
}
