package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

class Utils {
  private static final Map<String, String> COURT_DICT =
      Map.of(
          "bundesgerichtshof",
          "BGH",
          "bgh-karlsruhe",
          "BGH",
          "bundesfinanzhof",
          "BFH",
          "bundesverfassungsgericht",
          "BVerfG");

  private static final Replacement[] COURT_REPLACEMENTS = {
    new Replacement("Bundesarbeitsgerichts?", "BAG"),
    new Replacement("Amtsgerichts?", "AG"),
    new Replacement("Landgerichts?", "LG"),
    new Replacement("Oberlandesgerichts?", "OLG"),
    new Replacement("Bundesgerichtshofs?", "BGH"),
    new Replacement("Arbeitsgerichts?", "ArbG"),
    new Replacement("Landesarbeitsgerichts?", "LAG"),
    new Replacement("Sozialgerichts?", "SG"),
    new Replacement("Landessozialgerichts?", "LSG"),
    new Replacement("Bundessozialgerichts?", "BSG"),
    new Replacement("Verwaltungsgerichtshofs?", "VGH"),
    new Replacement("Verwaltungsgerichts?", "VG"),
    new Replacement("Oberverwaltungsgerichts?", "OVG"),
    new Replacement("Bundesverwaltungsgerichts?", "BVerwG"),
    new Replacement("Finanzgerichts?", "FG"),
    new Replacement("Bundesfinanzhofs?", "BFH"),
    new Replacement("Verfassungsgerichtshofs?", "VerfGH"),
    new Replacement("Bundesverfassungsgerichts?", "BVerfG"),
  };

  private static final DateTimeFormatter ISO_DATE_FORMATTER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("[d. MMMM yyyy][d. MMM yyyy][dd.MM.yyyy]")
          .toFormatter(Locale.GERMAN);

  public static String getNormalizedCourt(String name) {
    if (name == null) return null;
    String normalizedName = name.trim().replace('\u00A0', ' ');
    for (Replacement replacement : COURT_REPLACEMENTS) {
      normalizedName =
          replacement.pattern.matcher(normalizedName).replaceAll(replacement.shortForm);
    }
    return COURT_DICT.getOrDefault(normalizedName.toLowerCase(), normalizedName);
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
    try {
      return LocalDate.parse(dateStr, ISO_DATE_FORMATTER).toString();
    } catch (Exception e) {
      return dateStr;
    }
  }

  private static class Replacement {
    final Pattern pattern;
    final String shortForm;

    Replacement(String regex, String shortForm) {
      this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
      this.shortForm = shortForm;
    }
  }
}
