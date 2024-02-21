package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neuris")
@Getter
@Setter
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

    documentNumberPatterns
        .values()
        .forEach(
            pattern -> {
              if (pattern.length() != 13) {
                try {
                  throw new DocumentNumberPatternException(
                      "Document number pattern: " + pattern + " must consist of 13 chars");
                } catch (DocumentNumberPatternException e) {
                  throw new RuntimeException(e);
                }
              }
            });
  }
}
