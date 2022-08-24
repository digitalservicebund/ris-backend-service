package de.bund.digitalservice.ris.domain.docx;

public class BlockBorder {
  private Border top;
  private Border right;
  private Border bottom;
  private Border left;

  public void setTop(Border border) {
    if (top != null) return;
    top = border;
  }

  public void setRight(Border border) {
    if (right != null) return;
    right = border;
  }

  public void setBottom(Border border) {
    if (bottom != null) return;
    bottom = border;
  }

  public void setLeft(Border border) {
    if (left != null) return;
    left = border;
  }

  public void removeTop() {
    top = null;
  }

  public void removeBottom() {
    bottom = null;
  }

  public Boolean isSet() {
    var isSet = top != null;
    isSet |= right != null;
    isSet |= bottom != null;
    isSet |= left != null;
    return isSet;
  }

  private String toHtmlString(String position, Border border) {
    return "border-"
        + position
        + ": "
        + border.width
        + "px "
        + border.type
        + " "
        + border.color
        + ";";
  }

  public String toHtmlString() {
    var sb = new StringBuilder();

    if (top != null) sb.append(toHtmlString("top", top));
    if (right != null) sb.append(toHtmlString("right", right));
    if (bottom != null) sb.append(toHtmlString("bottom", bottom));
    if (left != null) sb.append(toHtmlString("left", left));

    return sb.toString();
  }

  public static class Border {
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
}
