package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

// import de.bund.digitalservice.ris.caselaw.domain.extraction.rulebased.ExtractionMatch;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

record ExtractionRequest(String html, String court) {}

record ExtractionResponse(List<Extraction> extractions) {}

@RestController
@RequestMapping("api/v1/caselaw/extract")
public class RulebasedExtractionController {
  private RulebasedExtractionService extractionService;

  public RulebasedExtractionController(RulebasedExtractionService extractionService) {
    this.extractionService = extractionService;
  }

  @PostMapping()
  // @PreAuthorize("@userIsInternal.apply(#oidcUser)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ExtractionResponse> extract(@RequestBody ExtractionRequest request) {
    List<Extraction> extractions = extractionService.extract(request.html(), request.court());
    ExtractionResponse data = new ExtractionResponse(extractions);
    return ResponseEntity.ok(data);
  }
}
