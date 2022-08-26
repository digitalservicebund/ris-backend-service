package de.bund.digitalservice.ris.domain.docx;

public class RunTabElement extends TextElement implements RunElement {
  @Override
  public String toHtmlString() {
    return "<span>&emsp;</span>";
  }

  @Override
  public String toString() {
    return "\t";
  }
}
