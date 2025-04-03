package de.bund.digitalservice.ris.caselaw.domain;

import static java.util.Arrays.stream;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

@Slf4j
public class TextCheckService {
  private final DocumentationUnitRepository documentationUnitRepository;
  private final IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;

  public TextCheckService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.ignoredTextCheckWordRepository = ignoredTextCheckWordRepository;
  }

  public List<Match> check(String text) {
    return requestTool(text).stream().map(this::addIgnoreInformationToMatch).toList();
  }

  private Match addIgnoreInformationToMatch(Match match) {
    List<IgnoredTextCheckWord> ignoredTextCheckWordDTOs =
        ignoredTextCheckWordRepository.findByWord(match.word());

    if (!ignoredTextCheckWordDTOs.isEmpty()) {
      List<IgnoredTextCheckWord> ignoredTextCheckWords =
          ignoredTextCheckWordDTOs.stream().distinct().toList();
      return match.toBuilder().ignoredTextCheckWords(ignoredTextCheckWords).build();
    }

    return match;
  }

  protected List<Match> requestTool(String text) {
    throw new NotImplementedException();
  }

  public List<Match> checkWholeDocumentationUnit(UUID id)
      throws DocumentationUnitNotExistsException {
    List<Match> allMatches = new ArrayList<>();

    Documentable documentable = documentationUnitRepository.findByUuid(id);

    if (documentable instanceof DocumentationUnit documentationUnit) {
      allMatches.addAll(checkText(documentationUnit.longTexts().reasons(), CategoryType.REASONS));
      allMatches.addAll(
          checkText(documentationUnit.longTexts().caseFacts(), CategoryType.CASE_FACTS));
      allMatches.addAll(
          checkText(
              documentationUnit.longTexts().decisionReasons(), CategoryType.DECISION_REASONS));
      allMatches.addAll(checkText(documentationUnit.longTexts().tenor(), CategoryType.TENOR));
      allMatches.addAll(
          checkText(documentationUnit.shortTexts().headnote(), CategoryType.HEADNOTE));
      allMatches.addAll(
          checkText(
              documentationUnit.shortTexts().guidingPrinciple(), CategoryType.GUIDING_PRINCIPLE));
    } else {
      throw new UnsupportedOperationException();
    }
    return allMatches;
  }

  public TextCheckCategoryResponse checkCategory(UUID id, CategoryType category)
      throws DocumentationUnitNotExistsException {
    if (category == null) {
      throw new TextCheckUnknownCategoryException();
    }

    Documentable documentable = documentationUnitRepository.findByUuid(id);

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

  @NotNull
  static String normalizeHTML(Document document) {
    StringBuilder builder = new StringBuilder();
    NodeTraversor.traverse(new NormalizingNodeVisitor(builder), document.body().children());
    return builder.toString();
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType) {
    if (htmlText == null) {
      return null;
    }

    // normalize HTML to assure correct positioning
    String normalizedHtml = normalizeHTML(Jsoup.parse(htmlText));

    List<Match> matches = check(normalizedHtml);

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);

    List<Match> modifiedMatches =
        matches.stream()
            .map(match -> match.toBuilder().category(categoryType).build())
            .map(
                match -> {
                  if (match.replacements() != null)
                    return match.toBuilder()
                        .replacements(
                            match
                                .replacements()
                                .subList(0, Math.min(match.replacements().size(), 5)))
                        .build();
                  return match;
                })
            .toList();

    modifiedMatches.forEach(
        match -> {
          newHtmlText
              .append(normalizedHtml, lastPosition.get(), match.offset())
              .append(
                  "<text-check id=\"%s\" type=\"%s\">%s</text-check>"
                      .formatted(
                          match.id(),
                          match.ignoredTextCheckWords() == null
                                  || match.ignoredTextCheckWords().isEmpty()
                              ? match.rule().issueType().toLowerCase()
                              : "ignored",
                          normalizedHtml.substring(
                              match.offset(), match.offset() + match.length())));
          lastPosition.set(match.offset() + match.length());
        });

    newHtmlText.append(normalizedHtml, lastPosition.get(), normalizedHtml.length());

    return new TextCheckCategoryResponse(newHtmlText.toString(), modifiedMatches);
  }

  private List<Match> checkText(String text, CategoryType categoryType) {
    if (text == null) {
      return Collections.emptyList();
    }

    return checkCategoryByHTML(text, categoryType).matches().stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .toList();
  }

  public DocumentationUnit addNoIndexTags(DocumentationUnit documentationUnit) {
    String normalizedHtml = normalizeHTML(Jsoup.parse(documentationUnit.longTexts().tenor()));

    List<Match> matches = check(normalizedHtml);

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);

    matches.stream()
        .filter(
            match ->
                match.ignoredTextCheckWords().stream()
                    .anyMatch(
                        ignoredTextCheckWord ->
                            ignoredTextCheckWord.type() == IgnoredTextCheckType.DOCUMENTATION_UNIT))
        .forEach(
            match -> {
              newHtmlText
                  .append(normalizedHtml, lastPosition.get(), match.offset())
                  .append("<noindex>")
                  .append(normalizedHtml, match.offset(), match.offset() + match.length())
                  .append("</noindex>");
              lastPosition.set(match.offset() + match.length());
            });
    newHtmlText.append(normalizedHtml, lastPosition.get(), normalizedHtml.length());

    LongTexts newLongTexts =
        documentationUnit.longTexts().toBuilder().tenor(newHtmlText.toString()).build();

    return documentationUnit.toBuilder().longTexts(newLongTexts).build();
  }

  public void removeIgnoredWord(UUID documentationUnitId, String word) {
    ignoredTextCheckWordRepository.deleteAllByWordAndDocumentationUnitId(word, documentationUnitId);
  }

  public IgnoredTextCheckWord addIgnoreWord(UUID documentationUnitId, String word) {
    List<IgnoredTextCheckWord> ignoredTextCheckWords =
        ignoredTextCheckWordRepository.findByWord(word);
    return ignoredTextCheckWordRepository.addIgnoredTextCheckWord(word, documentationUnitId);
  }

  @SuppressWarnings("java:S3776")
  protected record NormalizingNodeVisitor(StringBuilder builder) implements NodeVisitor {

    @Override
    public void head(Node node, int depth) {
      if (node instanceof TextNode textNode) {
        // Use getWholeText() to capture non-breaking spaces
        String processedText = textNode.getWholeText();
        processedText = processedText.replace("<", "&lt;");
        processedText = processedText.replace(">", "&gt;");

        if (!processedText.isEmpty()) {
          builder.append(processedText);
        }
        // Ignore comments and other non-element nodes
      } else if (!node.nodeName().startsWith("#")) {
        builder.append(buildOpeningTag(node));
      }
    }

    @Override
    public void tail(Node node, int depth) {
      if (shouldClose(node)) {
        builder.append(buildClosingTag(node));
      }
    }

    @NotNull
    public static String buildOpeningTag(Node node) {
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
              .append(attr.getValue().replace("\"", "&quot;"))
              .append("\"");
        }
      }
      markupTag.append(">");
      return markupTag.toString();
    }

    public static boolean shouldClose(Node node) {
      return !node.nodeName().startsWith("#")
          && !(node instanceof TextNode)
          && stream(new String[] {"col", "img", "br", "hr"})
              .noneMatch(node.nodeName()::equals); // self-closing tags do not need to be closed;
    }

    @NotNull
    public static String buildClosingTag(Node node) {
      return "</" + node.nodeName() + ">";
    }
  }
}
