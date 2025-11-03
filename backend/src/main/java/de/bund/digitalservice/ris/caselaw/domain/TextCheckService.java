package de.bund.digitalservice.ris.caselaw.domain;

import static java.util.Arrays.stream;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckNotAllowedCategoryTypeException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

/**
 * Service for text check.
 *
 * <p>Checks the input of the API call and return a list of matches. Runs the check against the
 * external text check tool. Handle the global and local ignored words.
 */
@Slf4j
public class TextCheckService {
  private final DocumentationUnitRepository documentationUnitRepository;
  private final IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;

  //  private final FeatureToggleService featureToggleService;

  public TextCheckService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.ignoredTextCheckWordRepository = ignoredTextCheckWordRepository;
    //    this.featureToggleService = featureToggleService;
  }

  protected List<Match> check(String text) {
    return requestTool(text);
  }

  protected List<Match> requestTool(String text) {
    throw new NotImplementedException();
  }

  /**
   * Check all relevant categories of a whole documentation unit (decision or pending proceeding).
   *
   * @param id UUID of the documentation unit
   * @return the list of matches
   * @throws DocumentationUnitNotExistsException if no documentation unit exist for the given UUID
   */
  public List<Match> checkWholeDocumentationUnit(UUID id)
      throws DocumentationUnitNotExistsException {

    DocumentationUnit documentationUnit = documentationUnitRepository.findByUuid(id);

    if (!(documentationUnit instanceof Decision)
        && !(documentationUnit instanceof PendingProceeding)) {
      throw new UnsupportedOperationException(
          "Check not supported for Documentable type: " + documentationUnit.getClass());
    }

    List<Match> allMatches = Collections.synchronizedList(new ArrayList<>());

    stream(CategoryType.forDocumentationUnitType(documentationUnit.getClass()))
        .parallel()
        .forEach(
            categoryType -> {
              try {
                TextCheckCategoryResponse response = checkCategory(documentationUnit, categoryType);
                if (response != null) {
                  allMatches.addAll(response.matches());
                }
              } catch (Exception e) {
                log.error(
                    "Could not process text category: {} for doc unit id: {}", categoryType, id, e);
              }
            });

    return allMatches;
  }

  /**
   * Check one category in a documentation unit.
   *
   * @param id UUID of the documentation unit
   * @param categoryType category type to check
   * @return a response object with the adapted html text (text check information) and a list of
   *     matches
   * @throws DocumentationUnitNotExistsException if no documentation unit exist for the given UUID
   */
  public TextCheckCategoryResponse checkCategory(UUID id, CategoryType categoryType)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit = documentationUnitRepository.findByUuid(id);

    return checkCategory(documentationUnit, categoryType);
  }

  private TextCheckCategoryResponse checkCategory(
      DocumentationUnit documentationUnit, CategoryType categoryType) {
    if (documentationUnit instanceof Decision decision) {
      return checkCategory(decision, categoryType);
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      return checkCategory(pendingProceeding, categoryType);
    }

    throw new UnsupportedOperationException(
        "Check not supported for Documentable type: " + documentationUnit.getClass());
  }

  private TextCheckCategoryResponse checkCategory(Decision decision, CategoryType category) {
    if (category == null) {
      throw new TextCheckUnknownCategoryException();
    }

    return switch (category) {
      case REASONS ->
          checkCategoryByHTML(decision.longTexts().reasons(), category, decision.uuid());
      case CASE_FACTS ->
          checkCategoryByHTML(decision.longTexts().caseFacts(), category, decision.uuid());
      case DECISION_REASONS ->
          checkCategoryByHTML(decision.longTexts().decisionReasons(), category, decision.uuid());
      case HEADNOTE ->
          checkCategoryByHTML(decision.shortTexts().headnote(), category, decision.uuid());
      case OTHER_HEADNOTE ->
          checkCategoryByHTML(decision.shortTexts().otherHeadnote(), category, decision.uuid());
      case HEADLINE ->
          checkCategoryByHTML(decision.shortTexts().headline(), category, decision.uuid());
      case GUIDING_PRINCIPLE ->
          checkCategoryByHTML(decision.shortTexts().guidingPrinciple(), category, decision.uuid());
      case TENOR -> checkCategoryByHTML(decision.longTexts().tenor(), category, decision.uuid());
      case OTHER_LONG_TEXT ->
          checkCategoryByHTML(decision.longTexts().otherLongText(), category, decision.uuid());
      case DISSENTING_OPINION ->
          checkCategoryByHTML(decision.longTexts().dissentingOpinion(), category, decision.uuid());
      case OUTLINE ->
          checkCategoryByHTML(decision.longTexts().outline(), category, decision.uuid());
      default -> throw new TextCheckNotAllowedCategoryTypeException(category, Decision.class);
    };
  }

  private TextCheckCategoryResponse checkCategory(
      PendingProceeding pendingProceeding, CategoryType category) {
    if (category == null) {
      throw new TextCheckUnknownCategoryException();
    }

    return switch (category) {
      case HEADLINE ->
          checkCategoryByHTML(
              pendingProceeding.shortTexts().headline(), category, pendingProceeding.uuid());
      case LEGAL_ISSUE ->
          checkCategoryByHTML(
              pendingProceeding.shortTexts().legalIssue(), category, pendingProceeding.uuid());
      case RESOLUTION_NOTE ->
          checkCategoryByHTML(
              pendingProceeding.shortTexts().resolutionNote(), category, pendingProceeding.uuid());
      default ->
          throw new TextCheckNotAllowedCategoryTypeException(category, PendingProceeding.class);
    };
  }

  @NotNull
  static String normalizeHTML(Document document) {
    StringBuilder builder = new StringBuilder();
    NodeTraversor.traverse(new NormalizingNodeVisitor(builder), document.body().children());
    return builder.toString();
  }

  //  /**
  //   * Method to retrieve no index words and exports the ignore list for jDV publication
  //   *
  //   * @param decision without noindex tags
  //   * @return object with noindex tags on long and short texts
  //   */
  //  public Decision addNoIndexTagsForHandOver(Decision decision) {
  //    if (!featureToggleService.isEnabled("neuris.text-check-noindex-handover")) {
  //      return decision;
  //    }
  //
  //    List<String> ignoredTextCheckWords =
  //        ignoredTextCheckWordRepository.findAllByDocumentationUnitId(decision.uuid()).stream()
  //            .map(IgnoredTextCheckWord::word)
  //            .sorted(
  //                (s1, s2) ->
  //                    Integer.compare(s2.length(), s1.length())) // sort by length (long words
  // first)
  //            .toList();
  //
  //    if (ignoredTextCheckWords.isEmpty()) {
  //      return decision;
  //    }
  //
  //    if (decision.longTexts() != null) {
  //      decision =
  //          decision.toBuilder()
  //              .longTexts(updateLongTexts(decision.longTexts(), ignoredTextCheckWords))
  //              .build();
  //    }
  //
  //    if (decision.shortTexts() != null) {
  //      decision =
  //          decision.toBuilder()
  //              .shortTexts(updateShortTexts(decision.shortTexts(), ignoredTextCheckWords))
  //              .build();
  //    }
  //
  //    return decision;
  //  }
  //
  //  private LongTexts updateLongTexts(LongTexts texts, List<String> ignoredWords) {
  //    return texts.toBuilder()
  //        .reasons(addNoIndexTags(texts.reasons(), ignoredWords))
  //        .caseFacts(addNoIndexTags(texts.caseFacts(), ignoredWords))
  //        .decisionReasons(addNoIndexTags(texts.decisionReasons(), ignoredWords))
  //        .tenor(addNoIndexTags(texts.tenor(), ignoredWords))
  //        .otherLongText(addNoIndexTags(texts.otherLongText(), ignoredWords))
  //        .dissentingOpinion(addNoIndexTags(texts.dissentingOpinion(), ignoredWords))
  //        .outline(addNoIndexTags(texts.outline(), ignoredWords))
  //        .build();
  //  }
  //
  //  private ShortTexts updateShortTexts(ShortTexts texts, List<String> ignoredWords) {
  //    return texts.toBuilder()
  //        .headnote(addNoIndexTags(texts.headnote(), ignoredWords))
  //        .otherHeadnote(addNoIndexTags(texts.otherHeadnote(), ignoredWords))
  //        .headline(addNoIndexTags(texts.headline(), ignoredWords))
  //        .guidingPrinciple(addNoIndexTags(texts.guidingPrinciple(), ignoredWords))
  //        .build();
  //  }
  //
  //  /**
  //   * Wraps ignored words with <noindex></noindex> for handover service
  //   *
  //   * @param htmlText to add no index tags to
  //   * @param ignoredWords on Documentation Level or global level
  //   */
  //  public static String addNoIndexTags(String htmlText, List<String> ignoredWords) {
  //    if (htmlText == null || ignoredWords == null || ignoredWords.isEmpty()) {
  //      return htmlText;
  //    }
  //
  //    Document document = Jsoup.parse(htmlText);
  //    document.outputSettings().prettyPrint(false);
  //
  //    for (String ignoredWord : ignoredWords) {
  //      NodeTraversor.traverse(new NoIndexNodeWrapperVisitor(ignoredWord), document.body());
  //    }
  //
  //    return document.body().html();
  //  }

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
            htmlText,
            matches.stream()
                .map(match -> match.toBuilder().category(categoryType).build())
                .map(this::limitReplacements)
                .toList());

    modifiedMatches.forEach(
        match -> {
          // check for ignored words - either ignored once or when ignored globally/ in docunit, the
          // word is added to ignoredTextCheckWords
          boolean isIgnored =
              match.isIgnoredOnce()
                  || (match.ignoredTextCheckWords() != null
                      && !match.ignoredTextCheckWords().isEmpty());
          newHtmlText
              .append(normalizedHtml, lastPosition.get(), match.offset())
              .append(
                  "<text-check id=\"%s\" type=\"%s\" ignored=\"%s\">%s</text-check>"
                      .formatted(
                          match.id(),
                          match.rule().issueType().toLowerCase(),
                          isIgnored,
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

  /**
   * Remove a word from the documentation unit wide ignore list. The global ignore list is not
   * affective.
   *
   * @param documentationUnitId UUID of the documentation unit
   * @param word word to remove from the documentation wide ignore list
   */
  public void removeIgnoredWord(UUID documentationUnitId, String word) {
    ignoredTextCheckWordRepository.deleteWordIgnoredInDocumentationUnitWithId(
        word, documentationUnitId);
  }

  /**
   * Add a word to the documentation unit wide ignore list.
   *
   * @param documentationUnitId UUID of the documentation unit
   * @param word word to add to the documentation unit wide ignore list
   * @return return the saved ignore entry
   */
  public IgnoredTextCheckWord addIgnoreWord(UUID documentationUnitId, String word) {
    return ignoredTextCheckWordRepository.addWord(word, documentationUnitId);
  }

  /**
   * Remove a word from the global ignore list.
   *
   * @param word word to remove from the global ignore list
   * @return true, if the word could be removed. otherwise false
   */
  public boolean removeIgnoredWord(String word) {
    return ignoredTextCheckWordRepository.deleteWordGlobally(word);
  }

  /**
   * Add a word to the global ignore list.
   *
   * @param word word to add to the global ignore list
   * @param documentationOffice documentation unit of the user which added the word (not used at the
   *     moment)
   * @return return the saved ignore entry
   */
  public IgnoredTextCheckWord addIgnoreWord(String word, DocumentationOffice documentationOffice) {
    IgnoredTextCheckWord globallyIgnoreWord =
        ignoredTextCheckWordRepository.getGloballyIgnoreWord(word);
    if (globallyIgnoreWord != null) {
      return globallyIgnoreWord;
    }
    return ignoredTextCheckWordRepository.addWord(word, documentationOffice);
  }

  protected List<Match> addIgnoredTextChecksIndividually(
      UUID documentationUnitId, String originalHtml, List<Match> matches) {
    if (documentationUnitId == null || matches == null || matches.isEmpty()) {
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
              boolean isIgnoredOnce = isWrappedByIgnoreOnceTag(originalHtml, match);

              return match.toBuilder()
                  .ignoredTextCheckWords(ignoredWords)
                  .isIgnoredOnce(isIgnoredOnce)
                  .build();
            })
        .toList();
  }

  /**
   * Checks if the specific word occurrence is preceded by the <ignore-once> opening tag in the raw
   * HTML string, using Match.offset().
   */
  private boolean isWrappedByIgnoreOnceTag(String originalHTML, Match match) {
    if (originalHTML == null || match == null) {
      return false;
    }

    Integer offset = match.offset();

    if (offset == null || offset < 0 || offset >= originalHTML.length()) {
      return false;
    }

    String searchTarget = "<ignore-once>";
    // Search back far enough to catch the tag, the 5 is just a 'safety-buffer' (e.g. for linebreaks
    // or leading whitespaces)
    int searchLength = searchTarget.length() + 5;

    // Define the region immediately preceding the word
    int searchStart = Math.max(0, offset - searchLength);
    String precedingContent = originalHTML.substring(searchStart, offset);

    // Find the last occurrence of the full <ignore-once> tag in the preceding string.
    int lastIgnoreTagIndex = precedingContent.lastIndexOf(searchTarget);

    if (lastIgnoreTagIndex == -1) {
      return false;
    }

    // Ensure the tag is the immediate wrapper: check if the last opening tag of any kind (e.g. <p>,
    // <text-check>) matches the <ignore-once> tag found. This confirms no other tag is nested
    // between.
    int lastOpeningTagIndex = precedingContent.lastIndexOf('<');

    // If the last opening tag found is exactly where the ignore-tag starts, then the word is
    // immediately preceded by the <ignore-once> tag.
    return lastOpeningTagIndex == lastIgnoreTagIndex;
  }

  //  protected record NoIndexNodeWrapperVisitor(String ignoredWord) implements NodeVisitor {
  //
  //    @Override
  //    public void head(@NotNull Node node, int depth) {
  //      if (!(node instanceof TextNode textNode)) return;
  //      if (isInsideNoIndex(textNode)) return;
  //
  //      String text = textNode.getWholeText();
  //      Pattern pattern =
  //          Pattern.compile(
  //              "(?<![\\p{L}\\p{N}])" + Pattern.quote(ignoredWord) + "(?![\\p{L}\\p{N}])",
  //              Pattern.UNICODE_CHARACTER_CLASS);
  //      Matcher matcher = pattern.matcher(text);
  //
  //      if (!matcher.find()) return;
  //
  //      // Build new content
  //      List<Node> newNodes = new ArrayList<>();
  //      int lastEnd = 0;
  //      matcher.reset();
  //
  //      while (matcher.find()) {
  //        if (matcher.start() > lastEnd) {
  //          newNodes.add(new TextNode(text.substring(lastEnd, matcher.start())));
  //        }
  //        newNodes.add(new Element("noindex").text(matcher.group()));
  //        lastEnd = matcher.end();
  //      }
  //
  //      if (lastEnd < text.length()) {
  //        newNodes.add(new TextNode(text.substring(lastEnd)));
  //      }
  //
  //      // Replace original text node
  //      Node parent = textNode.parent();
  //      if (parent instanceof Element element) {
  //        int index = textNode.siblingIndex();
  //        textNode.remove();
  //        element.insertChildren(index, newNodes);
  //      }
  //    }
  //
  //    private boolean isInsideNoIndex(Node node) {
  //      while (node != null) {
  //        if ("noindex".equalsIgnoreCase(node.nodeName())) {
  //          return true;
  //        }
  //        node = node.parent();
  //      }
  //      return false;
  //    }
  //  }

  @SuppressWarnings("java:S3776")
  protected record NormalizingNodeVisitor(StringBuilder builder) implements NodeVisitor {

    @Override
    public void head(@NotNull Node node, int depth) {
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
    public void tail(@NotNull Node node, int depth) {
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
