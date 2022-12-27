package de.bund.digitalservice.ris.caselaw.domain.docx;

public class AnchorImageElement extends InlineImageElement {
  public void setFloating(String floating) {
    addStyle("float", floating);
  }
}
