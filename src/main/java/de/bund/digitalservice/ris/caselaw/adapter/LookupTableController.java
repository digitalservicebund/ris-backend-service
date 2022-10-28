package de.bund.digitalservice.ris.caselaw.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/lookuptable")
@Slf4j
public class LookupTableController {

  @GetMapping(value = "documentTypes")
  public Flux<String> getDocumentTypes() {
    // Flux<DocumentType> TODO
    // pass search query @param TODO
    return null;
  }
}
