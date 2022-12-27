package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public record TableRowElement(List<TableCellElement> cells) implements DocumentUnitDocx {
  public String toHtmlString() {
    return "<tr>"
        + cells.stream().map(TableCellElement::toHtmlString).collect(Collectors.joining())
        + "</tr>";
  }
}
