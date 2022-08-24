package de.bund.digitalservice.ris.domain.docx;

public class BlockBorder {
  private final Border topSelf;
  private final Border rightSelf;
  private final Border bottomSelf;
  private final Border leftSelf;
  private Border top;
  private Border right;
  private Border bottom;
  private Border left;

  public BlockBorder(Border topSelf, Border rightSelf, Border bottomSelf, Border leftSelf) {
    this.topSelf = topSelf;
    this.rightSelf = rightSelf;
    this.bottomSelf = bottomSelf;
    this.leftSelf = leftSelf;
  }

  public BlockBorder() {
    this.topSelf = null;
    this.rightSelf = null;
    this.bottomSelf = null;
    this.leftSelf = null;
  }

  public void setTop(Border border) {
    top = border;
  }

  public void setRight(Border border) {
    right = border;
  }

  public void setBottom(Border border) {
    bottom = border;
  }

  public void setLeft(Border border) {
    left = border;
  }

  public void removeTop() {
    top = null;
  }

  public void removeBottom() {
    bottom = null;
  }

  public Boolean isSet() {
    var isSet = topSelf != null;
    isSet |= rightSelf != null;
    isSet |= bottomSelf != null;
    isSet |= leftSelf != null;
    isSet |= top != null;
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

    if (topSelf != null) {
      sb.append(toHtmlString("top", topSelf));
    } else if (top != null) {
      sb.append(toHtmlString("top", top));
    }

    if (rightSelf != null) {
      sb.append(toHtmlString("right", rightSelf));
    } else if (right != null) {
      sb.append(toHtmlString("right", right));
    }

    if (bottomSelf != null) {
      sb.append(toHtmlString("bottom", bottomSelf));
    } else if (bottom != null) {
      sb.append(toHtmlString("bottom", bottom));
    }

    if (leftSelf != null) {
      sb.append(toHtmlString("left", leftSelf));
    } else if (left != null) {
      sb.append(toHtmlString("left", left));
    }

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
