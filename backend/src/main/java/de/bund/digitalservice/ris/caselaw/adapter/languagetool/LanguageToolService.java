package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
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

    JSONArray annotations = getAnnotationsArray(text, document);

    JSONObject data = new JSONObject();
    data.put("annotation", annotations);

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
  static JSONArray getAnnotationsArray(String htmlText, Document document) {
    JSONArray annotations = new JSONArray();
    NodeTraversor.traverse(
        new AnnotationsNodeVisitor(annotations, htmlText), document.body().children());
    return annotations;
  }

  @SuppressWarnings("java:S3776")
  private record AnnotationsNodeVisitor(JSONArray annotations, String htmlText)
      implements NodeVisitor {

    @Override
    public void head(Node node, int depth) {

      if (node instanceof TextNode textNode) {
        // Use getWholeText() to capture non-breaking spaces
        String processedText = textNode.getWholeText();

        if (!processedText.isEmpty()) {
          JSONObject textEntry = new JSONObject();
          textEntry.put("text", processedText);
          annotations.add(textEntry);
        }
        // Ignore comments and other non-element nodes
      } else if (!node.nodeName().startsWith("#") && htmlText.contains(node.nodeName())) {
        JSONObject markupEntry = new JSONObject();
        markupEntry.put("markup", NormalizingNodeVisitor.buildOpeningTag(node));

        // Custom logic for specific tags (optional)
        if (node.nodeName().equals("p")) {
          markupEntry.put("interpretAs", "\n\n");
        }
        if (node.nodeName().equals("br")) {
          markupEntry.put("interpretAs", "\n");
        }

        annotations.add(markupEntry);
      }
    }

    @Override
    public void tail(Node node, int depth) {
      if (NormalizingNodeVisitor.shouldClose(node)) {
        JSONObject markupEntry = new JSONObject();
        markupEntry.put("markup", NormalizingNodeVisitor.buildClosingTag(node));
        annotations.add(markupEntry);
      }
    }
  }
}
