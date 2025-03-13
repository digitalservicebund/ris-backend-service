package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

@Slf4j
public class TextCheckService {
  private final DocumentationUnitService documentationUnitService;

  public TextCheckService(DocumentationUnitService documentationUnitService) {
    this.documentationUnitService = documentationUnitService;
  }

  public List<Match> check(String text) {
    return requestTool(text);
  }

  public List<Match> checkAsResponse(String text) {
    return check(text);
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

  private TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType) {
    if (htmlText == null) {
      return null;
    }

    Parser parser = Parser.htmlParser();
    parser.setTrackPosition(true);
    Document document = Jsoup.parse(htmlText, parser);

    Map<Integer, String> textNodes = new LinkedMap<>();
    document
        .getElementsByTag("body")
        .first()
        .childNodes()
        .forEach(child -> handleChildren(child, textNodes, 1));

    //    var textChunks = generateTextChunks(textNodes);

    List<Match> matches = new ArrayList<>();
    AtomicInteger id = new AtomicInteger(1);
    textNodes.forEach(
        (pos, text) -> {
          List<Match> textMatches = checkText(text, categoryType);
          List<Match> newTextMatches =
              textMatches.stream()
                  .map(
                      match ->
                          match.toBuilder()
                              .htmlOffset(pos + match.offset())
                              .id(id.getAndIncrement())
                              .build())
                  .toList();
          matches.addAll(newTextMatches);
        });

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);
    matches.forEach(
        match -> {
          newHtmlText.append(htmlText, lastPosition.get(), match.htmlOffset());
          newHtmlText.append("<text-check id=\"").append(match.id()).append("\">");
          newHtmlText.append(htmlText, match.htmlOffset(), match.htmlOffset() + match.length());
          newHtmlText.append("</text-check>");
          lastPosition.set(match.htmlOffset() + match.length());
        });

    newHtmlText.append(htmlText, lastPosition.get(), htmlText.length() - 1);

    return new TextCheckCategoryResponse(newHtmlText.toString(), matches);
  }

  @SuppressWarnings(" java:S1144")
  private Map<Integer, String> generateTextChunks(Map<Integer, String> textNodes) {
    Map<Integer, String> textChunks = new LinkedMap<>();

    StringBuilder builder = new StringBuilder();
    AtomicInteger startPos = new AtomicInteger(0);
    for (var entry : textNodes.entrySet()) {
      String text = entry.getValue();
      Integer pos = entry.getKey();
      if (builder.length() + text.length() > 5000) {
        textChunks.put(startPos.get(), builder.toString());
        builder = new StringBuilder();
        startPos.set(pos);
      }

      builder.append(text).append(" ").append(System.lineSeparator());
    }

    textChunks.put(startPos.get(), builder.toString());

    return textChunks;
  }

  private void handleChildren(Node child, Map<Integer, String> textNodes, int level) {

    log.info("{} node '{}' at {}", level, child.nodeName(), child.sourceRange());
    if (child instanceof TextNode textNode) {
      log.info("  text node content: '{}'", textNode.text());
      textNodes.put(child.sourceRange().startPos(), textNode.text());
    } else {
      child.childNodes().forEach(grandChild -> handleChildren(grandChild, textNodes, level + 1));
    }
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

    return check(URLEncoder.encode(text, StandardCharsets.UTF_8)).stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .toList();
  }
}
