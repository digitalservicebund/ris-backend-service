package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableCellElement implements DocUnitDocx {
  public List<DocUnitDocx> paragraphElements;

  private String borderColor;
  private Integer borderWidth;
  private String borderStyle;

  public DocUnitTableCellElement(List<DocUnitDocx> paragraphElements) {
    this.paragraphElements = paragraphElements;
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

  private boolean hasBorder() {
    var hasBorder = borderColor != null;
    hasBorder &= borderWidth != null;
    hasBorder &= borderStyle != null;
    return hasBorder;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder("<td");

    if (hasBorder())
      sb.append(" style=\"border: ")
          .append(borderWidth)
          .append("px solid #")
          .append(borderColor)
          .append("\";");

    sb.append(">");
    sb.append(
        paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining()));
    sb.append("</td>");

    return sb.toString();
  }
}
