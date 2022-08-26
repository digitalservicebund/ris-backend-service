package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableCellElement extends BlockElement implements DocUnitDocx {
  public final List<DocUnitDocx> paragraphElements;
  public Integer columnSpan;

  public DocUnitTableCellElement(List<DocUnitDocx> paragraphElements) {
    this.paragraphElements = paragraphElements;
    setInitialBorders();
  }

  private boolean hasStyle() {
    var hasStyle = hasBorder();
    hasStyle |= hasBackgroundColor();
    return hasStyle;
  }

  public void setColumnSpan(Integer columnSpan) {
    this.columnSpan = columnSpan;
  }

  private String columnSpanToHtmlString() {
    return columnSpan != null ? " colspan=\"" + columnSpan + "\"" : "";
  }

  @Override
  public String toHtmlString() {

    return "<td"
        + columnSpanToHtmlString()
        + " style=\"padding: 5px; min-width: 5px;"
        + borderToHtmlString()
        + backgroundColorToHtmlString()
        + "\""
        + ">"
        + paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining())
        + "</td>";
  }
}
