package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.util.Collections;
import java.util.List;
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
      LanguageToolConfig languageToolConfig, DocumentationUnitService documentationUnitService) {

    super(documentationUnitService);
    this.languageToolConfig = languageToolConfig;
  }

  @Override
  protected List<Match> requestTool(String text) {
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
    formData.add("disabledRules", "WHITESPACE_RULE");

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

  private record AnnotationsNodeVisitor(JsonArray annotations) implements NodeVisitor {
    @Override
    public void head(Node node, int depth) {
      proceedText(node);
      proceedElements(node);
    }

    private void proceedElements(Node node) {
      if (node.nodeName().startsWith("#")) {
        return;
      }

      JsonObject markupEntry = new JsonObject();
      markupEntry.add("markup", new JsonPrimitive(NormalizingNodeVisitor.buildOpeningTag(node)));

      proceedParagraph(node, markupEntry);
      proceedBreaks(node, markupEntry);

      annotations.add(markupEntry);
    }

    private void proceedBreaks(Node node, JsonObject markupEntry) {
      if (node.nodeName().startsWith("#") || !node.nodeName().equals("br")) {
        return;
      }

      markupEntry.add("interpretAs", new JsonPrimitive("\n"));
    }

    private void proceedParagraph(Node node, JsonObject markupEntry) {
      if (node.nodeName().startsWith("#") || !node.nodeName().equals("p")) {
        return;
      }

      markupEntry.add("interpretAs", new JsonPrimitive("\n\n"));
    }

    private void proceedText(Node node) {
      if (node instanceof TextNode textNode) {
        // Use getWholeText() to capture non-breaking spaces
        String processedText = textNode.getWholeText();

        if (!processedText.isEmpty()) {
          JsonObject textEntry = new JsonObject();
          textEntry.add("text", new JsonPrimitive(processedText));
          annotations.add(textEntry);
        }
      }
    }

    @Override
    public void tail(Node node, int depth) {
      if (!NormalizingNodeVisitor.shouldClose(node)) {
        return;
      }

      JsonObject markupEntry = new JsonObject();
      markupEntry.add("markup", new JsonPrimitive(NormalizingNodeVisitor.buildClosingTag(node)));
      annotations.add(markupEntry);
    }
  }
}
