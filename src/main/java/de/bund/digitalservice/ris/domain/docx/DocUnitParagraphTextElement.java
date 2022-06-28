package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitParagraphTextElement extends DocUnitTextElement {
  private String alignment;
  private final List<DocUnitRunTextElement> runTextElements = new ArrayList<>();

  public String getAlignment() {
    return alignment;
  }

  public void setAlignment(String alignment) {
    this.alignment = alignment;
  }

  public void addRunTextElement(DocUnitRunTextElement text) {
    this.runTextElements.add(text);
  }

  public List<DocUnitRunTextElement> getRunTextElements() {
    return runTextElements;
  }

  @Override
  public String toString() {
    return runTextElements.toString();
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

    for (DocUnitRunTextElement textElement : runTextElements) {
      sb.append(textElement.toHtmlString());
    }
    sb.append("</p>");

    return sb.toString();
  }
}
