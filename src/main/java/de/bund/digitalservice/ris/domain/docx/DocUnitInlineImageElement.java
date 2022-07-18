package de.bund.digitalservice.ris.domain.docx;

import java.awt.Dimension;

public class DocUnitInlineImageElement implements DocUnitRunElement {
  private String contentType;
  private String base64Representation;
  private String alternateText;
  private Dimension size;

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getBase64Representation() {
    return base64Representation;
  }

  public void setBase64Representation(String base64Representation) {
    this.base64Representation = base64Representation;
  }

  public String getAlternateText() {
    return alternateText;
  }

  public void setAlternateText(String alternateText) {
    this.alternateText = alternateText;
  }

  public Dimension getSize() {
    return size;
  }

  public void setSize(Dimension size) {
    this.size = size;
  }

  String addStyle() {
    return "";
  }

  @Override
  public String toHtmlString() {
    if (contentType == null || base64Representation == null) {
      return "<span style=\"color: #FF0000;\">no image information</span>";
    }

    String html =
        "<img src=\"data:"
            + contentType
            + ";base64, "
            + base64Representation
            + "\""
            + (alternateText != null ? " alt=\"" + alternateText + "\"" : "");

    if (size != null) {
      html += size.width > 0 ? " width=\"" + size.width + "\"" : "";
      html += size.height > 0 ? " height=\"" + size.height + "\"" : "";
    }

    html += addStyle();

    html += " />";

    return html;
  }
}
