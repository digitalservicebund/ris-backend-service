package de.bund.digitalservice.ris.caselaw.integration.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExampleTest extends BaseIntegrationTest {

  @Test
  void fail() {
    Assertions.fail("Integration test failed");
  }

  @Test
  @Disabled("Disabled integration test")
  void skip() {}
}
