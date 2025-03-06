package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.io.IOException;
import java.util.List;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import org.springframework.context.annotation.Bean;

public class LanguageToolLibrary extends TextCheckService {
  private final LanguageToolConfig languageToolConfig;

  public LanguageToolLibrary(
      LanguageToolConfig languageToolConfig, DocumentationUnitService documentationUnitService) {

    super(documentationUnitService);
    this.languageToolConfig = languageToolConfig;
  }

  @Bean
  public JLanguageTool getLanguageTool() {
    return new JLanguageTool(Languages.getLanguageForShortCode(languageToolConfig.getLanguage()));
  }

  @Override
  protected List<Match> requestTool(String text) {
    JLanguageTool langTool = getLanguageTool();

    try {
      // TODO comment in to use statistical ngram data:
      // langTool.activateLanguageModelRules(new File("/data/google-ngram-data"));
      List<RuleMatch> matches = langTool.check(text);
      return TextCheckResponseTransformer.transformToListOfDomainMatches(matches);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
