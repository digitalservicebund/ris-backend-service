package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class TableRowElement implements DocUnitDocx {
  public final List<TableCellElement> cells;

  public TableRowElement(List<TableCellElement> cells) {
    this.cells = cells;
  }

  public String toHtmlString() {
    return "<tr>"
        + cells.stream().map(TableCellElement::toHtmlString).collect(Collectors.joining())
        + "</tr>";
  }
}
