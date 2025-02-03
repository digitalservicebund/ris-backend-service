package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextCheckService {
  private static final String NO_INDEX_ELEMENT = "noindex";

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

  public static List<TextRange> findNoIndexPositions(Document doc) {
    return findNoIndexPositions(doc, doc.text());
  }

  public static List<TextRange> findNoIndexPositions(Document doc, String plainText) {
    List<TextRange> positions = new ArrayList<>();

    Elements noIndexElements = doc.select(NO_INDEX_ELEMENT);

    for (Element noIndexElement : noIndexElements) {
      String noIndexText = noIndexElement.text();

      int start = plainText.indexOf(noIndexText);
      if (start != -1) {
        int end = start + noIndexText.length();
        positions.add(TextRange.builder().start(start).end(end).text(noIndexText).build());
      }
    }

    return positions;
  }

  public static boolean matchIsBetweenNoIndexPosition(
      Match match, List<TextRange> noIndexPositions) {
    for (TextRange noIndexPosition : noIndexPositions) {
      if (noIndexPosition.start() < match.offset()
          && noIndexPosition.end() < match.offset() + match.length()) {
        return true;
      }
    }
    return false;
  }

  public static List<Match> removeNoIndexMatches(
      List<Match> matches, Document doc, String plainText) {
    var noIndexPositions = findNoIndexPositions(doc, plainText);

    List<Match> list = new ArrayList<>();
    for (Match match : matches) {
      if (!matchIsBetweenNoIndexPosition(match, noIndexPositions)) {
        list.add(match);
      }
    }
    return list;
  }

  public List<Match> checkWholeDocumentationUnit(UUID id)
      throws DocumentationUnitNotExistsException {
    List<Match> allMatches = new ArrayList<>();

    DocumentationUnit documentationUnit = documentationUnitService.getByUuid(id);

    if (documentationUnit.longTexts() != null) {
      allMatches.addAll(checkReasons(documentationUnit));
      allMatches.addAll(checkCaseFacts(documentationUnit));
      allMatches.addAll(checkDecisionReasons(documentationUnit));
    }

    return allMatches;
  }

  private List<Match> checkReasons(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts().reasons() == null) {
      return Collections.emptyList();
    }

    Document document = Jsoup.parse(documentationUnit.longTexts().reasons());
    document
        .getElementsByTag("border-number")
        .forEach(
            element -> {
              Element newElement = element.getElementsByTag("content").first();
              element.after(newElement.html());
              element.remove();
            });

    return check(URLEncoder.encode(document.text(), StandardCharsets.UTF_8)).stream()
        .map(match -> match.toBuilder().category(CategoryType.REASONS).build())
        .toList();
  }

  private List<Match> checkCaseFacts(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts().caseFacts() == null) {
      return Collections.emptyList();
    }

    Document document = Jsoup.parse(documentationUnit.longTexts().caseFacts());
    document
        .getElementsByTag("border-number")
        .forEach(
            element -> {
              Element newElement = element.getElementsByTag("content").first();
              element.after(newElement.html());
              element.remove();
            });

    return check(URLEncoder.encode(document.text(), StandardCharsets.UTF_8)).stream()
        .map(match -> match.toBuilder().category(CategoryType.CASE_FACTS).build())
        .toList();
  }

  private List<Match> checkDecisionReasons(DocumentationUnit documentationUnit) {
    if (documentationUnit.longTexts() == null
        || documentationUnit.longTexts().decisionReasons() == null) {
      return Collections.emptyList();
    }

    Document document = Jsoup.parse(documentationUnit.longTexts().decisionReasons());
    document
        .getElementsByTag("border-number")
        .forEach(
            element -> {
              Element newElement = element.getElementsByTag("content").first();
              element.after(newElement.html());
              element.remove();
            });

    return check(URLEncoder.encode(document.text(), StandardCharsets.UTF_8)).stream()
        .map(match -> match.toBuilder().category(CategoryType.DECISION_REASON).build())
        .toList();
  }
}
