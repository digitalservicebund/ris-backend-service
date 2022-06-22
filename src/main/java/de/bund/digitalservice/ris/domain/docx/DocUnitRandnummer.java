package de.bund.digitalservice.ris.domain.docx;

public class DocUnitRandnummer implements DocUnitDocx {
  private final StringBuilder number = new StringBuilder();
  private String textContent;

  public String getNumber() {
    return number.toString();
  }

  public String getTextContent() {
    return textContent;
  }

  public void setTextContent(String textContent) {
    this.textContent = textContent;
  }

  public void addNumberText(String text) {
    number.append(text);
  }

  @Override
  public String toString() {
    return number + "" + (textContent != null ? textContent : "");
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<randnummer number=\"");
    sb.append(number);
    sb.append("\">");
    if (textContent != null) {
      sb.append(textContent);
    }
    sb.append("</randnummer>");

    return sb.toString();
  }
}
