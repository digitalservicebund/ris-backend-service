package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public record DocUnitTable(List<DocUnitTableRow> rows) implements DocUnitDocx {
  @Override
  public String toHtmlString() {
    return "<table>"
        + rows.stream().map(DocUnitTableRow::toHtmlString).collect(Collectors.joining())
        + "</table>";
  }

  public record DocUnitTableRow(List<DocUnitTableCell> columns) implements DocUnitDocx {
    @Override
    public String toHtmlString() {
      return "<tr>"
          + columns.stream().map(DocUnitTableCell::toHtmlString).collect(Collectors.joining())
          + "</tr>";
    }
  }

  public record DocUnitTableCell(List<DocUnitDocx> paragraphElements) implements DocUnitDocx {
    @Override
    public String toHtmlString() {
      return "<td>"
          + paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining())
          + "</td>";
    }
  }
}
