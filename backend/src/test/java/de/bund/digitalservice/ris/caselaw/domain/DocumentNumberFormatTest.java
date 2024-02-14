package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Year;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DocumentNumberFormatTest {
  @Test
  void shouldCreateFormat() {
    String format = "KORE7****YYYY";
    Year currentYear = DateUtil.getCurrentYear();

    DocumentNumberFormat documentNumberFormat =
        DocumentNumberFormat.builder().pattern(format).year(currentYear).docNumber(2).build();

    var expectedId = "KORE70002" + currentYear;

    assert (documentNumberFormat.toString().endsWith(DateUtil.getCurrentYear().toString()));
    Assertions.assertEquals(expectedId, documentNumberFormat.toString());
  }

  @Test
  void shouldCreateShortYearFormat() {
    var currentYear = DateUtil.getCurrentYear();
    String format = "BSGRE1****YY";
    DocumentNumberFormat documentNumberFormat =
        DocumentNumberFormat.builder().pattern(format).year(currentYear).docNumber(1).build();
    assert (documentNumberFormat.toString().endsWith(DateUtil.getYearAsYY(currentYear)));
  }

  @Test
  void shouldThrownExceptionWhenDocNumberOverflow() {
    Exception exception =
        assertThrows(
            DocumentNumberPatternException.class,
            () -> {
              String format = "BSGRE1****YY";
              DocumentNumberFormat documentNumberFormat =
                  DocumentNumberFormat.builder()
                      .pattern(format)
                      .year(DateUtil.getCurrentYear())
                      .docNumber(10000)
                      .build();
              documentNumberFormat.toString();
            });

    String expectedMessage = "Doc number is bigger than the * amount";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  // Todo: validation annotation dont work.
  void shouldThrownExceptionWhenDocNumberWithNegativeDocNumber() {

    DocumentNumberFormat documentNumberFormat =
        DocumentNumberFormat.builder()
            .pattern(null)
            .year(DateUtil.getCurrentYear())
            .docNumber(-4)
            .build();
  }
}
