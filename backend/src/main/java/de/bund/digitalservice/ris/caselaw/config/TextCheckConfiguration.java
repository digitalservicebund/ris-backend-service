package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseIgnoredTextCheckWordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckConfiguration {
  @Bean
  public TextCheckService textCheckService(
      LanguageToolConfig config,
      DocumentationUnitRepository documentationUnitRepository,
      DatabaseIgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {
    return new LanguageToolService(
        config, documentationUnitRepository, ignoredTextCheckWordRepository);
  }
}
