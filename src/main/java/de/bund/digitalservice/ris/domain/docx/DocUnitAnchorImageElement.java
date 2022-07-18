package de.bund.digitalservice.ris.domain.docx;

public class DocUnitAnchorImageElement extends DocUnitInlineImageElement {
  private String floating;

  public void setFloating(String floating) {
    this.floating = floating;
  }

  @Override
  String addStyle() {
    if (floating != null) {
      return " style=\"float: " + floating + ";\"";
    }

    return "";
  }
}
