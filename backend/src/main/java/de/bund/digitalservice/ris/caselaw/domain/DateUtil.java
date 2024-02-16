package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {

  public static Year getCurrentYear() {
    return Year.of(Calendar.getInstance().get(Calendar.YEAR));
  }

  public static String getYearAsYY(Year year) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
    return year.format(formatter);
  }

  public static String getYearAsYY() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
    return getCurrentYear().format(formatter);
  }
}
