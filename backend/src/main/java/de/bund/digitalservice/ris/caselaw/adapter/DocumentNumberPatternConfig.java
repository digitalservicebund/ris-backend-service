package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import java.util.Map;
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

  /**
   * Validate that the document number follows the updated document pattern prefixes.
   *
   * @param documentationOfficeAbbreviation document office abbreviation
   * @param documentationUnitNumber document number for check
   * @return true if document office pattern matches the document number pattern
   */
  public boolean hasValidPattern(
      String documentationOfficeAbbreviation, String documentationUnitNumber) {
    try {
      if (StringUtils.returnTrueIfNullOrBlank(documentationUnitNumber)) {
        throw new DocumentNumberPatternException("Document unit number is null or blank");
      }

      var pattern = documentNumberPatterns.get(documentationOfficeAbbreviation);

      if (pattern == null)
        throw new DocumentNumberPatternException(
            documentationOfficeAbbreviation + " is not included in pattern");

      if (!documentationUnitNumber.contains(DocumentNumberFormatter.extractPrefix(pattern))) {
        throw new DocumentNumberPatternException("prefix is not included in pattern");
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
