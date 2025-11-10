package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.languagetool.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Category;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Context;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match.MatchBuilder;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Rule;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Suggestion;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckAllResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextCheckResponseTransformer {
  private TextCheckResponseTransformer() {}

  public static TextCheckAllResponse transformToAllDomain(
      List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> matches) {
    Set<CategoryType> categoryTypes = new HashSet<>();
    List<Suggestion> suggestions = new ArrayList<>();
    int totalTextCheckErrors = 0;

    for (de.bund.digitalservice.ris.caselaw.domain.textcheck.Match match : matches) {
      String word = getMatchWord(match);

      Suggestion suggestion =
          suggestions.stream()
              .filter(s -> s.word().equals(word))
              .findFirst()
              .orElseGet(
                  () -> {
                    Suggestion newSuggestion = new Suggestion(word, new ArrayList<>());
                    suggestions.add(newSuggestion);
                    return newSuggestion;
                  });

      suggestion.matches().add(match);
      if (!isIgnored(match)) {
        categoryTypes.add(match.category());
        totalTextCheckErrors++;
      }
    }

    return de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckAllResponse.builder()
        .suggestions(suggestions)
        .categoryTypes(categoryTypes)
        .totalTextCheckErrors(totalTextCheckErrors)
        .build();
  }

  public static de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckResponse
      transformToDomain(List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> matches) {
    return de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckResponse.builder()
        .matches(matches)
        .build();
  }

  public static List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match>
      transformToListOfDomainMatches(List<Match> languageToolMatches) {
    List<de.bund.digitalservice.ris.caselaw.domain.textcheck.Match> matches = new ArrayList<>();

    for (int i = 0; i < languageToolMatches.size(); i++) {
      Match match = languageToolMatches.get(i);
      MatchBuilder matchBuilder =
          de.bund.digitalservice.ris.caselaw.domain.textcheck.Match.builder()
              .id(i + 1) // start from 1
              .message(match.getMessage())
              .shortMessage(match.getShortMessage())
              .ignoreForIncompleteSentence(match.isIgnoreForIncompleteSentence());

      if (match.getContext() != null) {
        String word = getMatchWord(match);
        matchBuilder
            .word(word)
            .context(
                new Context(
                    match.getContext().getText(),
                    match.getContext().getOffset(),
                    match.getContext().getLength()));
      }

      matchBuilder
          .offset(match.getOffset())
          .length(match.getLength())
          .sentence(match.getSentence())
          .type(new Type(match.getType().getTypeName()));

      Rule rule =
          Rule.builder()
              .id(match.getRule().getId())
              .description(match.getRule().getDescription())
              .issueType(match.getRule().getIssueType())
              .category(
                  new Category(
                      match.getRule().getCategory().getId(),
                      match.getRule().getCategory().getName()))
              .build();

      matchBuilder.rule(rule);

      matches.add(matchBuilder.build());
    }

    return matches;
  }

  private static String getMatchWord(
      de.bund.digitalservice.ris.caselaw.domain.textcheck.Match match) {
    return match
        .context()
        .text()
        .substring(match.context().offset(), match.context().offset() + match.context().length());
  }

  private static String getMatchWord(Match match) {
    int startIndex = match.getContext().getOffset();
    int endIndex = match.getContext().getOffset() + match.getContext().getLength();
    return match.getContext().getText().substring(startIndex, endIndex);
  }

  private static boolean isIgnored(
      de.bund.digitalservice.ris.caselaw.domain.textcheck.Match match) {
    var ignoredWords = match.ignoredTextCheckWords();
    var ignoredOnce = match.isIgnoredOnce();

    if (ignoredOnce) {
      return true;
    }

    if (ignoredWords == null || ignoredWords.isEmpty()) {
      return false;
    }

    return ignoredWords.stream()
        .anyMatch(ignoredWordObj -> ignoredWordObj.word().equals(match.word()));
  }
}
