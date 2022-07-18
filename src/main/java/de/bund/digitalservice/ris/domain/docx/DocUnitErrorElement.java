package de.bund.digitalservice.ris.domain.docx;

public class DocUnitErrorElement implements DocUnitDocx {

  String name;

  public DocUnitErrorElement(String name) {
    this.name = name;
  }

  @Override
  public String toHtmlString() {
    return "<p><span style=\"color: #FF0000;\">unknown element: " + name + "</span></p>";
  }

  @Override
  public String toString() {
    return "unknown element: " + name;
  }
}
