package de.bund.digitalservice.ris.domain.docx;

public class Border {
  protected String color;
  protected Integer width;
  protected String type;

  public Border(String color, Integer width, String type) {
    this.color = color;
    this.width = width;
    this.type = type;
  }

  private static Border getEmptyBorder() {
    return new Border(null, null, null);
  }
}
