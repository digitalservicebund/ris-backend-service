package de.bund.digitalservice.ris.caselaw.adapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExampleTest {

  @Test
  void fail() {
    Assertions.fail("Test failed");
  }

  @Test
  @Disabled("Disabled test")
  void skip() {}
}
