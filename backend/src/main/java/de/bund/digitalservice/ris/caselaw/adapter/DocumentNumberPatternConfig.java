package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neuris")
@Data
public class DocumentNumberPatternConfig implements InitializingBean {

  Map<String, String> documentNumberPatterns;

  @Override
  public void afterPropertiesSet() throws DocumentNumberPatternException {
    validate();
  }

  private void validate() throws DocumentNumberPatternException {
    if (documentNumberPatterns == null || documentNumberPatterns.isEmpty()) {
      throw new DocumentNumberPatternException(
          "Document number pattern list is empty check yml config");
    }
    validateMaxCharacters();
  }

  private void validateMaxCharacters() throws DocumentNumberPatternException {
    for (String pattern : documentNumberPatterns.values()) {
      if (pattern.length() != 13) {
        throw new DocumentNumberPatternException(
            "Document number pattern: " + pattern + " must consist of 13 chars");
      }
    }
  }
}
