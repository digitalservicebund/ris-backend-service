package de.bund.digitalservice.ris.caselaw.domain;

import static java.util.Arrays.stream;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
  private final FeatureToggleService featureToggleService;

  public TextCheckService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.ignoredTextCheckWordRepository = ignoredTextCheckWordRepository;
    this.featureToggleService = featureToggleService;
  }

  public List<Match> check(String text) {
    return requestTool(text);
  }

  protected List<Match> requestTool(String text) {
    throw new NotImplementedException();
  }

  public List<Match> checkWholeDocumentationUnit(UUID id)
      throws DocumentationUnitNotExistsException {

    Documentable documentable = documentationUnitRepository.findByUuid(id);

    if (documentable instanceof DocumentationUnit documentationUnit) {
      List<Match> allMatches = new ArrayList<>();

      for (CategoryType type : CategoryType.values()) {
        try {
          TextCheckCategoryResponse response = checkCategory(documentationUnit, type);
          if (response == null) {
            continue;
          }
          allMatches.addAll(response.matches());
        } catch (Exception e) {
          log.error("Could not process category", e);
        }
      }
      return allMatches;

    } else {
      throw new UnsupportedOperationException(
          "Check not supported for Documentable type: " + documentable.getClass());
    }
  }

  public TextCheckCategoryResponse checkCategory(UUID id, CategoryType categoryType)
      throws DocumentationUnitNotExistsException {
    Documentable documentable = documentationUnitRepository.findByUuid(id);

    if (documentable instanceof DocumentationUnit documentationUnit) {
      return checkCategory(documentationUnit, categoryType);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private TextCheckCategoryResponse checkCategory(
      DocumentationUnit documentationUnit, CategoryType category) {
    if (category == null) {
      throw new TextCheckUnknownCategoryException();
    }

    return switch (category) {
      case REASONS ->
          checkCategoryByHTML(
              documentationUnit.longTexts().reasons(), category, documentationUnit.uuid());
      case CASE_FACTS ->
          checkCategoryByHTML(
              documentationUnit.longTexts().caseFacts(), category, documentationUnit.uuid());
      case DECISION_REASONS ->
          checkCategoryByHTML(
              documentationUnit.longTexts().decisionReasons(), category, documentationUnit.uuid());
      case HEADNOTE ->
          checkCategoryByHTML(
              documentationUnit.shortTexts().headnote(), category, documentationUnit.uuid());
      case OTHER_HEADNOTE ->
          checkCategoryByHTML(
              documentationUnit.shortTexts().otherHeadnote(), category, documentationUnit.uuid());
      case HEADLINE ->
          checkCategoryByHTML(
              documentationUnit.shortTexts().headline(), category, documentationUnit.uuid());
      case GUIDING_PRINCIPLE ->
          checkCategoryByHTML(
              documentationUnit.shortTexts().guidingPrinciple(),
              category,
              documentationUnit.uuid());
      case TENOR ->
          checkCategoryByHTML(
              documentationUnit.longTexts().tenor(), category, documentationUnit.uuid());
      case OTHER_LONG_TEXT ->
          checkCategoryByHTML(
              documentationUnit.longTexts().otherLongText(), category, documentationUnit.uuid());
      case DISSENTING_OPINION ->
          checkCategoryByHTML(
              documentationUnit.longTexts().dissentingOpinion(),
              category,
              documentationUnit.uuid());
      case OUTLINE ->
          checkCategoryByHTML(
              documentationUnit.longTexts().outline(), category, documentationUnit.uuid());
    };
  }

  @NotNull
  static String normalizeHTML(Document document) {
    StringBuilder builder = new StringBuilder();
    NodeTraversor.traverse(new NormalizingNodeVisitor(builder), document.body().children());
    return builder.toString();
  }

  /**
   * Method to retrieve no index words and exports the ignore list for jDV publication
   *
   * @param documentationUnit without noindex tags
   * @return object with noindex tags on long and short texts
   */
  public DocumentationUnit addNoIndexTagsForHandOver(DocumentationUnit documentationUnit) {
    if (!featureToggleService.isEnabled("neuris.text-check-noindex-handover")) {
      return documentationUnit;
    }

    List<String> ignoredTextCheckWords =
        ignoredTextCheckWordRepository
            .findAllByDocumentationUnitId(documentationUnit.uuid())
            .stream()
            .map(IgnoredTextCheckWord::word)
            .toList();

    if (ignoredTextCheckWords.isEmpty()) {
      return documentationUnit;
    }

    if (documentationUnit.longTexts() != null) {
      documentationUnit =
          documentationUnit.toBuilder()
              .longTexts(updateLongTexts(documentationUnit.longTexts(), ignoredTextCheckWords))
              .build();
    }

    if (documentationUnit.shortTexts() != null) {
      documentationUnit =
          documentationUnit.toBuilder()
              .shortTexts(updateShortTexts(documentationUnit.shortTexts(), ignoredTextCheckWords))
              .build();
    }

    return documentationUnit;
  }

  private LongTexts updateLongTexts(LongTexts texts, List<String> ignoredWords) {
    return texts.toBuilder()
        .reasons(addNoIndexTags(texts.reasons(), ignoredWords))
        .caseFacts(addNoIndexTags(texts.caseFacts(), ignoredWords))
        .decisionReasons(addNoIndexTags(texts.decisionReasons(), ignoredWords))
        .tenor(addNoIndexTags(texts.tenor(), ignoredWords))
        .otherLongText(addNoIndexTags(texts.otherLongText(), ignoredWords))
        .dissentingOpinion(addNoIndexTags(texts.dissentingOpinion(), ignoredWords))
        .outline(addNoIndexTags(texts.outline(), ignoredWords))
        .build();
  }

  private ShortTexts updateShortTexts(ShortTexts texts, List<String> ignoredWords) {
    return texts.toBuilder()
        .headnote(addNoIndexTags(texts.headnote(), ignoredWords))
        .otherHeadnote(addNoIndexTags(texts.otherHeadnote(), ignoredWords))
        .headline(addNoIndexTags(texts.headline(), ignoredWords))
        .guidingPrinciple(addNoIndexTags(texts.guidingPrinciple(), ignoredWords))
        .build();
  }

  /**
   * Wraps ignored words with <noindex></noindex> for handover service
   *
   * @param htmlText to add no index tags to
   * @param ignoredWords on Documentation Level or global level
   */
  public static String addNoIndexTags(String htmlText, List<String> ignoredWords) {
    if (htmlText == null || ignoredWords == null || ignoredWords.isEmpty()) {
      return htmlText;
    }

    Document document = Jsoup.parse(htmlText);
    document.outputSettings().prettyPrint(false);

    for (String ignoredWord : ignoredWords) {
      NodeTraversor.traverse(new NoIndexNodeWrapperVisitor(htmlText, ignoredWord), document.body());
    }

    var htmlWithNoIndexTags = document.body().html();
    htmlWithNoIndexTags = htmlWithNoIndexTags.replace("&lt;noindex&gt;", "<noindex>");
    htmlWithNoIndexTags = htmlWithNoIndexTags.replace("&lt;/noindex&gt;", "</noindex>");

    return htmlWithNoIndexTags;
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType) {
    return checkCategoryByHTML(htmlText, categoryType, null);
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType, UUID documentationUnitId) {
    if (htmlText == null) {
      return null;
    }

    // normalize HTML to assure correct positioning
    String normalizedHtml = normalizeHTML(Jsoup.parse(htmlText));

    List<Match> matches = check(normalizedHtml);

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);

    List<Match> modifiedMatches =
        addIgnoredTextChecksIndividually(
            documentationUnitId,
            matches.stream()
                .map(match -> match.toBuilder().category(categoryType).build())
                .map(this::limitReplacements)
                .toList());

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

  private Match limitReplacements(Match match) {
    if (match.replacements() != null) {
      return match.toBuilder()
          .replacements(match.replacements().subList(0, Math.min(match.replacements().size(), 5)))
          .build();
    }
    return match;
  }

  public void removeIgnoredWord(UUID documentationUnitId, String word) {
    ignoredTextCheckWordRepository.deleteWordIgnoredInDocumentationUnitWithId(
        word, documentationUnitId);
  }

  public IgnoredTextCheckWord addIgnoreWord(UUID documentationUnitId, String word) {
    return ignoredTextCheckWordRepository.addWord(word, documentationUnitId);
  }

  public boolean removeIgnoredWord(String word) {
    return ignoredTextCheckWordRepository.deleteWordGlobally(word);
  }

  public IgnoredTextCheckWord addIgnoreWord(String word, DocumentationOffice documentationOffice) {
    IgnoredTextCheckWord globallyIgnoreWord =
        ignoredTextCheckWordRepository.getGloballyIgnoreWord(word);
    if (globallyIgnoreWord != null) {
      return globallyIgnoreWord;
    }
    return ignoredTextCheckWordRepository.addWord(word, documentationOffice);
  }

  public List<Match> addIgnoredTextChecksIndividually(
      UUID documentationUnitId, List<Match> matches) {
    if (documentationUnitId == null) {
      return matches;
    }

    var words = matches.stream().map(Match::word).toList();

    List<IgnoredTextCheckWord> globalAndDocumentationUnitIgnoredWords =
        ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            words, documentationUnitId);

    Map<String, List<IgnoredTextCheckWord>> groupedByWord =
        globalAndDocumentationUnitIgnoredWords.stream()
            .collect(Collectors.groupingBy(IgnoredTextCheckWord::word));

    return matches.stream()
        .map(
            match -> {
              List<IgnoredTextCheckWord> ignoredWords =
                  groupedByWord.getOrDefault(match.word(), null);
              return match.toBuilder().ignoredTextCheckWords(ignoredWords).build();
            })
        .toList();
  }

  /**
   * See test method for covered cases {@code
   * TextCheckServiceTest#testAddNoIndexTags_withMultipleCases()}
   */
  @SuppressWarnings("java:S3776")
  protected record NoIndexNodeWrapperVisitor(String html, String ignoredWord)
      implements NodeVisitor {

    @Override
    public void head(@NotNull Node node, int i) {
      if (node instanceof TextNode textNode) {
        String text = textNode.getWholeText();

        Pattern exactWordsMatchPattern =
            Pattern.compile(
                "(?<![\\p{L}\\p{N}])" + Pattern.quote(ignoredWord) + "(?![\\p{L}\\p{N}])",
                Pattern.UNICODE_CHARACTER_CLASS);

        Matcher matcher = exactWordsMatchPattern.matcher(text);

        StringBuilder newTextBuffer = new StringBuilder();
        while (matcher.find()) {
          matcher.appendReplacement(newTextBuffer, "<noindex>" + matcher.group() + "</noindex>");
        }
        matcher.appendTail(newTextBuffer);

        textNode.text(newTextBuffer.toString());
      }
    }
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
