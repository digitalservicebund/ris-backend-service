package de.bund.digitalservice.ris.domain.docx;

public record NumberingListEntry(
    DocumentUnitDocx paragraphElement, NumberingListEntryIndex numberingListEntryIndex)
    implements DocumentUnitDocx {
  @Override
  public String toHtmlString() {
    return paragraphElement.toHtmlString();
  }
}
