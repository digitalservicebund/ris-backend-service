package de.bund.digitalservice.ris.domain.docx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public abstract class DocUnitTextElement implements DocUnitDocx {
  private static final DecimalFormat DECIMAL_FORMATTER;

  static {
    DECIMAL_FORMATTER = new DecimalFormat();
    DecimalFormatSymbols formatSymbol = DecimalFormatSymbols.getInstance();
    formatSymbol.setDecimalSeparator('.');
    DECIMAL_FORMATTER.setDecimalFormatSymbols(formatSymbol);
    DECIMAL_FORMATTER.setMaximumFractionDigits(2);
  }

  Boolean bold;
  Boolean strike;
  Integer size;
  String underline;

  public void setBold(Boolean bold) {
    this.bold = bold;
  }

  public Boolean getBold() {
    return bold;
  }

  public void setStrike(Boolean strike) {
    this.strike = strike;
  }

  public Boolean getStrike() {
    return strike;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getSize() {
    return size;
  }

  public String getUnderline() {
    return underline;
  }

  public void setUnderline(String underline) {
    this.underline = underline;
  }

  boolean hasStyle() {
    var hasStyle = bold != null && bold;
    hasStyle |= strike != null && strike;
    hasStyle |= size != null;
    hasStyle |= underline != null;
    return hasStyle;
  }

  void addStyle(StringBuilder sb) {
    if (bold != null && bold) {
      sb.append("font-weight: bold;");
    }

    if (strike != null && strike) {
      sb.append("text-decoration: line-through;");
    }

    if (size != null) {
      sb.append("font-size: ").append(DECIMAL_FORMATTER.format(size / 2.0f)).append("pt;");
    }

    if (underline != null) {
      sb.append("text-decoration: underline;");
    }
  }
}
