package de.bund.digitalservice.ris.caselaw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "languagetool")
@Data
public class LanguageToolConfig {
  private String url;
  private String language;
}
