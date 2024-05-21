package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/fieldsoflaw")
public class FieldOfLawController {
  private final FieldOfLawService service;

  public FieldOfLawController(FieldOfLawService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Slice<FieldOfLaw> getFieldsOfLawBySearchQuery(
      @RequestParam("q") Optional<String> searchStr,
      @RequestParam("pg") int page,
      @RequestParam("sz") int size) {
    return service.getFieldsOfLawBySearchQuery(searchStr, PageRequest.of(page, size));
  }

  @GetMapping(value = "/search-by-identifier")
  @PreAuthorize("isAuthenticated()")
  public List<FieldOfLaw> getFieldsOfLawByIdentifierSearch(
      @RequestParam("q") Optional<String> searchStr) {
    return service.getFieldsOfLawByIdentifierSearch(searchStr);
  }

  @GetMapping(value = "{identifier}/children")
  @PreAuthorize("isAuthenticated()")
  public List<FieldOfLaw> getChildrenOfFieldOfLaw(@PathVariable String identifier) {
    return service.getChildrenOfFieldOfLaw(identifier);
  }

  @GetMapping(value = "{identifier}/tree")
  @PreAuthorize("isAuthenticated()")
  public FieldOfLaw getTreeForFieldOfLaw(@PathVariable String identifier) {
    return service.getTreeForFieldOfLaw(identifier);
  }
}
