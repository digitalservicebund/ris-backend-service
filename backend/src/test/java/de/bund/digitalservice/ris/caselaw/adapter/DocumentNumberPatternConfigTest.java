package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application.yaml"})
@EnableConfigurationProperties({
  de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig.class
})
class DocumentNumberPatternConfigTest {

  @Autowired protected DocumentNumberPatternConfig documentNumberPatternConfig;

  @Test
  void validateMaxCharacters() throws DocumentNumberPatternException {
    for (String pattern : documentNumberPatternConfig.getDocumentNumberPatterns().values()) {

      assertEquals(13, pattern.length());
    }
  }

  @Test
  void assertThatDocOfficePatternsAreNotEmpty() throws DocumentNumberPatternException {
    assertFalse(documentNumberPatternConfig.getDocumentNumberPatterns().isEmpty());
  }

  @Test
  void assertDocOfficePatternsAreValid() throws DocumentNumberFormatterException {
    for (Map.Entry<String, String> entry :
        documentNumberPatternConfig.getDocumentNumberPatterns().entrySet()) {

      String docOffice = entry.getKey();
      String value = entry.getValue();

      String pattern =
          DocumentNumberFormatter.builder()
              .sequenceNumber(0)
              .year(DateUtil.getYear())
              .pattern(value)
              .build()
              .generate();

      assertDoesNotThrow(() -> documentNumberPatternConfig.hasValidPattern(docOffice, pattern));
    }
  }
}
