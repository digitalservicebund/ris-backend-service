package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/normabbreviation")
public class NormAbbreviationController {
  private final NormAbbreviationService service;

  public NormAbbreviationController(NormAbbreviationService service) {
    this.service = service;
  }

  @GetMapping
  public Flux<NormAbbreviation> getAllNormAbbreviationsBySearchQuery(
      @RequestParam(value = "q", required = false, defaultValue = "") String query,
      @RequestParam(value = "sz", required = false) Integer size,
      @RequestParam(value = "pg", required = false) Integer page) {

    return service.getNormAbbreviationBySearchQuery(query, size, page);
  }

  @GetMapping("/{uuid}")
  public Mono<NormAbbreviation> getNormAbbreviationController(@PathVariable("uuid") UUID uuid) {
    return service.getNormAbbreviationById(uuid);
  }

  @GetMapping("/search")
  public Flux<NormAbbreviation> getAllNormAbbreviationsByAwesomeSearchQuery(
      @RequestParam(value = "q", required = false, defaultValue = "") String query,
      @RequestParam(value = "sz", required = false) Integer size,
      @RequestParam(value = "pg", required = false) Integer page) {

    return service.getNormAbbreviationByAwesomeSearchQuery(query, size, page);
  }
}
