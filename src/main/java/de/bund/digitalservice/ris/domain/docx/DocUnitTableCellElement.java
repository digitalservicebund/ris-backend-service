package de.bund.digitalservice.ris.domain.docx;

import java.util.List;
import java.util.stream.Collectors;

public class DocUnitTableCellElement implements DocUnitDocx {
  public List<DocUnitDocx> paragraphElements;

  public DocUnitTableCellElement(List<DocUnitDocx> paragraphElements) {
    this.paragraphElements = paragraphElements;
  }

  @Override
  public String toHtmlString() {
    return "<td>"
            + paragraphElements.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining())
            + "</td>";
  }
}
