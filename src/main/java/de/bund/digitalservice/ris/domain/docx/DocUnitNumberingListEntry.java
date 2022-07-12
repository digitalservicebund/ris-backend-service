package de.bund.digitalservice.ris.domain.docx;

import de.bund.digitalservice.ris.domain.docx.DocUnitNumberingList.DocUnitNumberingListNumberFormat;

public record DocUnitNumberingListEntry(
    DocUnitParagraphElement paragraphElement,
    DocUnitNumberingListNumberFormat numberFormat,
    String numId,
    String iLvl)
    implements DocUnitDocx {
  @Override
  public String toHtmlString() {
    return "<li>" + paragraphElement.toHtmlString() + "</li>";
  }
}
