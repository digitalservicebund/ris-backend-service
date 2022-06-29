package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitBorderNumber implements DocUnitDocx {
  private final StringBuilder number = new StringBuilder();
  private List<DocUnitParagraphElement> textElements = new ArrayList<>();

  public String getNumber() {
    return number.toString();
  }

  public void addParagraphTextElement(DocUnitParagraphElement textElement) {
    textElements.add(textElement);
  }

  public void addNumberText(String text) {
    number.append(text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(number);
    sb.append(" ");
    for (DocUnitParagraphElement textElement : textElements) {
      sb.append(textElement.toString());
    }
    return sb.toString();
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<border-number number=\"");
    sb.append(number);
    sb.append("\">");
    for (DocUnitParagraphElement textElement : textElements) {
      sb.append(textElement.toHtmlString());
    }
    sb.append("</border-number>");

    return sb.toString();
  }
}
