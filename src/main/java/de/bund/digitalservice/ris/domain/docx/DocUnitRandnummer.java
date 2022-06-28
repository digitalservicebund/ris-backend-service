package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitRandnummer implements DocUnitDocx {
  private final StringBuilder number = new StringBuilder();
  private List<DocUnitParagraphTextElement> textElements = new ArrayList<>();

  public String getNumber() {
    return number.toString();
  }

  public void addParagraphTextElement(DocUnitParagraphTextElement textElement) {
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
    for (DocUnitParagraphTextElement textElement : textElements) {
      sb.append(textElement.toString());
    }
    return sb.toString();
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<randnummer number=\"");
    sb.append(number);
    sb.append("\">");
    for (DocUnitParagraphTextElement textElement : textElements) {
      sb.append(textElement.toHtmlString());
    }
    sb.append("</randnummer>");

    return sb.toString();
  }
}
