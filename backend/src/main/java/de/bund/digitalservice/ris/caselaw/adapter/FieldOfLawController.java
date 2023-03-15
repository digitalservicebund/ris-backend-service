package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/fieldsoflaw")
public class FieldOfLawController {
  private final FieldOfLawService service;

  public FieldOfLawController(FieldOfLawService service) {
    this.service = service;
  }

  @GetMapping
  public Mono<Page<FieldOfLaw>> getFieldsOfLawBySearchQuery(
      @RequestParam("q") Optional<String> searchStr,
      @RequestParam("pg") int page,
      @RequestParam("sz") int size) {
    return service.getFieldsOfLawBySearchQuery(searchStr, PageRequest.of(page, size));
  }

  @GetMapping(value = "{subjectFieldNumber}/children")
  public Flux<FieldOfLaw> getChildrenOfFieldOfLaw(@PathVariable String subjectFieldNumber) {
    return service.getChildrenOfFieldOfLaw(subjectFieldNumber);
  }

  @GetMapping(value = "{subjectFieldNumber}/tree")
  public Mono<FieldOfLaw> getTreeForFieldOfLaw(@PathVariable String subjectFieldNumber) {
    return service.getTreeForFieldOfLaw(subjectFieldNumber);
  }
}
