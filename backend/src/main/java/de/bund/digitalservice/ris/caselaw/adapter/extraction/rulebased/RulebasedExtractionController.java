package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import de.bund.digitalservice.ris.caselaw.domain.extraction.rulebased.ExtractionMatch;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/extract")
public class RulebasedExtractionController {
  private RulebasedExtractionService extractionService;

  public RulebasedExtractionController(RulebasedExtractionService extractionService) {
    this.extractionService = extractionService;
  }

  @PostMapping()
  //  @PreAuthorize("@userIsInternal.apply(#oidcUser)")
  public ResponseEntity<List<ExtractionMatch>> extract(String html) {
    List<ExtractionMatch> matches = extractionService.extract(html);

    return ResponseEntity.ok(matches);
  }
}
