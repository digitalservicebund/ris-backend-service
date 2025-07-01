package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Config to import and validate doc number patterns to generate doc numbers by doc office */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "neuris")
@Data
public class DocumentNumberPatternConfig {

  Map<String, String> documentNumberPatterns;

  /**
   * Validate that the document number follows the updated document pattern prefixes.
   *
   * @param documentationOfficeAbbreviation document office abbreviation
   * @param documentationUnitNumber document number for check
   * @return true if document office pattern matches the document number pattern
   */
  public boolean hasValidPattern(
      String documentationOfficeAbbreviation, String documentationUnitNumber) {
    var pattern = documentNumberPatterns.get(documentationOfficeAbbreviation);

    try {
      if (StringUtils.returnTrueIfNullOrBlank(documentationUnitNumber)) {
        throw new DocumentNumberPatternException("Document unit number is null or blank");
      }

      if (pattern == null)
        throw new DocumentNumberPatternException(
            documentationOfficeAbbreviation + " is not included in pattern");

      if (!documentationUnitNumber.contains(DocumentNumberFormatter.extractPrefix(pattern))
          && !documentationOfficeAbbreviation.equals("BFH")) {
        throw new DocumentNumberPatternException(
            String.format(
                "Invalid pattern %s: the prefix must appear before the sequence part (****).",
                pattern));
      }
      return true;
    } catch (Exception e) {
      throw new DocumentNumberPatternException("Pattern: " + pattern + "is invalid cause: ", e);
    }
  }
}
