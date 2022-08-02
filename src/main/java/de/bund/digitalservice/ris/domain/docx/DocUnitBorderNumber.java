package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitBorderNumber implements DocUnitDocx {
  private final StringBuilder number = new StringBuilder();
  private List<DocUnitParagraphElement> paragraphElements = new ArrayList<>();

  public String getNumber() {
    return number.toString();
  }

  public void addParagraphElement(DocUnitParagraphElement paragraphElement) {
    paragraphElements.add(paragraphElement);
  }

  public void addNumberText(String text) {
    number.append(text);
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<border-number>");
    sb.append("<number>");
    sb.append(number);
    sb.append("</number>");
    if (paragraphElements != null && !paragraphElements.isEmpty()) {
      sb.append("<content>");
      for (DocUnitParagraphElement textElement : paragraphElements) {
        sb.append(textElement.toHtmlString());
      }
      sb.append("</content>");
    }
    sb.append("</border-number>");

    return sb.toString();
  }
}
