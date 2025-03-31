package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckTestConfiguration {

  @Bean
  public TextCheckService textCheckMockService(
      DocumentationUnitService documentationUnitService,
      DocumentationOfficeService documentationOfficeService,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {
    return new TextCheckMockService(
        documentationUnitService, documentationOfficeService, ignoredTextCheckWordRepository);
  }
}
