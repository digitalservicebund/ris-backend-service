package de.bund.digitalservice.ris.caselaw.adapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter DATE_DISPLAY_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private DateUtils() {}

  public static String toDateString(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.format(DATE_FORMATTER);
  }

  public static String toFormattedDateString(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.format(DATE_DISPLAY_FORMATTER);
  }
}
