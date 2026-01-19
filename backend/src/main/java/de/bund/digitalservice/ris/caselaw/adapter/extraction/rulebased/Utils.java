package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class Utils {
  private static final Map<String, String> COURTS =
      Map.of(
          "bundesgerichtshof",
          "BGH",
          "bgh-karlsruhe",
          "BGH",
          "bundesfinanzhof",
          "BFH",
          "bundesverfassungsgericht",
          "BVerfG");

  private static final DateTimeFormatter ISO_DATE_FORMATTER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("[d. MMMM yyyy][d. MMM yyyy][dd.MM.yyyy]")
          .toFormatter(Locale.GERMAN);

  public static String getNormalizedCourt(String name) {
    if (name == null) return null;
    String norm = name.strip().replace('\u00A0', ' ');
    return COURTS.getOrDefault(norm.toLowerCase(), norm);
  }

  public static String getExtractionText(List<Extraction> extractions, String className) {
    Extraction e =
        extractions.stream()
            .filter(ex -> className.equals(ex.extractionClass()))
            .max(Comparator.comparingInt(Extraction::priority))
            .orElse(null);
    return e != null ? e.extractionText() : null;
  }

  public static String dateToIso(String dateStr) {
    return LocalDate.parse(dateStr, ISO_DATE_FORMATTER).toString();
  }
}
