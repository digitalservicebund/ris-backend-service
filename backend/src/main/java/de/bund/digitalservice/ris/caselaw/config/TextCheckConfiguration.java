package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TextCheckConfiguration {
  @Bean
  @Profile({"text-check", "staging"})
  public TextCheckService textCheckService(
      LanguageToolConfig config, DocumentationUnitService service) {
    return new LanguageToolService(config, service);
  }

  @Bean
  @Profile("!text-check & !staging")
  public TextCheckService textCheckMockService(DocumentationUnitService service) {
    return new TextCheckMockService(service);
  }
}
