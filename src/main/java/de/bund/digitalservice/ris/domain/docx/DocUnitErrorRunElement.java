package de.bund.digitalservice.ris.domain.docx;

public class DocUnitErrorRunElement extends DocUnitErrorElement implements DocUnitRunElement {
  public DocUnitErrorRunElement(String name) {
    super(name);
  }

  @Override
  public String toHtmlString() {
    return "<span style=\"color: #FF0000;\">unknown run element: " + name + "</span>";
  }

  @Override
  public String toString() {
    return "unknown run element: " + name;
  }
}
