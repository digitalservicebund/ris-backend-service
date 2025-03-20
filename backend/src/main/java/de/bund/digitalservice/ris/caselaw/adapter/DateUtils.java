package de.bund.digitalservice.ris.caselaw.adapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private DateUtils() {}

  public static String toDateString(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.format(DATE_FORMATTER);
  }
}
