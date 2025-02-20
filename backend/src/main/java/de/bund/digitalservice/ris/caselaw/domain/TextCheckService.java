package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Context;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

@Slf4j
public class TextCheckService {
  private static final String BORDER_NUMBER = "border-number";
  private static final String CONTENT = "content";
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
    DocumentationUnit documentationUnit = documentationUnitService.getByUuid(id);

    if (Objects.requireNonNull(category) == CategoryType.DECISION_REASON) {
      return checkDecisionReasonsByHTML(documentationUnit);
    }

    return Collections.emptyList();
  }

  private List<Match> checkGuidingPrinciple(DocumentationUnit documentationUnit) {
    return checkText(documentationUnit.longTexts().tenor(), CategoryType.GUIDING_PRINCIPLE);
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

  public List<Match> checkDecisionReasonsByHTML(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts().decisionReasons() == null) {
      return Collections.emptyList();
    }

    Parser parser = Parser.htmlParser();
    parser.setTrackPosition(true);
    Document document = Jsoup.parse(documentationUnit.longTexts().decisionReasons(), parser);
    StringBuilder stringBuilder = new StringBuilder();
    Map<Integer, Integer> borderNumberOffset = new LinkedMap<>();
    AtomicInteger lastOffset = new AtomicInteger(0);
    document
        .getElementsByTag("body")
        .first()
        .childNodes()
        .forEach(child -> handleChildren(child, stringBuilder, borderNumberOffset, lastOffset, 1));

    String cleanText = cleanUpSpaces(stringBuilder.toString());

    return check(URLEncoder.encode(cleanText, StandardCharsets.UTF_8)).stream()
        .map(match -> calculateRightPosition(match, borderNumberOffset))
        .toList();
  }

  private Match calculateRightPosition(Match match, Map<Integer, Integer> borderNumberOffset) {
    AtomicInteger lastOffset = new AtomicInteger(0);
    borderNumberOffset.forEach(
        (position, offset) -> {
          if (position <= match.offset()) {
            lastOffset.set(offset);
          }
        });

    Context oldContext = match.context();
    return match.toBuilder()
        .offset(lastOffset.get() + match.offset())
        .context(
            new Context(
                oldContext.text(), lastOffset.get() + oldContext.offset(), oldContext.length()))
        .build();
  }

  private String cleanUpSpaces(String dirtyText) {
    final AtomicBoolean spaceFound = new AtomicBoolean(false);
    StringBuilder cleanText = new StringBuilder();
    dirtyText
        .chars()
        .forEach(
            ch -> {
              if (spaceFound.get()) {
                if (ch == ' ') {
                  return;
                } else {
                  spaceFound.set(false);
                }
              } else {
                if (ch == ' ') {
                  spaceFound.set(true);
                }
              }
              cleanText.append((char) ch);
            });

    return cleanText.toString().trim();
  }

  private void handleChildren(
      Node child,
      StringBuilder stringBuilder,
      Map<Integer, Integer> borderNumberOffset,
      AtomicInteger lastOffset,
      int level) {
    if (child.nodeName().equals(BORDER_NUMBER)) {
      handleBorderNumber(child, stringBuilder, borderNumberOffset, lastOffset, level + 1);
    } else {
      log.info("{} node '{}' at {}", level, child.nodeName(), child.sourceRange());
      if (child instanceof TextNode textNode) {
        log.info("  text node content: '{}'", textNode.text());
        stringBuilder.append(textNode.text());
      } else {
        child
            .childNodes()
            .forEach(
                grandChild ->
                    handleChildren(
                        grandChild, stringBuilder, borderNumberOffset, lastOffset, level + 1));
      }
    }
  }

  private void handleBorderNumber(
      Node borderNumber,
      StringBuilder stringBuilder,
      Map<Integer, Integer> borderNumberOffset,
      AtomicInteger lastOffset,
      int level) {
    log.info("{} border number at {}", level, borderNumber.sourceRange());

    Element borderNumberElement = (Element) borderNumber;
    Element numberElement = borderNumberElement.getElementsByTag("number").first();
    Element contentElement = borderNumberElement.getElementsByTag(CONTENT).first();

    int position = stringBuilder.length();
    lastOffset.set(lastOffset.addAndGet(numberElement.text().length() + 6));
    borderNumberOffset.put(position, lastOffset.get());

    contentElement
        .children()
        .forEach(
            child ->
                handleChildren(child, stringBuilder, borderNumberOffset, lastOffset, level + 1));
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
    Document document = Jsoup.parse(text);
    document
        .getElementsByTag(BORDER_NUMBER)
        .forEach(
            element -> {
              Element newElement = element.getElementsByTag(CONTENT).first();
              element.after(newElement.html());
              element.remove();
            });

    return check(URLEncoder.encode(document.text(), StandardCharsets.UTF_8)).stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .toList();
  }
}
