package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/normabbreviation")
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
public class NormAbbreviationController {
  private final NormAbbreviationService service;

  public NormAbbreviationController(NormAbbreviationService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Flux<NormAbbreviation> getAllNormAbbreviationsStartingWithExact(
      @RequestParam(value = "q", required = false, defaultValue = "") String normAbbreviation,
      @RequestParam(value = "sz", required = false, defaultValue = "30") Integer size,
      @RequestParam(value = "pg", required = false, defaultValue = "0") Integer page) {
    return Flux.fromIterable(
        service.getNormAbbreviationsStartingWithExact(normAbbreviation, size, page));
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

  @PutMapping("/refreshMaterializedViews")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> refreshMaterializedViews() {
    service.refreshMaterializedViews();
    return Mono.just(
        ResponseEntity.ok("Refreshed the materialized view 'norm_abbreviation_search_migration'"));
  }
}
