package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.text.DecimalFormat;
import java.time.Year;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
public class DocumentNumberFormat {
  // TODO: Lombok annotation from unknown reason ignores the @NotEmpty annotation
  @NonNull final Year year;

  @Min(value = 0, message = "Doc number must be positive")
  int docNumber;

  @NotEmpty final String pattern;

  private String fillYear(String pattern) {
    if (pattern.contains("YYYY")) {
      return pattern.replace("YYYY", year.toString());
    } else if (pattern.contains("YY")) {
      return pattern.replace("YY", DateUtil.getYearAsYY(year));
    }
    throw new DocumentNumberPatternException("YY | YYYY must be provided in the format");
  }

  private String fillCounter(String pattern) {
    String asteriskString =
        pattern
            .chars()
            .filter(ch -> ch == '*')
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    DecimalFormat decimalFormat = new DecimalFormat(asteriskString.replace("*", "0"));
    String docNumberString = decimalFormat.format(docNumber);

    if (docNumberString.length() > asteriskString.length()) {
      throw new DocumentNumberPatternException("Doc number is bigger than the * amount");
    }
    return pattern.replace(asteriskString, docNumberString);
  }

  public String toString() {
    return fillCounter(fillYear(pattern));
  }
}
