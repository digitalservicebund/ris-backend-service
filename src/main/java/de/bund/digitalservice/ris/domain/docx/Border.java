package de.bund.digitalservice.ris.domain.docx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record Border(String color, float width, String type) {
  public String toString() {
    return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(width)
        + "px "
        + type
        + " "
        + color;
  }
}
