package de.bund.digitalservice.ris.domain.docx;

import org.springframework.beans.factory.annotation.Value;

public class DocUnitErrorElement implements DocUnitDocx {
  @Value("${error.nodes.active}")
  private boolean active;

  String name;

  public DocUnitErrorElement(String name) {
    this.name = name;
  }

  @Override
  public String toHtmlString() {
    if (active) {
      return "<p><span style=\"color: #FF0000;\">unknown element: " + name + "</span></p>";
    } else {
      return "";
    }
  }

  @Override
  public String toString() {
    return "unknown element: " + name;
  }
}
