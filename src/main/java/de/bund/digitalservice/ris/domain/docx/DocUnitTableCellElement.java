package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableCellElement extends Block implements DocUnitDocx {
  public final List<DocUnitDocx> paragraphElements;

  public DocUnitTableCellElement(List<DocUnitDocx> paragraphElements) {
    this.paragraphElements = paragraphElements;
    setInitialBorders();
  }

  private boolean hasStyle() {
    return hasBorder();
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder("<td");

    if (hasStyle()) {
      sb.append(" style=\"");
      sb.append(borderToHtmlString());
      sb.append("\"");
    }
    sb.append(">");
    sb.append(
        paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining()));
    sb.append("</td>");

    return sb.toString();
  }
}
