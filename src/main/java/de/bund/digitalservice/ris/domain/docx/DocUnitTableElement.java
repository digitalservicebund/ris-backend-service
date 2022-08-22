package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableElement implements DocUnitDocx {
  public List<DocUnitTableRowElement> rows;

  String borderColor;
  Integer borderWidth;
  String borderStyle;

  public DocUnitTableElement(List<DocUnitTableRowElement> rows) {
    this.rows = rows;
  }

  public void setBorderColor(String color) {
    if (color.matches("^[0-9a-z]{6}$")) {
      this.borderColor = "#" + color;
    } else {
      this.borderColor = color;
    }
  }

  public void setBorderWidth(Integer width) {
    this.borderWidth = width;
  }

  public void setBorderStyle(String style) {
    this.borderStyle = style;
  }

  public String getBorderColor() {
    return borderColor;
  }

  public Integer getBorderWidth() {
    return borderWidth;
  }

  public String getBorderStyle() {
    return borderStyle;
  }

  void addStyle(StringBuilder sb) {
    if (hasBorder()) {
      sb.append("border: ");
      sb.append(borderWidth).append("px ");
      sb.append(borderStyle).append(" ");
      sb.append(borderColor).append("; ");
    }
  }

  private boolean hasStyle() {
    return hasBorder();
  }

  private boolean hasBorder() {
    var hasBorder = borderColor != null;
    hasBorder &= borderWidth != null;
    hasBorder &= borderStyle != null;
    return hasBorder;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<table style=\"border-collapse: collapse;");

    if (hasStyle()) {
      addStyle(sb);
    }

    sb.append("\">");
    sb.append(
        rows.stream().map(DocUnitTableRowElement::toHtmlString).collect(Collectors.joining()));
    sb.append("</table>");

    return sb.toString();
  }
}
