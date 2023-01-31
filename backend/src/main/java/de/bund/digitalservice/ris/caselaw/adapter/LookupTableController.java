package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  public Flux<DocumentType> getCaselawDocumentTypes(@RequestParam Optional<String> searchStr) {
    return service.getCaselawDocumentTypes(searchStr);
  }

  @GetMapping(value = "courts")
  public Flux<Court> getCourts(@RequestParam Optional<String> searchStr) {
    return service.getCourts(searchStr);
  }

  @GetMapping(value = "subjectFields")
  public Flux<SubjectField> getSubjectFields(@RequestParam Optional<String> q) {
    return service.getSubjectFields(q);
  }

  @GetMapping(value = "subjectFieldChildren")
  public Flux<SubjectField> getSubjectFieldChildren(
      @RequestParam(required = false) Long subjectFieldId) { // TODO required vs Optional
    return service.getSubjectFieldChildren(subjectFieldId);
  }

  @GetMapping(value = "subjectFieldKeywords/{subjectFieldId}")
  public Flux<Keyword> getSubjectFieldKeywords(@PathVariable long subjectFieldId) {
    return service.getSubjectFieldKeywords(subjectFieldId);
  }

  @GetMapping(value = "subjectFieldNorms/{subjectFieldId}")
  public Flux<Norm> getSubjectFieldNorms(@PathVariable long subjectFieldId) {
    return service.getSubjectFieldNorms(subjectFieldId);
  }
}
