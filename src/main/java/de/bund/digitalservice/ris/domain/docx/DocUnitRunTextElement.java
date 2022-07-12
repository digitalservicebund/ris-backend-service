package de.bund.digitalservice.ris.domain.docx;

public class DocUnitRunTextElement extends DocUnitTextElement implements DocUnitRunElement {
  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    var hasStyle = hasStyle();
    if (hasStyle) {
      sb.append("<span");
      sb.append(" style=\"");
      addStyle(sb);
      sb.append("\"");
      sb.append(">");
    }

    sb.append(text);

    if (hasStyle) {
      sb.append("</span>");
    }

    return sb.toString();
  }
}
