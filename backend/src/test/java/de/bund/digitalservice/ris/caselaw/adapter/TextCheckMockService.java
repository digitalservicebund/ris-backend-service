package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.List;
import java.util.stream.Stream;

public class TextCheckMockService extends TextCheckService {

  public TextCheckMockService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService) {
    super(documentationUnitRepository, ignoredTextCheckWordRepository, featureToggleService);
  }

  @Override
  public List<Match> requestTool(String text) {
    if (text == null) {
      return List.of();
    }
    return Stream.of(
            "ignoredWordOnDocUnitLevel",
            "ignoredWordOnGlobalLevel",
            "ignoredWordOnGlobalJDVLevel",
            "notIgnoredWord")
        .filter(text::contains)
        .map(word -> Match.builder().word(word).build())
        .toList();
  }
}
