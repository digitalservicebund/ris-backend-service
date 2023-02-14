package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/lookuptable")
@Slf4j
public class LookupTableController {

  private final LookupTableService service;

  public LookupTableController(LookupTableService service) {
    this.service = service;
  }

  @GetMapping(value = "documentTypes")
  public Flux<DocumentType> getCaselawDocumentTypes(@RequestParam Optional<String> searchStr) {
    return service.getCaselawDocumentTypes(searchStr);
  }

  @GetMapping(value = "courts")
  public Flux<Court> getCourts(@RequestParam Optional<String> searchStr) {
    return service.getCourts(searchStr);
  }

  // TODO replace q variable with searchStr and but use q as parameter name
  @GetMapping(value = "subjectFields")
  public Flux<SubjectField> getSubjectFields(@RequestParam Optional<String> q) {
    return service.getSubjectFields(q);
  }

  @GetMapping(value = "subjectFieldChildren/{subjectFieldNumber}")
  public Flux<SubjectField> getSubjectFieldChildren(@PathVariable String subjectFieldNumber) {
    return service.getSubjectFieldChildren(subjectFieldNumber);
  }

  @GetMapping(value = "subjectFields/{subjectFieldNumber}/tree")
  public Mono<SubjectField> getTreeForSubjectField(@PathVariable String subjectFieldNumber) {
    return service.getTreeForSubjectFieldNumber(subjectFieldNumber);
  }
}
