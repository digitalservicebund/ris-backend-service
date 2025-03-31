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
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TextCheckService {
  private final DocumentationUnitService documentationUnitService;

  private final DocumentationOfficeService documentationOfficeService;

  private final IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;

  public TextCheckService(
      DocumentationUnitService documentationUnitService,
      DocumentationOfficeService documentationOfficeService,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {
    this.documentationUnitService = documentationUnitService;
    this.documentationOfficeService = documentationOfficeService;
    this.ignoredTextCheckWordRepository = ignoredTextCheckWordRepository;
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

      List<UUID> docOfficeIds =
          getDocumentationOfficeIds(documentable.coreData().documentationOffice().uuid());

      return switch (category) {
        case REASONS ->
            checkCategoryByHTML(
                documentationUnit.longTexts().reasons(),
                category,
                docOfficeIds,
                documentable.uuid());
        case CASE_FACTS ->
            checkCategoryByHTML(
                documentationUnit.longTexts().caseFacts(),
                category,
                docOfficeIds,
                documentable.uuid());
        case DECISION_REASONS ->
            checkCategoryByHTML(
                documentationUnit.longTexts().decisionReasons(),
                category,
                docOfficeIds,
                documentable.uuid());
        case HEADNOTE ->
            checkCategoryByHTML(
                documentationUnit.shortTexts().headnote(),
                category,
                docOfficeIds,
                documentable.uuid());
        case HEADLINE ->
            checkCategoryByHTML(
                documentationUnit.shortTexts().headline(),
                category,
                docOfficeIds,
                documentable.uuid());
        case GUIDING_PRINCIPLE ->
            checkCategoryByHTML(
                documentationUnit.shortTexts().guidingPrinciple(),
                category,
                docOfficeIds,
                documentable.uuid());
        case TENOR ->
            checkCategoryByHTML(
                documentationUnit.longTexts().tenor(), category, docOfficeIds, documentable.uuid());
        case OTHER_LONG_TEXT ->
            checkCategoryByHTML(
                documentationUnit.longTexts().otherLongText(),
                category,
                docOfficeIds,
                documentable.uuid());
        case DISSENTING_OPINION ->
            checkCategoryByHTML(
                documentationUnit.longTexts().dissentingOpinion(),
                category,
                docOfficeIds,
                documentable.uuid());
        case OUTLINE ->
            checkCategoryByHTML(
                documentationUnit.longTexts().outline(),
                category,
                docOfficeIds,
                documentable.uuid());
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

  @NotNull
  static String normalizeHTML(Document document) {
    StringBuilder builder = new StringBuilder();
    NodeTraversor.traverse(new NormalizingNodeVisitor(builder), document.body().children());
    return builder.toString();
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType) {
    return checkCategoryByHTML(htmlText, categoryType, null, null);
  }

  protected TextCheckCategoryResponse checkCategoryByHTML(
      String htmlText, CategoryType categoryType, List<UUID> docOfficeIds, UUID docUnitId) {
    if (htmlText == null) {
      return null;
    }

    // normalize HTML to assure correct positioning
    String normalizedHtml = normalizeHTML(Jsoup.parse(htmlText));

    List<Match> matches = check(normalizedHtml);

    StringBuilder newHtmlText = new StringBuilder();
    AtomicInteger lastPosition = new AtomicInteger(0);

    List<Match> filteredMatches = addIgnoredTextChecks(docOfficeIds, docUnitId, matches);

    List<Match> modifiedMatches =
        filteredMatches.stream()
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
                          match.ignoredTextCheckWords().isEmpty()
                              ? match.rule().issueType().toLowerCase()
                              : "ignored",
                          normalizedHtml.substring(
                              match.offset(), match.offset() + match.length())));
          lastPosition.set(match.offset() + match.length());
        });

    newHtmlText.append(normalizedHtml, lastPosition.get(), normalizedHtml.length());

    return new TextCheckCategoryResponse(newHtmlText.toString(), modifiedMatches);
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

    return checkCategoryByHTML(text, categoryType, null, null).matches().stream()
        .map(match -> match.toBuilder().category(categoryType).build())
        .toList();
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

  public List<Match> addIgnoredTextChecks(
      List<UUID> documentationOfficeIds, UUID documentationUnitId, List<Match> matches) {

    List<IgnoredTextCheckWord> ignoredTextCheckWords =
        ignoredTextCheckWordRepository
            .findAllByDocumentationOfficesOrUnitAndWords(
                documentationOfficeIds,
                documentationUnitId,
                matches.stream().map(Match::word).toList())
            .stream()
            .toList();

    return matches.stream()
        .map(
            match ->
                match.toBuilder()
                    .ignoredTextCheckWords(
                        ignoredTextCheckWords.stream()
                            .filter(
                                ignoredTextCheckWord ->
                                    ignoredTextCheckWord.getWord().equals(match.word()))
                            .toList())
                    .build())
        .toList();
  }

  public List<UUID> getDocumentationOfficeIds(UUID documentationOfficeId) {

    List<UUID> docOfficeIds = new ArrayList<>();

    documentationOfficeService.getDocumentationOffices("juris").stream()
        .map(DocumentationOffice::uuid)
        .forEach(docOfficeIds::add);

    docOfficeIds.add(documentationOfficeId);
    return docOfficeIds;
  }

  public IgnoredTextCheckWord addIgnoredTextCheckWord(
      IgnoredTextCheckWord ignoredTextCheckWord, UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    Documentable documentable = documentationUnitService.getByUuid(documentationUnitId);

    return ignoredTextCheckWordRepository.addIgnoredTextCheckWord(
        ignoredTextCheckWord,
        documentable.coreData().documentationOffice().uuid(),
        documentationUnitId);
  }
}
