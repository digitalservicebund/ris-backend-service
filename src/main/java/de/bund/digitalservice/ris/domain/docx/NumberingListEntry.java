package de.bund.digitalservice.ris.domain.docx;

public record NumberingListEntry(
    DocUnitDocx paragraphElement, NumberingListEntryIndex numberingListEntryIndex)
    implements DocUnitDocx {
  @Override
  public String toHtmlString() {
    return paragraphElement.toHtmlString();
  }
}
