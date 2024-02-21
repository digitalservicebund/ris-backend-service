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

@Builder
@Validated
public class DocumentNumberFormatter {
  // TODO: Lombok annotation from unknown reason ignores the @NotEmpty annotation
  @NonNull final Year year;

  @Min(value = 0, message = "Doc number must be positive")
  int docNumber;

  @NotEmpty
  @Size(min = 13, max = 14, message = "Pattern support 13 chars only")
  final String pattern;

  public String generate() throws DocumentNumberFormatterException {
    return fillCounter(fillYear(pattern));
  }

  private String fillYear(String pattern) throws DocumentNumberFormatterException {
    int yearDigits = StringUtils.countMatches(pattern, "Y");
    if (yearDigits > 0 && yearDigits < 5)
      return pattern.replace("Y".repeat(yearDigits), DateUtil.getYear(year, yearDigits));

    throw new DocumentNumberFormatterException(
        "Y | YY | YYY | YYYY must be provided in the format");
  }

  private String fillCounter(String pattern) throws DocumentNumberFormatterException {
    String asteriskString =
        pattern
            .chars()
            .filter(ch -> ch == '*')
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    DecimalFormat decimalFormat = new DecimalFormat(asteriskString.replace("*", "0"));
    String docNumberString = decimalFormat.format(docNumber);

    if (docNumberString.length() > asteriskString.length()) {
      throw new DocumentNumberFormatterException("Doc number is bigger than the * amount");
    }
    return pattern.replace(asteriskString, docNumberString);
  }
}
