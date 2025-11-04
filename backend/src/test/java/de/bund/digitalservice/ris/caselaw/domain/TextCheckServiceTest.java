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
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.lang.reflect.Method;
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

  private IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;

  @BeforeEach
  void setUp() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    ignoredTextCheckWordRepository = mock(IgnoredTextCheckWordRepository.class);

    FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
    when(featureToggleService.isEnabled("neuris.text-check-noindex-handover")).thenReturn(true);
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
            Decision.builder()
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
    DocumentationUnit documentationUnit = mock(DocumentationUnit.class);

    when(documentationUnitRepository.findByUuid(uuid)).thenReturn(documentationUnit);

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
            Decision.builder()
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
                            DocumentationOffice.builder().id(UUID.randomUUID()).build())
                        .build())
                .shortTexts(
                    ShortTexts.builder()
                        .guidingPrinciple("<p>Guiding principle text</p>")
                        .headnote("<p>Headnote text</p>")
                        .otherHeadnote("<p>Other headnote text</p>")
                        .decisionNames(List.of("<p>Decision name text</p>"))
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
            Decision.builder()
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
                            DocumentationOffice.builder().id(UUID.randomUUID()).build())
                        .build())
                .shortTexts(
                    ShortTexts.builder()
                        .guidingPrinciple("<p>Guiding principle text</p>")
                        .headnote("<p>Headnote text</p>")
                        .decisionNames(List.of("<p>Decision name text</p>"))
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
            Decision.builder()
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().id(UUID.randomUUID()).build())
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
            Decision.builder()
                .coreData(
                    CoreData.builder()
                        .documentationOffice(
                            DocumentationOffice.builder().id(UUID.randomUUID()).build())
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
        "<p><text-check id=\"1\" type=\"redundancy\" ignored=\"false\">text</text-check> text <text-check id=\"2\" type=\"typo\" ignored=\"false\">widt</text-check> <text-check id=\"3\" type=\"typo\" ignored=\"false\">missspelling</text-check></p>",
        response.htmlText());
    assertEquals(3, response.matches().size());
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
        "<p>text with a <border-number number=\"2\"><text-check id=\"1\" type=\"typo\" ignored=\"false\">missspelling</text-check></border-number></p>",
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
        "<p>This is a test <text-check id=\"1\" type=\"typo\" ignored=\"false\">&gt; 10 & &lt; 20</text-check>. Also \"quoted\". And ♥ and ♥</p>",
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
        "<p>This text contains a fake &lt;<text-check id=\"1\" type=\"typo\" ignored=\"false\">tag</text-check>&gt;</p>",
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
        "<p>This is a<text-check id=\"1\" type=\"grammar\" ignored=\"false\">,<br>with</text-check> line<br>breaks</p>",
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
  void testCheckCategoryByHTML_withGloballyIgnoredMatches() {
    String htmlText = "<p>text text with ignored match</p>";
    CategoryType categoryType = CategoryType.REASONS;

    final String ignoredWord = "ignored match";
    final UUID docUnitId = UUID.randomUUID();

    var globallyIgnoredWord =
        new IgnoredTextCheckWord(UUID.randomUUID(), IgnoredTextCheckType.GLOBAL, ignoredWord);

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .word(ignoredWord)
                    .offset(18)
                    .length(ignoredWord.length())
                    .rule(Rule.builder().issueType("misspelling").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of(globallyIgnoredWord));

    TextCheckCategoryResponse response =
        mockService.checkCategoryByHTML(htmlText, categoryType, docUnitId);

    assertNotNull(response);
    assertEquals(
        "<p>text text with <text-check id=\"1\" type=\"misspelling\" ignored=\"true\">ignored match</text-check></p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withDocunitIgnoredMatches() {
    String htmlText = "<p>text text with ignored match</p>";
    CategoryType categoryType = CategoryType.REASONS;

    final String ignoredWord = "ignored match";
    final UUID docUnitId = UUID.randomUUID();

    var docunitIgnoredWord =
        new IgnoredTextCheckWord(
            UUID.randomUUID(), IgnoredTextCheckType.DOCUMENTATION_UNIT, ignoredWord);

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of(docunitIgnoredWord));

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .word(ignoredWord)
                    .offset(18)
                    .length(ignoredWord.length())
                    .rule(Rule.builder().issueType("misspelling").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response =
        mockService.checkCategoryByHTML(htmlText, categoryType, docUnitId);

    assertNotNull(response);
    assertEquals(
        "<p>text text with <text-check id=\"1\" type=\"misspelling\" ignored=\"true\">ignored match</text-check></p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withOnceIgnoredMatches() {
    String htmlText = "<p>text text with <ignore-once>once ignored match</ignore-once></p>";
    CategoryType categoryType = CategoryType.REASONS;

    final String onceIgnoredWord = "once ignored match";
    final UUID docUnitId = UUID.randomUUID();
    // no globally or docunit ignored words
    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of());

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .word(onceIgnoredWord)
                    .offset(31)
                    .length(onceIgnoredWord.length())
                    .rule(Rule.builder().issueType("misspelling").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response =
        mockService.checkCategoryByHTML(htmlText, categoryType, docUnitId);

    assertNotNull(response);
    assertEquals(
        "<p>text text with <ignore-once><text-check id=\"1\" type=\"misspelling\" ignored=\"true\">once ignored match</text-check></ignore-once></p>",
        response.htmlText());
    assertEquals(1, response.matches().size());
  }

  @Test
  void testCheckCategoryByHTML_withOnceIgnoredWord_withSeveralOccurences() {
    String htmlText =
        "<p>text text with once ignored <ignore-once>errrror</ignore-once> and same word again errrror</p>";
    CategoryType categoryType = CategoryType.REASONS;

    final String onceIgnoredWord = "errrror";
    final UUID docUnitId = UUID.randomUUID();
    // no globally or docunit ignored words
    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of());

    TextCheckService mockService = spy(textCheckService);
    when(mockService.check(any(String.class)))
        .thenReturn(
            List.of(
                Match.builder()
                    .id(1)
                    .word(onceIgnoredWord)
                    .offset(44)
                    .length(onceIgnoredWord.length())
                    .rule(Rule.builder().issueType("misspelling").build())
                    .build(),
                Match.builder()
                    .id(2)
                    .word(onceIgnoredWord)
                    .offset(86)
                    .length(onceIgnoredWord.length())
                    .rule(Rule.builder().issueType("misspelling").build())
                    .build()));
    when(mockService.checkCategoryByHTML(any(String.class), any(CategoryType.class)))
        .thenCallRealMethod();

    TextCheckCategoryResponse response =
        mockService.checkCategoryByHTML(htmlText, categoryType, docUnitId);

    assertNotNull(response);
    assertEquals(
        "<p>text text with once ignored <ignore-once><text-check id=\"1\" type=\"misspelling\" ignored=\"true\">errrror</text-check></ignore-once> and same word again <text-check id=\"2\" type=\"misspelling\" ignored=\"false\">errrror</text-check></p>",
        response.htmlText());
    assertEquals(2, response.matches().size());
  }

  private static final UUID TEST_DOC_UNIT_ID = UUID.randomUUID();
  private static final String MATCH_WORD = "misspelling";

  @Test
  void testAddIgnoredTextChecksIndividually_withGloballyIgnoredWord() {
    String originalHtml = "<p>text misspelling text</p>";
    IgnoredTextCheckWord globallyIgnoredWord =
        new IgnoredTextCheckWord(UUID.randomUUID(), IgnoredTextCheckType.GLOBAL, MATCH_WORD);
    Match matchBeforeIgnoreLogic =
        Match.builder()
            .id(1)
            .word(MATCH_WORD)
            .offset(8)
            .length(MATCH_WORD.length())
            .rule(Rule.builder().issueType("missspelling").build())
            .build();

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of(globallyIgnoredWord));

    // ACT
    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, List.of(matchBeforeIgnoreLogic));

    // ASSERT
    Match resultMatch = result.getFirst();
    assertThat(resultMatch.isIgnoredOnce()).isFalse();
    assertThat(resultMatch.ignoredTextCheckWords()).hasSize(1);
  }

  @Test
  void testAddIgnoredTextChecksIndividually_withGloballyIgnoredWord_withMultipleOccurance() {
    String originalHtml = "<p>text misspelling text misspelling</p>";
    IgnoredTextCheckWord globallyIgnoredWord =
        new IgnoredTextCheckWord(UUID.randomUUID(), IgnoredTextCheckType.GLOBAL, MATCH_WORD);
    List<Match> matchesBeforeIgnoreLogic =
        List.of(
            Match.builder()
                .id(1)
                .word(MATCH_WORD)
                .offset(8)
                .length(MATCH_WORD.length())
                .rule(Rule.builder().issueType("misspelling").build())
                .build(),
            Match.builder()
                .id(2)
                .word(MATCH_WORD)
                .offset(25)
                .length(MATCH_WORD.length())
                .rule(Rule.builder().issueType("misspelling").build())
                .build());

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of(globallyIgnoredWord));

    // ACT
    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, matchesBeforeIgnoreLogic);

    // ASSERT
    Match resultMatch1 = result.getFirst();
    assertThat(resultMatch1.isIgnoredOnce()).isFalse();
    assertThat(resultMatch1.ignoredTextCheckWords()).hasSize(1);

    Match resultMatch2 = result.get(1);
    assertThat(resultMatch2.isIgnoredOnce()).isFalse();
    assertThat(resultMatch2.ignoredTextCheckWords()).hasSize(1);
  }

  @Test
  void testAddIgnoredTextChecksIndividually_IgnoredInDocunitOnly() {
    String originalHtml = "<p>text misspelling text</p>";
    IgnoredTextCheckWord globallyIgnoredWord =
        new IgnoredTextCheckWord(
            UUID.randomUUID(), IgnoredTextCheckType.DOCUMENTATION_UNIT, MATCH_WORD);
    Match matchBeforeIgnoreLogic =
        Match.builder()
            .id(1)
            .word(MATCH_WORD)
            .offset(8)
            .length(MATCH_WORD.length())
            .rule(Rule.builder().issueType("missspelling").build())
            .isIgnoredOnce(false)
            .build();

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of(globallyIgnoredWord));

    // ACT
    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, List.of(matchBeforeIgnoreLogic));

    // ASSERT
    Match resultMatch = result.getFirst();
    assertThat(resultMatch.isIgnoredOnce()).isFalse();
    assertThat(resultMatch.ignoredTextCheckWords()).hasSize(1);
  }

  @Test
  void testAddIgnoredTextChecksIndividually_withOnceIgnoredWord() {
    // ARRANGE: HTML HAS <ignore-once> tag
    String originalHtml = "<p>text <ignore-once>misspelling</ignore-once> text</p>";

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of());

    Match matchBeforeIgnoreLogic =
        Match.builder()
            .id(1)
            .word(MATCH_WORD)
            .offset(21)
            .length(MATCH_WORD.length())
            .rule(Rule.builder().issueType("missspelling").build())
            .isIgnoredOnce(false)
            .build();

    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, List.of(matchBeforeIgnoreLogic));

    Match resultMatch = result.getFirst();
    assertThat(resultMatch.isIgnoredOnce()).isTrue();
    assertThat(resultMatch.ignoredTextCheckWords()).isNullOrEmpty();
  }

  @Test
  void testAddIgnoredTextChecksIndividually_withOnceIgnoredWord_withSeveralOccurences() {
    // HTML with one wrapped instance and one unwrapped instance
    String originalHtml =
        "<p>text text with <ignore-once>errrror</ignore-once> and same word again errrror</p>";
    String word = "errrror";

    final int FIRST_OFFSET = 31;
    final int SECOND_OFFSET = 75;

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(any(), any()))
        .thenReturn(List.of());

    List<Match> matchesBeforeLogic =
        List.of(
            Match.builder().id(1).word(word).offset(FIRST_OFFSET).length(word.length()).build(),
            Match.builder().id(2).word(word).offset(SECOND_OFFSET).length(word.length()).build());

    // ACT: Run the logic that sets isIgnoredOnce
    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, matchesBeforeLogic);

    // ASSERT
    assertThat(result.get(0).isIgnoredOnce()).isTrue();
    assertThat(result.get(1).isIgnoredOnce()).isFalse();
  }

  @Test
  void testAddIgnoredTextChecksIndividually_IgnoredByNeither() {
    String originalHtml = "<p>text misspelling text</p>";

    when(ignoredTextCheckWordRepository.findByDocumentationUnitIdOrByGlobalWords(
            any(), any(UUID.class)))
        .thenReturn(List.of());

    Match matchBeforeIgnoreLogic =
        Match.builder()
            .id(1)
            .word(MATCH_WORD)
            .offset(8)
            .length(MATCH_WORD.length())
            .rule(Rule.builder().issueType("missspelling").build())
            .isIgnoredOnce(false)
            .build();

    List<Match> result =
        textCheckService.addIgnoredTextChecksIndividually(
            TEST_DOC_UNIT_ID, originalHtml, List.of(matchBeforeIgnoreLogic));

    Match resultMatch = result.getFirst();
    assertThat(resultMatch.isIgnoredOnce()).isFalse();
    assertThat(resultMatch.ignoredTextCheckWords()).isNullOrEmpty();
  }

  @Test
  void testIsWrappedByIgnoreOnceTag_NullChecksDirectly() throws Exception {
    // ARRANGE: Set up the necessary objects for reflection
    String html = "<p>test</p>";
    Match dummyMatch = Match.builder().word("test").build();

    // 1. Locate the private method using its name and argument types
    Method method =
        TextCheckService.class.getDeclaredMethod(
            "isWrappedByIgnoreOnceTag", String.class, Match.class);

    // 2. Make the private method accessible
    method.setAccessible(true);

    // Test Case 1: originalDoc is null
    boolean resultDocNull = (boolean) method.invoke(textCheckService, null, dummyMatch);
    assertThat(resultDocNull).isFalse(); // Should cover originalDoc == null

    // Test Case 2: match is null
    boolean resultMatchNull = (boolean) method.invoke(textCheckService, html, null);
    assertThat(resultMatchNull).isFalse(); // Should cover match == null

    // Clean up
    method.setAccessible(false);
  }

  @Test
  void testAddNoIndexTags_shouldReplaceTags() {
    var html = "<p>this and this be wrapped with no index</p>";
    var result = TextCheckService.addNoIndexTags(html, List.of("this"));

    var expected =
        "<p><noindex>this</noindex> and <noindex>this</noindex> be wrapped with no index</p>";
    assertEquals(expected, result);
  }

  @Test
  void testAddNoIndexForPublication_shouldUpdateAllSupportedFields() {
    var uuid = UUID.randomUUID();
    var ignoredWord =
        new IgnoredTextCheckWord(UUID.randomUUID(), IgnoredTextCheckType.GLOBAL, "text");
    var docUnit =
        Decision.builder()
            .uuid(uuid)
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
                        DocumentationOffice.builder().id(UUID.randomUUID()).build())
                    .build())
            .shortTexts(
                ShortTexts.builder()
                    .guidingPrinciple("<p>Guiding principle text</p>")
                    .headnote("<p>Headnote text</p>")
                    .otherHeadnote("<p>Other headnote text</p>")
                    .headline("<p>Headline text</p>")
                    .build())
            .build();

    when(ignoredTextCheckWordRepository.findAllByDocumentationUnitId(uuid))
        .thenReturn(List.of(ignoredWord));

    var result = textCheckService.addNoIndexTagsForHandOver(docUnit);

    // Long texts
    assertTrue(result.longTexts().reasons().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().tenor().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().decisionReasons().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().caseFacts().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().otherLongText().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().dissentingOpinion().contains("<noindex>text</noindex>"));
    assertTrue(result.longTexts().outline().contains("<noindex>text</noindex>"));

    // Short text
    assertTrue(result.shortTexts().guidingPrinciple().contains("<noindex>text</noindex>"));
    assertTrue(result.shortTexts().headnote().contains("<noindex>text</noindex>"));
    assertTrue(result.shortTexts().otherHeadnote().contains("<noindex>text</noindex>"));
    assertTrue(result.shortTexts().headline().contains("<noindex>text</noindex>"));
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
            "<p>CASE insensitive should not be replaced</p>",
            List.of("case"),
            "<p>CASE insensitive should not be replaced</p>"),
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
            "<p>hyphenated-<noindex>word</noindex> - first part should not replace</p>"),
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
            "<article>add-no-index should not reformat text</article>",
            List.of("add-no-index"),
            "<article><noindex>add-no-index</noindex> should not reformat text</article>"),
        Arguments.of(
            "<p>Abc§116A should replace</p>",
            List.of("Abc§116A"),
            "<p><noindex>Abc§116A</noindex> should replace</p>"),
        Arguments.of(
            "<border-number><number>7</number><content><p><noindex>should-keep-text</noindex></p></content></border-number>",
            List.of("Abc§116A"),
            "<border-number><number>7</number><content><p><noindex>should-keep-text</noindex></p></content></border-number>"),
        Arguments.of(
            "<em>vgl - should not add white space</em>",
            List.of("vgl"),
            "<em><noindex>vgl</noindex> - should not add white space</em>"),
        Arguments.of(
            "<em>NJW-RR - should not nest noindex tag</em>",
            List.of("NJW-RR", "NJW"),
            "<em><noindex>NJW-RR</noindex> - should not nest noindex tag</em>"));
  }
}
