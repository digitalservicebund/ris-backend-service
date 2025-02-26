package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
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

    DocumentationUnit documentationUnit = documentationUnitService.getByUuid(id);

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

  @SuppressWarnings("unused")
  public List<Match> checkCategory(UUID id, CategoryType category)
      throws DocumentationUnitNotExistsException {
    if (category == null) {
      return Collections.emptyList();
    }

    DocumentationUnit documentationUnit = documentationUnitService.getByUuid(id);

    return switch (category) {
      case REASONS -> checkCategoryByHTML(documentationUnit.longTexts().reasons(), category);
      case CASE_FACTS -> checkCategoryByHTML(documentationUnit.longTexts().caseFacts(), category);
      case DECISION_REASON ->
          checkCategoryByHTML(documentationUnit.longTexts().decisionReasons(), category);
      case HEADNOTE -> checkCategoryByHTML(documentationUnit.shortTexts().headnote(), category);
      case GUIDING_PRINCIPLE ->
          checkCategoryByHTML(documentationUnit.shortTexts().guidingPrinciple(), category);
      case TENOR -> checkCategoryByHTML(documentationUnit.longTexts().tenor(), category);
      case UNKNOWN -> Collections.emptyList();
    };
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

  private List<Match> checkCategoryByHTML(String htmlText, CategoryType categoryType) {
    if (htmlText == null) {
      return Collections.emptyList();
    }

    Parser parser = Parser.htmlParser();
    parser.setTrackPosition(true);
    Document document = Jsoup.parse(htmlText, parser);

    Map<Integer, String> textNodes = new LinkedMap<>();
    AtomicInteger offset = new AtomicInteger(0);
    document
        .getElementsByTag("body")
        .first()
        .childNodes()
        .forEach(child -> handleChildren(child, textNodes, offset, 1));

    var textChunks = generateTextChunks(textNodes);

    List<Match> matches = new ArrayList<>();
    textChunks.forEach(
        (pos, text) -> {
          List<Match> textMatches = checkText(text, categoryType);
          List<Match> newTextMatches =
              textMatches.stream()
                  .map(match -> match.toBuilder().offset(match.offset() + pos).build())
                  .toList();
          matches.addAll(newTextMatches);
        });

    return matches;
  }

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

  private void handleChildren(
      Node child, Map<Integer, String> textNodes, AtomicInteger offset, int level) {

    log.info("{} node '{}' at {}", level, child.nodeName(), child.sourceRange());
    if (child instanceof TextNode textNode) {
      log.info("  text node content: '{}'", textNode.text());
      textNodes.put(offset.getAndAdd(textNode.text().length() + 2), textNode.text());
    } else {
      child
          .childNodes()
          .forEach(grandChild -> handleChildren(grandChild, textNodes, offset, level + 1));
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

    return checkText(documentationUnit.longTexts().decisionReasons(), CategoryType.DECISION_REASON);
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
