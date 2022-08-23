package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableRowElement implements DocUnitDocx {
  public List<DocUnitTableCellElement> cells;

  public DocUnitTableRowElement(List<DocUnitTableCellElement> cells) {
    this.cells = cells;
  }

  public String toHtmlString() {
    return "<tr>"
        + cells.stream().map(DocUnitTableCellElement::toHtmlString).collect(Collectors.joining())
        + "</tr>";
  }
}
