package de.bund.digitalservice.ris.domain.docx;

public class AnchorImageElement extends InlineImageElement {
  public void setFloating(String floating) {
    addStyle("float", floating);
  }
}
