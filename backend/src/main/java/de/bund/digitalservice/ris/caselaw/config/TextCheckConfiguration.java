package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolClient;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckConfiguration {
  @Bean
  public TextCheckService textCheckService(
      LanguageToolConfig config,
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService,
      LanguageToolClient languageToolClient) {
    return new LanguageToolService(
        documentationUnitRepository,
        ignoredTextCheckWordRepository,
        featureToggleService,
        config,
        languageToolClient);
  }
}
