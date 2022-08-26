package de.bund.digitalservice.ris.domain.docx;

public class AnchorImageElement extends InlineImageElement {
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
