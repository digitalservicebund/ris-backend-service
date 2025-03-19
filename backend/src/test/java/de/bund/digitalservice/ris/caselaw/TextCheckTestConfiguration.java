package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.TextCheckMockService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextCheckTestConfiguration {

  @Bean
  public TextCheckService textCheckMockService(DocumentationUnitService service) {
    return new TextCheckMockService(service);
  }
}
