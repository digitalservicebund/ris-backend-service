package de.bund.digitalservice.ris.domain.docx;

public class DocUnitRunTabElement extends DocUnitTextElement implements DocUnitRunElement {
  @Override
  public String toHtmlString() {
    return "<span>&emsp;</span>";
  }

  @Override
  public String toString() {
    return "\t";
  }
}
