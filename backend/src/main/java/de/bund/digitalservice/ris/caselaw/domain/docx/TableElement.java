package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class TableElement extends BlockElement implements DocumentationUnitDocx {
  public final List<TableRowElement> rows;
  public final List<Integer> columnWidths;

  public TableElement(List<TableRowElement> rows, List<Integer> columnWidths) {
    this.rows = rows;
    this.columnWidths = columnWidths;
    addStyle("border-collapse", "collapse");
    addStyle("table-layout", "fixed");
  }

  @Override
  public String toHtmlString() {
    return "<table"
        + super.getStyleString()
        + ">"
        + getHtmlColumnWidths()
        + rows.stream().map(TableRowElement::toHtmlString).collect(Collectors.joining())
        + "</table>";
  }

  private String getHtmlColumnWidths() {
    String colgroupTags = "";

    if (columnWidths != null) {
      colgroupTags += "<colgroup>";
      colgroupTags +=
          columnWidths.stream()
              .map(
                  colWidth -> {
                    if (colWidth < 35) {
                      colWidth = 35;
                    }
                    return "<col style=\"width: " + colWidth + "px;\" />";
                  })
              .collect(Collectors.joining());
      colgroupTags += "</colgroup>";
    }

    return colgroupTags;
  }
}
