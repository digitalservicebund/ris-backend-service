package de.bund.digitalservice.ris.domain.docx;

public class RunTabElement extends TextElement implements RunElement {
  @Override
  public String toHtmlString() {
    return "&nbsp;&nbsp;&nbsp;&nbsp;";
  }

  @Override
  public String toString() {
    return "\t";
  }
}
