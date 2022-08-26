package de.bund.digitalservice.ris.domain.docx;

import org.springframework.beans.factory.annotation.Value;

public class ErrorRunElement extends ErrorElement implements RunElement {
  @Value("error.nodes.active")
  private boolean active;

  public ErrorRunElement(String name) {
    super(name);
  }

  @Override
  public String toHtmlString() {
    if (active) {
      return "<span style=\"color: #FF0000;\">unknown run element: " + name + "</span>";
    } else {
      return "";
    }
  }

  @Override
  public String toString() {
    return "unknown run element: " + name;
  }
}
