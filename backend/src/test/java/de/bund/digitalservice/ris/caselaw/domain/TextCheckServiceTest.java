package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnknownCategoryException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Replacement;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TextCheckServiceTest {

  private DocumentationUnitRepository documentationUnitRepository;
  private TextCheckService textCheckService;

  @BeforeEach
  void setUp() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    IgnoredTextCheckWordRepository ignoredTextCheckWordRepository =
        mock(IgnoredTextCheckWordRepository.class);

    FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    textCheckService =
        new TextCheckMockService(
            documentationUnitRepository, ignoredTextCheckWordRepository, featureToggleService);
  }

  @Test
  void testCheckWholeDocumentationUnit_validDocumentationUnit()
      throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();

    when(documentationUnitRepository.findByUuid(uuid))
        .thenReturn(
            DocumentationUnit.builder()
                .longTexts(
                    LongTexts.builder()
                        .reasons("<p>Reason text</p>")
                        .caseFacts("<p>Case facts text</p>")
                        .decisionReasons("<p>Decision reasons text</p>")
                        .tenor("<p>Tenor text</p>")
                        .build())
                .coreData(CoreData.builder().build())
                .shortTexts(
                    ShortTexts.builder()
                        .headnote("<p>Headnote text</p>")
                        .guidingPrinciple("<p>Guiding principle text</p>")
                        .build())
                .build());

    TestMemoryAppender memoryAppender = new TestMemoryAppender(TextCheckService.class);
    List<Match> result = textCheckService.checkWholeDocumentationUnit(uuid);
    assertThat(memoryAppender.count(Level.ERROR)).isZero();

    assertNotNull(result);
  }

  @Test
  void testCheckWholeDocumentationUnit_invalidDocumentableType()
      throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();
    Documentable documentable = mock(Documentable.class);

    when(documentationUnitRepository.findByUuid(uuid)).thenReturn(documentable);

    var exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> textCheckService.checkWholeDocumentationUnit(uuid));

    assertTrue(exception.getMessage().contains("Check not supported for Documentable type: "));
  }

  @ParameterizedTest
  @EnumSource(CategoryType.class)
  void testCheckCategory_validCategory(CategoryType categoryType)
      throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();

    when(documentationUnitRepository.findByUuid(uuid))
        .thenReturn(
            DocumentationUnit.builder()
                .longTexts(
                    LongTexts.builder()
                        .reasons("<p>Reason text</p>")
                        .tenor("<p>Tenor text</p>")
                        .decisionReasons("<p>Decision reasons text</p>")
                        .caseFacts("<p>Case facts text</p>")
                        .otherLongText("<p>OtherLongText text</p>")
                        .dissentingOpinion("<p>DissentingOpinion text</p>")
                        .outline("<p>Outline text</p>")
                        .build())
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().uuid(UUID.randomUUID()).build())
                        .build())
                .shortTexts(
                    ShortTexts.builder()
                        .guidingPrinciple("<p>Guiding principle text</p>")
                        .headnote("<p>Headnote text</p>")
                        .decisionName("<p>Decision name text</p>")
                        .headline("<p>Headline text</p>")
                        .build())
                .build());

    TextCheckCategoryResponse result = textCheckService.checkCategory(uuid, categoryType);
    assertNotNull(result);
  }

  @Test
  void testCheckCategory_nullCategory() throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();

    when(documentationUnitRepository.findByUuid(uuid))
        .thenReturn(
            DocumentationUnit.builder()
                .longTexts(
                    LongTexts.builder()
                        .reasons("<p>Reason text</p>")
                        .tenor("<p>Tenor text</p>")
                        .decisionReasons("<p>Decision reasons text</p>")
                        .caseFacts("<p>Case facts text</p>")
                        .otherLongText("<p>OtherLongText text</p>")
                        .dissentingOpinion("<p>DissentingOpinion text</p>")
                        .outline("<p>Outline text</p>")
                        .build())
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().uuid(UUID.randomUUID()).build())
                        .build())
                .shortTexts(
                    ShortTexts.builder()
                        .guidingPrinciple("<p>Guiding principle text</p>")
                        .headnote("<p>Headnote text</p>")
                        .decisionName("<p>Decision name text</p>")
                        .headline("<p>Headline text</p>")
                        .build())
                .build());
    assertThrows(
        TextCheckUnknownCategoryException.class, () -> textCheckService.checkCategory(uuid, null));
  }

  @Test
  void testCheckCategory_unknownCategory() throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(uuid))
        .thenReturn(
            DocumentationUnit.builder()
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().uuid(UUID.randomUUID()).build())
                        .build())
                .build());

    assertThrows(
        TextCheckUnknownCategoryException.class, () -> textCheckService.checkCategory(uuid, null));
  }

  @Test
  void testCheckCategory_nullHTML() throws DocumentationUnitNotExistsException {
    UUID uuid = UUID.randomUUID();

    when(documentationUnitRepository.findByUuid(uuid))
        .thenReturn(
            DocumentationUnit.builder()
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().uuid(UUID.randomUUID()).build())
                        .build())
                .longTexts(LongTexts.builder().build())
                .build());

    TextCheckCategoryResponse result = textCheckService.checkCategory(uuid, CategoryType.REASONS);
    assertEquals(null, result);
  }

  @Test
  void testCheckCategoryByHTML_withMatches() {
    String htmlText = "<p>text text widt missspelling</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(3)
                    .length(4)
                    .rule(Rule.builder().issueType("redundancy").build())
                    .build(),
                Match.builder()
                    .id(2)
                    .offset(13)
                    .length(4)
                    .rule(Rule.builder().issueType("typo").build())
                    .build(),
                Match.builder()
                    .id(3)
                    .offset(18)
                    .length(12)
                    .rule(Rule.builder().issueType("typo").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p><text-check id=\"1\" type=\"redundancy\">text</text-check> text <text-check id=\"2\" type=\"typo\">widt</text-check> <text-check id=\"3\" type=\"typo\">missspelling</text-check></p>",
        response.htmlText());
    assertEquals(3, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withMatches_LimitSuggestionsToFive() {
    String htmlText = "<p>z</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(1)
                    .length(1)
                    .rule(Rule.builder().issueType("redundancy").build())
                    .replacements(
                        List.of(
                            new Replacement("a"),
                            new Replacement("b"),
                            new Replacement("c"),
                            new Replacement("d"),
                            new Replacement("e"),
                            new Replacement("f")))
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(5, response.matches().getFirst().replacements().size());
  }

  @Test
  void testCheckCategoryByHTML_withMatchesAndCustomTagsWithAttributes() {
    String htmlText = "<p>text with a <border-number number=\"2\">missspelling</border-number></p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(41)
                    .length(12)
                    .rule(Rule.builder().issueType("typo").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p>text with a <border-number number=\"2\"><text-check id=\"1\" type=\"typo\">missspelling</text-check></border-number></p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withMatchesAndEncodedHtmlChars() {
    String htmlText =
        "<p>This is a test &gt; 10 &amp;&nbsp;&lt; 20. Also &quot;quoted&quot;. And &#x2665; and &#9829;</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(18)
                    .length(17)
                    .rule(Rule.builder().issueType("typo").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p>This is a test <text-check id=\"1\" type=\"typo\">&gt; 10 & &lt; 20</text-check>. Also \"quoted\". And ♥ and ♥</p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withMatchesAndEncodedTags() {
    String htmlText = "<p>This text contains a fake &lt;tag&gt;</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService spyService = spy(textCheckService);

    when(spyService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(33)
                    .length(3)
                    .rule(Rule.builder().issueType("typo").build())
                    .build()));
    when(spyService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = spyService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p>This text contains a fake &lt;<text-check id=\"1\" type=\"typo\">tag</text-check>&gt;</p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withMatchesAndBrs() {
    String htmlText = "<p>This is a,<br>with line<br>breaks</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .offset(12)
                    .length(9)
                    .rule(Rule.builder().issueType("grammar").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p>This is a<text-check id=\"1\" type=\"grammar\">,<br>with</text-check> line<br>breaks</p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_noMatches() {
    String htmlText = "<p>test text</p>";
    CategoryType categoryType = CategoryType.REASONS;

    TextCheckService mockService = spy(textCheckService);

    when(mockService.check(any(String.class))).thenReturn(new ArrayList<>());
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(htmlText, response.htmlText());
    assertEquals(0, response.matches().size());
  }

  @Test
  void testTransformAttributeWithQuotes() {
    String htmlText =
        "<p class=\"font-bold text-white bg-blue-700 before:content-[&quot;Rd_&quot;] ml-1 pr-1\" >test text</p>";
    String normalizedHtml = TextCheckService.normalizeHTML(Jsoup.parse(htmlText));
    assertEquals(
        "<p class=\"font-bold text-white bg-blue-700 before:content-[&quot;Rd_&quot;] ml-1 pr-1\">test text</p>",
        normalizedHtml);
  }

  @Test
  void testTransformGtAndLt() {
    String htmlText = "<p>test with gt &lt; and lt &gt; text</p>";
    String normalizedHtml = TextCheckService.normalizeHTML(Jsoup.parse(htmlText));
    assertEquals("<p>test with gt &lt; and lt &gt; text</p>", normalizedHtml);
  }

  @Test
  void testCheckCategoryByHTML_withIgnoredMatches() {
    String htmlText = "<p>text text with ignored match</p>";
    CategoryType categoryType = CategoryType.REASONS;

    final String ignoredWord = "ignored match";

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .word(ignoredWord)
                    .offset(18)
                    .length(ignoredWord.length())
                    .rule(Rule.builder().issueType("ignored").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response = mockService.checkCategoryByHTML(htmlText, categoryType);

    assertNotNull(response);
    assertEquals(
        "<p>text text with <text-check id=\"1\" type=\"ignored\">ignored match</text-check></p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testAddNoIndexTags_shouldReplaceTags() {
    var html = "<p>this and this be wrapped with no index</p>";
    var result = TextCheckService.addNoIndexTags(html, List.of("this"));

    var expected =
        "<p><noindex>this</noindex> and <noindex>this</noindex> be wrapped with no index</p>";
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @MethodSource("noIndexReplacementCases")
  void testAddNoIndexTags_withMultipleCases(
      String html, List<String> ignoredWords, String expected) {
    String result = TextCheckService.addNoIndexTags(html, ignoredWords);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> noIndexReplacementCases() {
    return Stream.of(
        Arguments.of(
            "<p>CASE insensitive should be replaced</p>",
            List.of("case"),
            "<p><noindex>CASE</noindex> insensitive should be replaced</p>"),
        Arguments.of(
            "<p>partsofwords should not replace</p>",
            List.of("parts"),
            "<p>partsofwords should not replace</p>"),
        Arguments.of(
            "<p>p with no index but not html tag should be replaced</p>",
            List.of("p"),
            "<p><noindex>p</noindex> with no index but not html tag should be replaced</p>"),
        Arguments.of(
            "<p>saved-words-with-hyphen should be replaced</p>",
            List.of("saved-words-with-hyphen"),
            "<p><noindex>saved-words-with-hyphen</noindex> should be replaced</p>"),
        Arguments.of(
            "<p>hyphenated-word - first part should not replace</p>",
            List.of("word"),
            "<p>hyphenated-word - first part should not replace</p>"),
        Arguments.of(
            "<p>\"[word]\" - words in square brackets should be replaced</p>",
            List.of("word"),
            "<p>\"[<noindex>word</noindex>]\" - words in square brackets should be replaced</p>"),
        Arguments.of(
            "<p>\"(word)\" - words in round brackets should be replaced</p>",
            List.of("word"),
            "<p>\"(<noindex>word</noindex>)\" - words in round brackets should be replaced</p>"),
        Arguments.of(
            "<p>\"word\" - words in double quotes should be replaced</p>",
            List.of("word"),
            "<p>\"<noindex>word</noindex>\" - words in double quotes should be replaced</p>"),
        Arguments.of(
            "<p>word, other word - both words should be replaced</p>",
            List.of("word"),
            "<p><noindex>word</noindex>, other <noindex>word</noindex> - both words should be replaced</p>"),
        Arguments.of(
            "<p>word; other word - both words should be replaced</p>",
            List.of("word"),
            "<p><noindex>word</noindex>; other <noindex>word</noindex> - both words should be replaced</p>"),
        Arguments.of(
            "<p>word;other word - both words should be replaced</p>",
            List.of("word"),
            "<p><noindex>word</noindex>;other <noindex>word</noindex> - both words should be replaced</p>"),
        Arguments.of(
            "<p>WORD. should replace</p>",
            List.of("WORD"),
            "<p><noindex>WORD</noindex>. should replace</p>"),
        Arguments.of(
            "<p>Abc§116A should replace</p>",
            List.of("Abc§116A"),
            "<p><noindex>Abc§116A</noindex> should replace</p>"));
  }
}
