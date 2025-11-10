package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TextCheckTestConfiguration {
  @Bean
  @Primary
  public TextCheckService textCheckService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {
    return new TextCheckMockService(documentationUnitRepository, ignoredTextCheckWordRepository);
  }
}
