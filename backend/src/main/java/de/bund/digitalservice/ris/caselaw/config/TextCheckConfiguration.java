package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.adapter.languagetool.LanguageToolService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckConfiguration {
  @Bean
  public TextCheckService textCheckService(
      LanguageToolConfig config, DocumentationUnitService service) {
    return new LanguageToolService(config, service);
  }
}
