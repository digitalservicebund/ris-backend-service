package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class LanguageToolService extends TextCheckService {
  private final LanguageToolConfig languageToolConfig;

  public LanguageToolService(
      LanguageToolConfig languageToolConfig,
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService) {

    super(documentationUnitRepository, ignoredTextCheckWordRepository, featureToggleService);
    this.languageToolConfig = languageToolConfig;
  }

  @Override
  protected List<Match> requestTool(String text) {
    if (!languageToolConfig.isEnabled()) {
      log.info("LanguageTool is disabled. Skipping text check.");
      return List.of();
    }
    Document document = Jsoup.parse(text);

    JsonArray annotations = getAnnotationsArray(document);

    JsonObject data = new JsonObject();
    data.add("annotation", annotations);

    RestTemplate restTemplate = new RestTemplate();

    // Prepare the form data
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("data", data.toString());
    formData.add("language", languageToolConfig.getLanguage());
    formData.add("mode", "all");
    formData.add("disabledRules", languageToolConfig.getDisabledRules());
    formData.add("disabledCategories", languageToolConfig.getDisabledCategories());

    // Set headers (optional but good practice)
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    // Create the HTTP request
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

    ResponseEntity<LanguageToolResponse> response =
        restTemplate.postForEntity(
            languageToolConfig.getUrl(), request, LanguageToolResponse.class);

    return TextCheckResponseTransformer.transformToListOfDomainMatches(
        Objects.requireNonNull(response.getBody()));
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
