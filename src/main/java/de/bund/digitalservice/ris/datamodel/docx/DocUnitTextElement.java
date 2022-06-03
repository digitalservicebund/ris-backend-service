package de.bund.digitalservice.ris.datamodel.docx;

import java.math.BigInteger;

public class DocUnitTextElement implements DocUnitDocx {
  private String alignment;
  private Boolean bold;
  private BigInteger size;
  private final StringBuilder text = new StringBuilder();

  public String getAlignment() {
    return alignment;
  }

  public void setAlignment(String alignment) {
    this.alignment = alignment;
  }

  public void setBold(Boolean bold) {
    this.bold = bold;
  }

  public Boolean getBold() {
    return bold;
  }

  public void setSize(BigInteger size) {
    this.size = size;
  }

  public BigInteger getSize() {
    return size;
  }

  public void addText(String text) {
    this.text.append(text);
  }

  public String getText() {
    return text.toString();
  }

  @Override
  public String toString() {
    return text.toString();
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<p");
    var hasStyle = bold != null && bold;
    hasStyle |= size != null;
    hasStyle |= alignment != null;

    if (hasStyle) {
      sb.append(" style=\"");

      if (bold != null && bold) {
        sb.append("font-weight: bold;");
      }

      if (size != null) {
        sb.append("font-size: " + size.divide(BigInteger.valueOf(2)) + "px;");
      }

      if (alignment != null) {
        sb.append("text-align: " + alignment + ";");
      }

      sb.append("\"");
    }

    sb.append(">");
    sb.append(text);
    sb.append("</p>");

    return sb.toString();
  }
}
