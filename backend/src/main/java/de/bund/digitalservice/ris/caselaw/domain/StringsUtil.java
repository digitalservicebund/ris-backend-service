package de.bund.digitalservice.ris.caselaw.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringsUtil {

  public static boolean returnTrueIfNullOrBlank(String string) {
    return string == null || string.isBlank();
  }
}
