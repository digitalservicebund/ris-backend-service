package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableRowElement implements DocUnitDocx {
  public List<DocUnitTableCellElement> cells;

  private String borderColor;
  private Integer borderWidth;
  private String borderStyle;

  public DocUnitTableRowElement(List<DocUnitTableCellElement> cells) {
    this.cells = cells;
  }

  public void setBorderColor(String color) {
    this.borderColor = color;
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

  private boolean hasBorder() {
    var hasBorder = borderColor != null;
    hasBorder &= borderWidth != null;
    hasBorder &= borderStyle != null;
    return hasBorder;
  }

  public String toHtmlString() {
    StringBuilder sb = new StringBuilder("<tr");

    if (hasBorder())
      sb.append(" style=\"border: ")
          .append(borderWidth)
          .append("px solid #")
          .append(borderColor)
          .append("\";");

    sb.append(">");
    sb.append(
        cells.stream().map(DocUnitTableCellElement::toHtmlString).collect(Collectors.joining()));
    sb.append("</tr>");

    return sb.toString();
  }
}
