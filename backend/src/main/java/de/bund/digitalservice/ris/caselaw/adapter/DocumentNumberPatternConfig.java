package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Config to import and validate doc number patterns to generate doc numbers by doc office */
@Configuration
@ConfigurationProperties(prefix = "neuris")
@Data
public class DocumentNumberPatternConfig implements InitializingBean {

  Map<String, String> documentNumberPatterns;

  Set<String> prefixes;

  @Override
  public void afterPropertiesSet() throws DocumentNumberPatternException {
    validate();
    savePrefixes();
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

  /**
   * Validate that the document number follows the updated document pattern prefixes.
   *
   * @param documentNumber give document number of documentation unit
   * @return true if prefix matches the document number
   */
  public boolean hasValidPrefix(String documentNumber) {
    if (StringsUtil.returnTrueIfNullOrBlank(documentNumber)) {
      return false;
    }
    return prefixes.stream().anyMatch(documentNumber::contains);
  }

  private void savePrefixes() throws DocumentNumberPatternException {
    prefixes =
        documentNumberPatterns.values().stream()
            .map(DocumentNumberFormatter::extractPrefix)
            .collect(Collectors.toSet());

    if (prefixes.isEmpty()) {
      throw new DocumentNumberPatternException("Document number pattern prefixes are not empty");
    }
  }
}
