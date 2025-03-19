package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Category;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Context;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolResponse;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Match;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Replacement;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Rule;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Type;
import java.util.ArrayList;
import java.util.List;
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
    List<Replacement> replacements = new ArrayList<>();
    Replacement replacement = new Replacement();
    replacement.setValue("the");
    replacements.add(replacement);
    match.setReplacements(replacements);
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
        TextCheckResponseTransformer.transformToListOfDomainMatches(response);

    // Assert
    assertNotNull(domainMatches);
    assertEquals(1, domainMatches.size());

    de.bund.digitalservice.ris.caselaw.domain.textcheck.Match domainMatch = domainMatches.get(0);
    assertEquals(1, domainMatch.id());
    assertEquals("Possible spelling mistake found.", domainMatch.message());
    assertEquals("Spelling mistake", domainMatch.shortMessage());
    assertTrue(domainMatch.ignoreForIncompleteSentence());
    assertEquals("teh", domainMatch.word());
    assertEquals(1, domainMatch.replacements().size());
    assertEquals("the", domainMatch.replacements().get(0).value());
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
}
