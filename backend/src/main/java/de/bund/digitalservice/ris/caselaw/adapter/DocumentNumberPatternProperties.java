package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.Map;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neuris")
@Getter
public class DocumentNumberPatternProperties {
  // TODO; Constructur

  private final Map<String, String> documentNumberPatterns;

  public DocumentNumberPatternProperties(@Autowired Map<String, String> documentNumberPatterns) {
    this.documentNumberPatterns = documentNumberPatterns;
  }
}
