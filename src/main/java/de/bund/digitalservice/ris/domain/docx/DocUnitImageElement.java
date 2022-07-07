package de.bund.digitalservice.ris.domain.docx;

public class DocUnitImageElement implements DocUnitRunElement {
  private String contentType;
  private String base64Representation;

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

  @Override
  public String toHtmlString() {
    if (contentType == null || base64Representation == null) {
      return "<span style=\"color: #FF0000;\">no image information</span>";
    }

    return "<img src=\"data:" + contentType + ";base64, " + base64Representation + "\" />";
  }
}
