package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class ParagraphElement extends TextElement {
  private String alignment;
  private boolean clearfix;
  private final List<RunElement> runElements = new ArrayList<>();

  public String getAlignment() {
    return alignment;
  }

  public void setAlignment(String alignment) {
    this.alignment = alignment;
  }

  public boolean isClearfix() {
    return clearfix;
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
    StringBuilder sb = new StringBuilder();

    sb.append("<p");

    var hasStyle = hasStyle();
    hasStyle |= alignment != null;

    if (clearfix) {
      sb.append(" class=\"clearfix\"");
    }

    if (hasStyle) {
      sb.append(" style=\"");
      addStyle(sb);

      if (alignment != null) {
        sb.append("text-align: ").append(alignment).append(";");
      }

      sb.append("\"");
    }
    sb.append(">");

    for (RunElement element : runElements) {
      sb.append(element.toHtmlString());
    }
    sb.append("</p>");

    return sb.toString();
  }
}
