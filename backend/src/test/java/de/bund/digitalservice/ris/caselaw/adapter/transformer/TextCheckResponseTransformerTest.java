package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Category;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Context;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolResponse;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Match;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Rule;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Type;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Suggestion;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckAllResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TextCheckResponseTransformerTest {

  @Test
  void transformToListOfDomainMatches_shouldTransformCorrectly() {
    // Arrange
    LanguageToolResponse response = new LanguageToolResponse();
    List<Match> matches = new ArrayList<>();
    Match match = new Match();
    match.setMessage("Possible spelling mistake found.");
    match.setShortMessage("Spelling mistake");
    match.setIgnoreForIncompleteSentence(true);
    Context context = new Context();
    context.setText("teh sentence.");
    context.setOffset(0);
    context.setLength(3);
    match.setContext(context);
    match.setOffset(0);
    match.setLength(3);
    match.setSentence("teh sentence.");
    Type type = new Type();
    type.setTypeName("misspelling");
    match.setType(type);
    Rule rule = new Rule();
    rule.setId("MORFOLOGIK_RULE_EN_US");
    rule.setDescription("Possible spelling mistake");
    rule.setIssueType("typographical");
    Category category = new Category();
    category.setId("typos");
    category.setName("Typos");
    rule.setCategory(category);
    match.setRule(rule);
    matches.add(match);
    response.setMatches(matches);

    // Act
    List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> domainMatches =
        TextCheckResponseTransformer.transformToListOfDomainMatches(response.getMatches());

    // Assert
    assertNotNull(domainMatches);
    assertEquals(1, domainMatches.size());

    de.bund.digitalservice.ris.caselaw.domain.textcheck.Match domainMatch = domainMatches.get(0);
    assertEquals(1, domainMatch.id());
    assertEquals("Possible spelling mistake found.", domainMatch.message());
    assertEquals("Spelling mistake", domainMatch.shortMessage());
    assertTrue(domainMatch.ignoreForIncompleteSentence());
    assertEquals("teh", domainMatch.word());
    assertEquals(0, domainMatch.offset());
    assertEquals(3, domainMatch.length());
    assertEquals("teh sentence.", domainMatch.sentence());
    assertEquals("misspelling", domainMatch.type().typeName());
    assertEquals("MORFOLOGIK_RULE_EN_US", domainMatch.rule().id());
    assertEquals("Possible spelling mistake", domainMatch.rule().description());
    assertEquals("typographical", domainMatch.rule().issueType());
    assertEquals("typos", domainMatch.rule().category().id());
    assertEquals("Typos", domainMatch.rule().category().name());
    assertEquals("teh sentence.", domainMatch.context().text());
    assertEquals(0, domainMatch.context().offset());
    assertEquals(3, domainMatch.context().length());
  }

  @Test
  void givenListOfErrorsWithIgnoredWords_whenTransforming_thenVerifyCorrectIgnoredWordsCount() {
    // Arrange = given
    var ignoredWordId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> matches = new ArrayList<>();
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Replacement replacementOne =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Replacement("Richtig");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Context contextOne =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Context("<p>Rihctig<p>", 3, 7);
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Context contextTwo =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Context("<p>geanu<p>", 3, 5);
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Type type =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Type("UnknownWord");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Category category =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Category(
            "TYPOS", "Mögliche Tippfehler");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord
        ignoredWord =
            new de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words
                .IgnoredTextCheckWord(
                ignoredWordId, IgnoredTextCheckType.DOCUMENTATION_UNIT, "geanu");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule rule =
        de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule.builder()
            .id("GERMAN_SPELLER_RULE")
            .description("Möglicher Rechtschreibfehler")
            .issueType("misspelling")
            .category(category)
            .build();
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Match matchOne =
        de.bund.digitalservice.ris.caselaw.domain.textcheck.Match.builder()
            .id(1)
            .word("Rihctig")
            .message("Möglicher Tippfehler gefunden.")
            .shortMessage("Rechtschreibfehler")
            .category(CategoryType.GUIDING_PRINCIPLE)
            .offset(3)
            .length(7)
            .context(contextOne)
            .sentence("Rihctig geanu")
            .type(type)
            .rule(rule)
            .ignoreForIncompleteSentence(false)
            .contextForSureMatch(0)
            .ignoredTextCheckWords(List.of())
            .build();
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Match matchTwo =
        de.bund.digitalservice.ris.caselaw.domain.textcheck.Match.builder()
            .id(2)
            .word("geanu")
            .message("Möglicher Tippfehler gefunden.")
            .shortMessage("Rechtschreibfehler")
            .category(CategoryType.GUIDING_PRINCIPLE)
            .offset(11)
            .length(5)
            .context(contextTwo)
            .sentence("Rihctig geanu")
            .type(type)
            .rule(rule)
            .ignoreForIncompleteSentence(false)
            .contextForSureMatch(0)
            .ignoredTextCheckWords(List.of(ignoredWord))
            .build();
    matches.add(matchOne);
    matches.add(matchTwo);

    Suggestion suggestionOne = new Suggestion("Rihctig", List.of(matchOne));
    Suggestion suggestionTwo = new Suggestion("geanu", List.of(matchTwo));
    TextCheckAllResponse expected =
        TextCheckAllResponse.builder()
            .suggestions(List.of(suggestionOne, suggestionTwo))
            .categoryTypes(Set.of(CategoryType.GUIDING_PRINCIPLE))
            .totalTextCheckErrors(1)
            .build();

    // Act = when
    var result = TextCheckResponseTransformer.transformToAllDomain(matches);

    // Assert = then
    assertEquals(expected, result);
  }

  @Test
  void givenLocallyIgnoredWord_whenTransforming_thenVerifyCorrectIgnoredWordsCount() {
    // Arrange = given
    List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> matches = new ArrayList<>();
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Context contextTwo =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Context("<p>geanu<p>", 3, 5);
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Type type =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Type("UnknownWord");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Category category =
        new de.bund.digitalservice.ris.caselaw.domain.textcheck.Category(
            "TYPOS", "Mögliche Tippfehler");
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule rule =
        de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule.builder()
            .id("GERMAN_SPELLER_RULE")
            .description("Möglicher Rechtschreibfehler")
            .issueType("misspelling")
            .category(category)
            .build();
    de.bund.digitalservice.ris.caselaw.domain.textcheck.Match matchTwo =
        de.bund.digitalservice.ris.caselaw.domain.textcheck.Match.builder()
            .id(1)
            .word("geanu")
            .message("Möglicher Tippfehler gefunden.")
            .shortMessage("Rechtschreibfehler")
            .category(CategoryType.GUIDING_PRINCIPLE)
            .offset(11)
            .length(5)
            .context(contextTwo)
            .sentence("Rihctig geanu")
            .type(type)
            .rule(rule)
            .ignoreForIncompleteSentence(false)
            .contextForSureMatch(0)
            .isIgnoredOnce(true)
            .build();
    matches.add(matchTwo);

    Suggestion suggestionTwo = new Suggestion("geanu", List.of(matchTwo));
    TextCheckAllResponse expected =
        TextCheckAllResponse.builder()
            .suggestions(List.of(suggestionTwo))
            .categoryTypes(Collections.emptySet())
            .totalTextCheckErrors(0)
            .build();

    // Act = when
    var result = TextCheckResponseTransformer.transformToAllDomain(matches);

    // Assert = then
    assertEquals(expected, result);
  }
}
