package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class TableCellElement extends BlockElement implements DocUnitDocx {
  public final List<DocUnitDocx> paragraphElements;
  public Integer columnSpan;

  public TableCellElement(List<DocUnitDocx> paragraphElements) {
    this.paragraphElements = paragraphElements;
    addStyle("min-width", "5px");
    addStyle("padding", "5px");
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
        + super.getStyleString()
        + ">"
        + paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining())
        + "</td>";
  }
}
