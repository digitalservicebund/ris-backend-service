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
  public void afterPropertiesSet() {
    validate();
  }

  private void validate() {
    if (documentNumberPatterns == null || documentNumberPatterns.isEmpty()) {
      throw new DocumentNumberPatternException(
          "Document number pattern list is empty check yml config");
    }

    documentNumberPatterns
        .values()
        .forEach(
            pattern -> {
              if (pattern.length() != 13) {
                throw new DocumentNumberPatternException(
                    "Document number pattern: " + pattern + " must consist of 13 digits");
              }
            });
  }
}
