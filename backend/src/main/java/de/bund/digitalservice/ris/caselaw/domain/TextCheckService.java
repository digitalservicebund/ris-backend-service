package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

@Slf4j
public class TextCheckService {
  private final DocumentationUnitService documentationUnitService;

  public TextCheckService(DocumentationUnitService documentationUnitService) {
    this.documentationUnitService = documentationUnitService;
  }

  public List<Match> check(String text) {
    return requestTool(text);
  }

  protected List<Match> requestTool(String text) {
    throw new NotImplementedException();
  }

  public List<Match> checkWholeDocumentationUnit(UUID id)
      throws DocumentationUnitNotExistsException {
    List<Match> allMatches = new ArrayList<>();

    Documentable documentable = documentationUnitService.getByUuid(id);

    if (!(documentable instanceof DocumentationUnit documentationUnit)) {
      throw new UnsupportedOperationException(
          "Check not supported for Documentable type: " + documentable.getClass());
    }

    if (documentationUnit.longTexts() != null) {
      allMatches.addAll(checkReasons(documentationUnit));
      allMatches.addAll(checkCaseFacts(documentationUnit));
      allMatches.addAll(checkDecisionReasons(documentationUnit));
      allMatches.addAll(checkTenor(documentationUnit));
      allMatches.addAll(checkHeadNote(documentationUnit));
      allMatches.addAll(checkGuidingPrinciple(documentationUnit));
    }

    return allMatches;
  }

  public TextCheckCategoryResponse checkCategory(UUID id, CategoryType category)
      throws DocumentationUnitNotExistsException {
    if (category == null) {
      throw new TextCheckUnknownCategoryException();
    }

    Documentable documentable = documentationUnitService.getByUuid(id);

    if (documentable instanceof DocumentationUnit documentationUnit) {
      return switch (category) {
        case REASONS -> checkCategoryByHTML(documentationUnit.longTexts().reasons(), category);
        case CASE_FACTS -> checkCategoryByHTML(documentationUnit.longTexts().caseFacts(), category);
        case DECISION_REASONS ->
            checkCategoryByHTML(documentationUnit.longTexts().decisionReasons(), category);
        case HEADNOTE -> checkCategoryByHTML(documentationUnit.shortTexts().headnote(), category);
        case HEADLINE -> checkCategoryByHTML(documentationUnit.shortTexts().headline(), category);
        case GUIDING_PRINCIPLE ->
            checkCategoryByHTML(documentationUnit.shortTexts().guidingPrinciple(), category);
        case TENOR -> checkCategoryByHTML(documentationUnit.longTexts().tenor(), category);
        case OTHER_LONG_TEXT ->
            checkCategoryByHTML(documentationUnit.longTexts().otherLongText(), category);
        case DISSENTING_OPINION ->
            checkCategoryByHTML(documentationUnit.longTexts().dissentingOpinion(), category);
        case OUTLINE -> checkCategoryByHTML(documentationUnit.longTexts().outline(), category);
        case UNKNOWN -> throw new TextCheckUnknownCategoryException(category.toString());
      };
    }

    return null;
  }

  private List<Match> checkGuidingPrinciple(DocumentationUnit documentationUnit) {
    return checkText(
        documentationUnit.shortTexts().guidingPrinciple(), CategoryType.GUIDING_PRINCIPLE);
  }

  private List<Match> checkTenor(DocumentationUnit documentationUnit) {
    return checkText(documentationUnit.longTexts().tenor(), CategoryType.TENOR);
  }

  private List<Match> checkHeadNote(DocumentationUnit documentationUnit) {
    return checkText(documentationUnit.shortTexts().headnote(), CategoryType.HEADNOTE);
  }

  private List<Match> checkReasons(DocumentationUnit documentationUnit) {
    return checkText(documentationUnit.longTexts().reasons(), CategoryType.REASONS);
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType) {
    if (htmlText == null) {
      return null;
    }

    Parser parser = Parser.htmlParser();
    parser.setTrackPosition(true);
    Document document = Jsoup.parse(htmlText, parser);

    JSONArray annotations = getAnnotationsArray(htmlText, document);

    JSONObject result = new JSONObject();
    result.put("annotation", annotations);

    List<Match> matches = check(result.toString());

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);

    String htmlWithReplacements = StringEscapeUtils.unescapeHtml4(htmlText);

    matches.stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .forEach(
            match -> {
              newHtmlText
                  .append(htmlWithReplacements, lastPosition.get(), match.offset())
                  .append(
                      "<text-check id=\"%s\" type=\"%s\">%s</text-check>"
                          .formatted(
                              match.id(),
                              match.rule().issueType().toLowerCase(),
                              htmlWithReplacements.substring(
                                  match.offset(), match.offset() + match.length())));
              lastPosition.set(match.offset() + match.length());
            });

    newHtmlText.append(htmlWithReplacements, lastPosition.get(), htmlWithReplacements.length());

    return new TextCheckCategoryResponse(newHtmlText.toString(), matches);
  }

  @SuppressWarnings("java:S3776")
  @NotNull
  private static JSONArray getAnnotationsArray(String htmlText, Document document) {
    JSONArray annotations = new JSONArray();
    NodeTraversor.traverse(
        new NodeVisitor() {

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
              // Start building the markup tag
              StringBuilder markupTag = new StringBuilder();
              markupTag.append("<").append(node.nodeName());

              // Add attributes if it's an Element
              if (node instanceof Element element) {
                for (Attribute attr : element.attributes()) {
                  markupTag
                      .append(" ")
                      .append(attr.getKey())
                      .append("=\"")
                      .append(attr.getValue())
                      .append("\"");
                }
              }

              markupTag.append(">");

              JSONObject markupEntry = new JSONObject();
              markupEntry.put("markup", markupTag.toString());

              // Custom logic for specific tags (optional)
              if (node.nodeName().equals("p")) {
                markupEntry.put("interpretAs", "\n\n");
              }

              annotations.add(markupEntry);
            }
          }

          @Override
          public void tail(Node node, int depth) {
            if (!(node instanceof TextNode) && !node.nodeName().startsWith("#")) {
              JSONObject markupEntry = new JSONObject();
              markupEntry.put("markup", "</" + node.nodeName() + ">");
              annotations.add(markupEntry);
            }
          }
        },
        document.body().children());
    return annotations;
  }

  private List<Match> checkCaseFacts(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts().caseFacts() == null) {
      return Collections.emptyList();
    }
    return checkText(documentationUnit.longTexts().caseFacts(), CategoryType.CASE_FACTS);
  }

  private List<Match> checkDecisionReasons(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts() == null
        || documentationUnit.longTexts().decisionReasons() == null) {
      return Collections.emptyList();
    }

    return checkText(
        documentationUnit.longTexts().decisionReasons(), CategoryType.DECISION_REASONS);
  }

  private List<Match> checkText(String text, CategoryType categoryType) {
    if (text == null) {
      return Collections.emptyList();
    }

    return checkCategoryByHTML(text, categoryType).matches().stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .toList();
  }
}
