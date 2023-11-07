package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.CitationTypeService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/citationtypes")
@Slf4j
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
public class CitationTypeController {
  private final CitationTypeService service;

  public CitationTypeController(CitationTypeService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Flux<CitationType> getCitationTypes(
      @RequestParam(value = "q") Optional<String> searchStr) {
    return Flux.fromIterable(service.getCitationStyles(searchStr));
  }
}
