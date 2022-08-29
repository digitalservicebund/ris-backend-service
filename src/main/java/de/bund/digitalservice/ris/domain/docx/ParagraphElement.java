package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class ParagraphElement extends TextElement {
  private boolean clearfix;
  private final List<RunElement> runElements = new ArrayList<>();

  public void setAlignment(String alignment) {
    addStyle("text-align", alignment);
  }

  public void setClearfix(boolean clearfix) {
    this.clearfix = clearfix;
  }

  public void addRunElement(RunElement element) {
    this.runElements.add(element);
  }

  public List<RunElement> getRunElements() {
    return runElements;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder("<p");

    if (clearfix) {
      sb.append(" class=\"clearfix\"");
    }
    sb.append(getStyleString());
    sb.append(">");

    for (RunElement element : runElements) {
      sb.append(element.toHtmlString());
    }
    sb.append("</p>");

    return sb.toString();
  }
}
