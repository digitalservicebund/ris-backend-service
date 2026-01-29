package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class RulebasedExtractionService {
  private final Map<String, ExtractionModule> moduleRegistry =
      Map.of(
          "ner", RulesetModule.fromFile("ner.json"),
          "section_markers", RulesetModule.fromFile("section_markers.json"),
          "section_builder", new SectionBuilderModule(),
          "target_path", new TargetPathModule(),
          "norms", RulesetModule.fromFile("norms.json"),
          "bgh", RulesetModule.fromFile("bgh.json"),
          "bfh", RulesetModule.fromFile("bfh.json"),
          "bverfg", RulesetModule.fromFile("bverfg.json"),
          "olg_karlsruhe", RulesetModule.fromFile("olg_karlsruhe.json"));

  private final Tokenizer tokenizer = new Tokenizer();

  public List<Extraction> extract(String html, String providedCourt) {
    // get court specific configuration
    String court = providedCourt != null ? providedCourt : getCourtFromPreflightCheck(html);
    List<String> config =
        new ArrayList<>(
            List.of("section_markers", "section_builder", "ner", "norms", "target_path"));
    if ("BFH".equals(court)) config.add("bfh");
    else if ("BGH".equals(court)) config.add("bgh");
    else if ("BVerfG".equals(court)) config.add("bverfg");
    else if ("OLG Karlsruhe".equals(court) || "LG Karlsruhe".equals(court))
      config.add("olg_karlsruhe");

    // System.out.println("Using court configuration: " + court + " -> " + config);

    return process(html, resolveModules(config));
  }

  private List<Extraction> process(String html, List<ExtractionModule> modules) {
    HtmlElement soup = JsoupParser.parse(html);
    HtmlElement firstDiv = soup.find("div", true);
    HtmlElement body = soup.find("body", true);
    HtmlElement root = firstDiv != null ? firstDiv : (body != null ? body : soup);

    ExtractionContext ctx = new ExtractionContext(root.findAll("*", false));

    // Loop over all tags and let every module visit them
    for (int i = 0; i < ctx.getChildTags().size(); i++) {
      processTag(i, ctx, modules);
    }

    // Call finalize on all modules
    modules.forEach(m -> m.finalize(ctx));

    List<Extraction> result = ctx.getExtractions();
    result.sort(
        Comparator.comparingInt(e -> e.charInterval() != null ? e.charInterval().startPos() : -1));
    return result;
  }

  private void processTag(Integer i, ExtractionContext ctx, List<ExtractionModule> modules) {
    HtmlElement tag = ctx.getChildTags().get(i);
    String text = tag.innerText();
    if (text.strip().isEmpty()) return;
    TagData td = new TagData(tag, i, text, tokenizer.tokenize(text));
    modules.forEach(m -> m.processTag(td, ctx));
  }

  private String getCourtFromPreflightCheck(String html) {
    String cleanedHtml = getHtmlWithoutImgs(html);
    String truncatedHtml =
        cleanedHtml.length() > 1000 ? cleanedHtml.substring(0, 1000) : cleanedHtml;
    List<Extraction> preflightExtractions = process(truncatedHtml, resolveModules(List.of("ner")));
    String courtRaw = Utils.getExtractionText(preflightExtractions, "court");
    return Utils.getNormalizedCourt(courtRaw);
  }

  private String getHtmlWithoutImgs(String html) {
    org.jsoup.nodes.Document doc = Jsoup.parse(html);
    doc.select("img").remove();
    return doc.outerHtml();
  }

  private List<ExtractionModule> resolveModules(List<String> keys) {
    return keys.stream().map(moduleRegistry::get).filter(Objects::nonNull).toList();
  }
}
