package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateKnownValidatorTest {
  private DateKnownValidator validator;

  @BeforeEach
  void setup() {
    validator = new DateKnownValidator();
  }

  @Test
  void testIsValid_withKnownDate() {
    PreviousDecision previousDecision =
        PreviousDecision.builder().dateKnown(true).decisionDate(LocalDate.now()).build();
    Assertions.assertTrue(validator.isValid(previousDecision, null));
  }

  @Test
  void testIsValid_withUnknownDate() {
    PreviousDecision previousDecision =
        PreviousDecision.builder().dateKnown(false).decisionDate(null).build();
    Assertions.assertTrue(validator.isValid(previousDecision, null));
  }

  @Test
  void testIsValid_withUnknownDateAndDecisionDate() {
    PreviousDecision previousDecision =
        PreviousDecision.builder().dateKnown(false).decisionDate(LocalDate.now()).build();
    Assertions.assertFalse(validator.isValid(previousDecision, null));
  }
}
