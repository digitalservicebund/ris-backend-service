package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

@Slf4j
public class LanguageToolService extends TextCheckService {
  private final LanguageToolConfig languageToolConfig;
  private final LanguageToolClient languageToolClient;

  public LanguageToolService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      LanguageToolConfig languageToolConfig,
      LanguageToolClient languageToolClient) {

    super(documentationUnitRepository, ignoredTextCheckWordRepository);
    this.languageToolConfig = languageToolConfig;
    this.languageToolClient = languageToolClient;
  }

  @Override
  protected List<Match> requestTool(String text) {
    if (!languageToolConfig.isEnabled()) {
      log.info("LanguageTool is disabled. Skipping text check.");
      return List.of();
    }
    Document document = Jsoup.parse(text);
    JsonObject data = new JsonObject();
    data.add("annotation", getAnnotationsArray(document));

    // Use the new client to get the response
    LanguageToolResponse response = languageToolClient.checkText(data);

    var filtered =
        response.getMatches().stream()
            .filter(
                match -> {
                  String categoryId = match.getRule().getCategory().getId();
                  Map<String, List<String>> categoriesWithAllowedRules =
                      languageToolConfig.getDisabledCategoriesWithWhitelistedRules();
                  return !categoriesWithAllowedRules.containsKey(categoryId)
                      || categoriesWithAllowedRules
                          .get(categoryId)
                          .contains(match.getRule().getId());
                })
            .toList();

    return TextCheckResponseTransformer.transformToListOfDomainMatches(filtered);
  }

  @NotNull
  static JsonArray getAnnotationsArray(Document document) {
    JsonArray annotations = new JsonArray();
    NodeTraversor.traverse(new AnnotationsNodeVisitor(annotations), document.body().children());
    return annotations;
  }

  private record AnnotationsNodeVisitor(JsonArray annotations)
      implements NodeVisitor { // interpretAs is used to specify how to interpret the text of the
    // node
    static Map<String, String> interpretAs =
        Map.of("p", "\n\n", "br", "\n", "&gt;", ">", "&lt;", "<", "img", "Bild");

    /**
     * Adds the given text to the annotations. Treats characters '<' and '>' as markup and all other
     * characters as text.
     *
     * @param text the text to process
     */
    private void processTextNode(String text) {
      StringBuilder currentText = new StringBuilder();
      for (char c : text.toCharArray()) {
        if (c == '<') {
          addTextEntry(currentText.toString());
          currentText.setLength(0); // Reset
          addMarkupEntry("&lt;", "&lt;");
        } else if (c == '>') {
          addTextEntry(currentText.toString());
          currentText.setLength(0); // Reset
          addMarkupEntry("&gt;", "&gt;");
        } else {
          currentText.append(c);
        }
      }
      addTextEntry(currentText.toString());
    }

    private void addTextEntry(String text) {
      if (text.isEmpty()) return;
      JsonObject textEntry = new JsonObject();
      textEntry.add("text", new JsonPrimitive(text));
      annotations.add(textEntry);
    }

    private void addMarkupEntry(String markup, String nodeName) {
      JsonObject markupEntry = new JsonObject();
      markupEntry.add("markup", new JsonPrimitive(markup));
      if (nodeName != null && interpretAs.containsKey(nodeName)) {
        markupEntry.add("interpretAs", new JsonPrimitive(interpretAs.get(nodeName)));
      }
      annotations.add(markupEntry);
    }

    @Override
    public void head(Node node, int depth) {
      if (node instanceof TextNode textNode) {
        String processedText = textNode.getWholeText();
        if (!processedText.isEmpty()) {
          processTextNode(processedText);
        }
      } else if (!node.nodeName().startsWith("#")) {
        String openingTag = NormalizingNodeVisitor.buildOpeningTag(node);
        addMarkupEntry(openingTag, node.nodeName());
      }
    }

    @Override
    public void tail(Node node, int depth) {
      if (NormalizingNodeVisitor.shouldClose(node)) {
        addMarkupEntry(NormalizingNodeVisitor.buildClosingTag(node), null);
      }
    }
  }
}
