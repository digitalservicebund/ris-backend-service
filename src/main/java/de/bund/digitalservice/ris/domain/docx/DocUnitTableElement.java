package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableElement implements DocUnitDocx {
  public List<DocUnitTableRowElement> rows;

  public DocUnitTableElement(List<DocUnitTableRowElement> rows) {
    this.rows = rows;
  }

  @Override
  public String toHtmlString() {
    return "<table"
        // + " style='border-collapse: collapse;'"
        + ">"
        + rows.stream().map(DocUnitTableRowElement::toHtmlString).collect(Collectors.joining())
        + "</table>";
  }
}
