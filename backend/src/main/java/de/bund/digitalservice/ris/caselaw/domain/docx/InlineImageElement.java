package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.awt.Dimension;

public class InlineImageElement extends StyledElement implements RunElement {
  private String path;
  private String contentType;
  private String alternateText;
  private Dimension size;

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setAlternateText(String alternateText) {
    this.alternateText = alternateText;
  }

  public void setSize(Dimension size) {
    this.size = size;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toHtmlString() {
    if (contentType == null || path == null) {
      return "<span style=\"color: #FF0000;\">no image information</span>";
    }

    String html =
        "<img src=\""
            + path
            + "\""
            + (alternateText != null ? " alt=\"" + alternateText + "\"" : "");

    if (size != null) {
      html += size.width > 0 ? " width=\"" + size.width + "\"" : "";
      html += size.height > 0 ? " height=\"" + size.height + "\"" : "";
    }

    html += super.getStyleString();

    html += " />";

    return html;
  }
}
