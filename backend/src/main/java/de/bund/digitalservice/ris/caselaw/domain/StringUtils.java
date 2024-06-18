package de.bund.digitalservice.ris.caselaw.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

  public static boolean returnTrueIfNullOrBlank(String string) {
    return string == null || string.isBlank();
  }

  public static String normalizeSpace(String input) {
    if (input == null) {
      return null;
    }

    // List of Unicode spaces to replace with a normal space
    String[] unicodeSpaces = {
      "\u00A0", // NO-BREAK SPACE
      "\u202F", // NARROW NO-BREAK SPACE
      "\uFEFF", // ZERO WIDTH NO-BREAK SPACE
      "\u2007", // FIGURE SPACE
      "\u180E", // MONGOLIAN VOWEL SEPARATOR
      "\u2060" // WORD JOINER
    };

    String normalized = input;
    for (String unicodeSpace : unicodeSpaces) {
      normalized = normalized.replace(unicodeSpace, " ");
    }

    // Use StringUtils.normalizeSpace to handle additional normalization
    return org.apache.commons.lang3.StringUtils.normalizeSpace(normalized);
  }
}
