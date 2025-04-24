package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckTestConfiguration {

  @Bean
  public TextCheckService textCheckMockService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository,
      FeatureToggleService featureToggleService) {
    return new TextCheckMockService(
        documentationUnitRepository, ignoredTextCheckWordRepository, featureToggleService);
  }
}
