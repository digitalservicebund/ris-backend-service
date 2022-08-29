package de.bund.digitalservice.ris.domain.docx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public abstract class TextElement extends StyledElement implements DocUnitDocx {
  private static final DecimalFormat DECIMAL_FORMATTER;

  static {
    DECIMAL_FORMATTER = new DecimalFormat();
    DecimalFormatSymbols formatSymbol = DecimalFormatSymbols.getInstance();
    formatSymbol.setDecimalSeparator('.');
    DECIMAL_FORMATTER.setDecimalFormatSymbols(formatSymbol);
    DECIMAL_FORMATTER.setMaximumFractionDigits(2);
  }

  Boolean bold;
  Boolean italic;
  Boolean strike;
  VerticalAlign vertAlign;
  Integer size;
  String underline;

  public void setBold(Boolean bold) {
    this.bold = bold;
    if (bold) addStyle("font-weight", "bold");
  }

  public Boolean getBold() {
    return bold;
  }

  public void setItalic(Boolean italic) {
    this.italic = italic;
    if (italic) addStyle("font-style", "italic");
  }

  public Boolean getItalic() {
    return italic;
  }

  public void setStrike(Boolean strike) {
    this.strike = strike;
    if (strike) addStyle("text-decoration", "line-through");
  }

  public Boolean getStrike() {
    return strike;
  }

  public void setSize(Integer size) {
    this.size = size;
    addStyle("font-size", DECIMAL_FORMATTER.format(size / 2.0f) + "pt");
  }

  public Integer getSize() {
    return size;
  }

  public void setUnderline(String underline) {
    this.underline = underline;
    addStyle("text-decoration", "underline");
  }

  public String getUnderline() {
    return underline;
  }

  public void setVertAlign(VerticalAlign vertAlign) {
    this.vertAlign = vertAlign;
    if (vertAlign == VerticalAlign.SUBSCRIPT) {
      addStyle("vertical-align", "sub");
    } else if (vertAlign == VerticalAlign.SUPERSCRIPT) {
      addStyle("vertical-align", "super");
    }
  }

  public VerticalAlign getVertAlign() {
    return vertAlign;
  }
}
