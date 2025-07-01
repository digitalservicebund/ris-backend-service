package de.bund.digitalservice.ris.caselaw.adapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.config.location=classpath:application-staging.yaml"})
class DocumentNumberPatternConfigStagingTest extends DocumentNumberPatternConfigTest {

  @Test
  void assertPatternsStartsWithXXRE() {
    this.documentNumberPatternConfig
        .getDocumentNumberPatterns()
        .values()
        .forEach(
            pattern -> {
              Assertions.assertTrue(
                  pattern.startsWith("XXRE"), "Doc number pattern must start with XXRE on staging");
            });
  }
}
