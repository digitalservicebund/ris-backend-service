package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/normabbreviation")
public class NormAbbreviationController {
  private final NormAbbreviationService service;

  public NormAbbreviationController(NormAbbreviationService service) {
    this.service = service;
  }

  @GetMapping("/{uuid}")
  @PreAuthorize("isAuthenticated()")
  public Mono<NormAbbreviation> getNormAbbreviationById(@PathVariable("uuid") UUID uuid) {
    return Mono.just(service.getNormAbbreviationById(uuid));
  }

  @GetMapping("/search")
  @PreAuthorize("isAuthenticated()")
  public Mono<List<NormAbbreviation>> getAllNormAbbreviationsContaining(
      @RequestParam(value = "q", required = false, defaultValue = "") String query,
      @RequestParam(value = "sz", required = false, defaultValue = "30") Integer size,
      @RequestParam(value = "pg", required = false, defaultValue = "0") Integer page) {
    return Mono.just(service.findAllNormAbbreviationsContaining(query, size, page));
  }
}
