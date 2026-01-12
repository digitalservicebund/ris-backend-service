package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import de.bund.digitalservice.ris.caselaw.domain.extraction.rulebased.ExtractionMatch;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class RulebasedExtractionService {
  private PreProcessService service;

  public RulebasedExtractionService(PreProcessService service) {
    this.service = service;
  }

  public List<ExtractionMatch> extract(String html) {
    Document doc = Jsoup.parse(html);
    doc.body().children().forEach(child -> {});
    return List.of();
  }
}
