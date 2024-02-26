package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.text.DecimalFormat;
import java.time.Year;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

/** A validator and generator of doc number id based on its pattern */
@Builder
@Validated
public class DocumentNumberFormatter {
  @NonNull final Year year;

  @Min(value = 0, message = "Doc number must be positive")
  int documentNumber;

  @NotEmpty
  @Size(min = 13, max = 14, message = "Pattern supports 13-14 chars only")
  final String pattern;

  public String generate() throws DocumentNumberFormatterException {
    return fillSequenceNumber(fillYear(pattern));
  }

  private String fillYear(String pattern) throws DocumentNumberFormatterException {
    int yearDigits = StringUtils.countMatches(pattern, "Y");
    if (yearDigits > 0 && yearDigits < 5)
      return pattern.replace("Y".repeat(yearDigits), DateUtil.getYear(year, yearDigits));

    throw new DocumentNumberFormatterException(
        "Y | YY | YYY | YYYY must be provided in the format");
  }

  private String fillSequenceNumber(String pattern) throws DocumentNumberFormatterException {
    int sequentialDigits = StringUtils.countMatches(pattern, "*");
    DecimalFormat decimalFormat = new DecimalFormat("0".repeat(sequentialDigits));
    String docNumberString = decimalFormat.format(documentNumber);

    if (docNumberString.length() > sequentialDigits) {
      throw new DocumentNumberFormatterException("Doc number is bigger than the * amount");
    }
    return pattern.replace("*".repeat(sequentialDigits), docNumberString);
  }
}
