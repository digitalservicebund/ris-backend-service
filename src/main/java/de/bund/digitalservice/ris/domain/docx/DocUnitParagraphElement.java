package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitParagraphElement extends DocUnitTextElement {
  private String alignment;
  private final List<DocUnitRunElement> runElements = new ArrayList<>();

  public String getAlignment() {
    return alignment;
  }

  public void setAlignment(String alignment) {
    this.alignment = alignment;
  }

  public void addRunElement(DocUnitRunElement element) {
    this.runElements.add(element);
  }

  public List<DocUnitRunElement> getRunElements() {
    return runElements;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<p");

    var hasStyle = hasStyle();
    hasStyle |= alignment != null;

    if (hasStyle) {
      sb.append(" style=\"");
      addStyle(sb);

      if (alignment != null) {
        sb.append("text-align: " + alignment + ";");
      }

      sb.append("\"");
    }
    sb.append(">");

    for (DocUnitRunElement element : runElements) {
      sb.append(element.toHtmlString());
    }
    sb.append("</p>");

    return sb.toString();
  }
}
