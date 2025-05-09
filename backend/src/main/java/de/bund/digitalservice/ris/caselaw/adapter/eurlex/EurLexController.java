package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.domain.SearchService;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/eurlex")
public class EurLexController {
  private final SearchService service;

  public EurLexController(SearchService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Page<SearchResult>> getSearchResults(
      @RequestParam(value = "page", required = false) String page,
      @RequestParam(value = "file-number") Optional<String> fileNumber,
      @RequestParam(value = "celex") Optional<String> celex,
      @RequestParam(value = "court") Optional<String> court,
      @RequestParam(value = "start-date") Optional<LocalDate> startDate,
      @RequestParam(value = "end-date") Optional<LocalDate> endDate) {
    return ResponseEntity.ok(
        service.getSearchResults(page, fileNumber, celex, court, startDate, endDate));
  }
}
