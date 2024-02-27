package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.DateUtil.getYear;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.time.Year;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentNumberFormatterTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "KORE7****YYYY",
        "KVRE*****YY41",
        "WBRE61*****YY",
        "BSGRE1*****YY",
        "DSRE*****YYYY",
        "XXRE*******YY",
        "XXRE******YYY"
      })
  void shouldCreateCorrectFormat(String pattern) throws DocumentNumberFormatterException {
    Year currentYear = DateUtil.getYear();
    var prefix = pattern.substring(0, pattern.indexOf('*'));
    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(pattern)
            .year(currentYear)
            .sequenceNumber(2)
            .build();

    var result = documentNumberFormatter.generate();

    Assertions.assertEquals(13, result.length(), "Format must contain 13");
    Assertions.assertTrue(result.startsWith(prefix), " Prefix is not contained in format");
    Assertions.assertFalse(result.contains("*"), "Doc number was not parsed correctly");
    Assertions.assertTrue(
        result.contains(getYear(StringUtils.countMatches(pattern, "Y"))),
        "Year is not included in format");
  }

  @Test
  void shouldCreateShortYearFormat() throws DocumentNumberFormatterException {
    var currentYear = DateUtil.getYear();
    String format = "BSRE1******YY";
    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(format)
            .year(currentYear)
            .sequenceNumber(1)
            .build();

    var result = documentNumberFormatter.generate();

    Assertions.assertTrue(result.endsWith(getYear(currentYear, 2)));
  }

  @Test
  void shouldCreateLongYearFormat() throws DocumentNumberFormatterException {
    var currentYear = DateUtil.getYear();
    String format = "BSRE1****YYYY";
    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(format)
            .year(currentYear)
            .sequenceNumber(1)
            .build();

    var result = documentNumberFormatter.generate();

    Assertions.assertTrue(result.endsWith(getYear(currentYear, 4)));
  }

  @Test
  void shouldThrownExceptionWhenDocNumberOverflow() {
    String pattern = "BSGRE1****YY";

    DocumentNumberFormatter documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(pattern)
            .year(DateUtil.getYear())
            .sequenceNumber(10000)
            .build();

    Exception exception =
        assertThrows(DocumentNumberFormatterException.class, documentNumberFormatter::generate);

    String expectedMessage = "Doc number is bigger than the * amount";
    String actualMessage = exception.getMessage();

    Assertions.assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test // TODO: Fix lombok annotation validator.
  @Disabled("Lombok annotation validation pass through Jakarta, see RISDEV-3374")
  void shouldThrownExceptionWhenDocNumberWithNegativeDocNumber() {
    String pattern = "BSGRE1****YY";
    var documentNumberFormatter =
        DocumentNumberFormatter.builder()
            .pattern(pattern)
            .year(DateUtil.getYear())
            .sequenceNumber(-4);
    assertThatThrownBy(documentNumberFormatter::build)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Doc number must be positive");
  }
}
