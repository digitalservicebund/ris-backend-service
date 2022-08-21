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
    sb.append(" style=\"");
    if (borderColor != null) {
      sb.append("border-color: ").append(borderColor).append("; ");
    }
    if (borderWidth != null) {
      sb.append("border-width: ").append(borderWidth).append("px; ");
    }
    if (borderStyle != null) {
      sb.append("border-style: ").append(borderStyle).append(";");
    }
    sb.append("\"");
  }

  private boolean hasStyle() {
    var hasStyle = borderColor != null;
    hasStyle |= borderWidth != null;
    return hasStyle;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<table");

    if (hasStyle()) {
      addStyle(sb);
    }

    sb.append(">");
    sb.append(
        rows.stream().map(DocUnitTableRowElement::toHtmlString).collect(Collectors.joining()));
    sb.append("</table>");

    return sb.toString();
  }
}
