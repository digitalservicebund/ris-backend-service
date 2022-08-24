package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableElement extends Block implements DocUnitDocx {
  public final List<DocUnitTableRowElement> rows;

  public DocUnitTableElement(List<DocUnitTableRowElement> rows) {
    this.rows = rows;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<table style=\"border-collapse: collapse;");
    sb.append(borderToHtmlString());
    sb.append(backgroundColorToHtmlString());

    sb.append("\">");
    sb.append(
        rows.stream().map(DocUnitTableRowElement::toHtmlString).collect(Collectors.joining()));
    sb.append("</table>");

    return sb.toString();
  }
}
