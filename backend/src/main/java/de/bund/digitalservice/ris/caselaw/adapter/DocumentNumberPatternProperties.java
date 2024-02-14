package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neuris")
@Getter
public class DocumentNumberPatternProperties {

  Map<String, String> documentNumberPatterns;
}
