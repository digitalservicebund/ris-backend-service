package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Year;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DocumentNumberFormatterTest {
  @Test
  void shouldCreateFormat() {
    String format = "KORE7****YYYY";
    Year currentYear = DateUtil.getCurrentYear();

    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder().pattern(format).year(currentYear).docNumber(2).build();

    var expectedId = "KORE70002" + currentYear;

    assert (documentNumberFormatter.toString().endsWith(DateUtil.getCurrentYear().toString()));
    Assertions.assertEquals(expectedId, documentNumberFormatter.toString());
  }

  @Test
  void shouldCreateShortYearFormat() {
    var currentYear = DateUtil.getCurrentYear();
    String format = "BSGRE1****YY";
    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder().pattern(format).year(currentYear).docNumber(1).build();
    assert (documentNumberFormatter.toString().endsWith(DateUtil.getYearAsYY(currentYear)));
  }

  @Test
  void shouldThrownExceptionWhenDocNumberOverflow() {
    Exception exception =
        assertThrows(
            DocumentNumberPatternException.class,
            () -> {
              String format = "BSGRE1****YY";
              DocumentNumberFormatter documentNumberFormatter =
                  DocumentNumberFormatter.builder()
                      .pattern(format)
                      .year(DateUtil.getCurrentYear())
                      .docNumber(10000)
                      .build();
              documentNumberFormatter.toString();
            });

    String expectedMessage = "Doc number is bigger than the * amount";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  // Todo: validation annotation dont work.
  void shouldThrownExceptionWhenDocNumberWithNegativeDocNumber() {

    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(null)
            .year(DateUtil.getCurrentYear())
            .docNumber(-4)
            .build();
  }
}
