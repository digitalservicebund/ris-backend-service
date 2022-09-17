package de.bund.digitalservice.ris.domain.docx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public abstract class TextElement extends StyledElement implements DocumentUnitDocx {
  private static final DecimalFormat DECIMAL_FORMATTER;

  static {
    DECIMAL_FORMATTER = new DecimalFormat();
    DecimalFormatSymbols formatSymbol = DecimalFormatSymbols.getInstance();
    formatSymbol.setDecimalSeparator('.');
    DECIMAL_FORMATTER.setDecimalFormatSymbols(formatSymbol);
    DECIMAL_FORMATTER.setMaximumFractionDigits(2);
  }

  public void setBold(Boolean bold) {
    if (bold) addStyle("font-weight", "bold");
  }

  public void setItalic(Boolean italic) {
    if (italic) addStyle("font-style", "italic");
  }

  public void setStrike(Boolean strike) {
    if (strike) addStyle("text-decoration", "line-through");
  }

  public void setSize(Integer size) {
    addStyle("font-size", DECIMAL_FORMATTER.format(size / 2.0f) + "pt");
  }

  public void setUnderline(String underline) {
    if (underline.equals("single")) addStyle("text-decoration", "underline");
  }

  public void setVertAlign(VerticalAlign vertAlign) {
    if (vertAlign == VerticalAlign.SUBSCRIPT) {
      addStyle("vertical-align", "sub");
    } else if (vertAlign == VerticalAlign.SUPERSCRIPT) {
      addStyle("vertical-align", "super");
    }
  }
}
