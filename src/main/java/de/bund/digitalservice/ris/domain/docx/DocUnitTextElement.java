package de.bund.digitalservice.ris.domain.docx;

import java.math.BigInteger;

public abstract class DocUnitTextElement implements DocUnitDocx {
  Boolean bold;
  Boolean strike;
  BigInteger size;
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

  public void setSize(BigInteger size) {
    this.size = size;
  }

  public BigInteger getSize() {
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
      sb.append("font-size: " + size.divide(BigInteger.valueOf(2)) + "px;");
    }

    if (underline != null) {
      sb.append("text-decoration: underline;");
    }
  }
}
