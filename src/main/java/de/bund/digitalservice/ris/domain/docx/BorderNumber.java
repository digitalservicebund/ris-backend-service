package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class BorderNumber implements DocUnitDocx {
  private final StringBuilder number = new StringBuilder();
  private final List<ParagraphElement> paragraphElements = new ArrayList<>();

  public String getNumber() {
    return number.toString();
  }

  public void addParagraphElement(ParagraphElement paragraphElement) {
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
    if (!paragraphElements.isEmpty()) {
      sb.append("<content>");
      for (ParagraphElement textElement : paragraphElements) {
        sb.append(textElement.toHtmlString());
      }
      sb.append("</content>");
    }
    sb.append("</border-number>");

    return sb.toString();
  }
}
