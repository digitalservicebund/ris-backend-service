package de.bund.digitalservice.ris.domain.docx;

public abstract class Block {
  private Border topSelf;
  private Border rightSelf;
  private Border bottomSelf;
  private Border leftSelf;
  private Border top;
  private Border right;
  private Border bottom;
  private Border left;

  public void setInitialBorders(
      Border topSelf, Border rightSelf, Border bottomSelf, Border leftSelf) {
    this.topSelf = topSelf;
    this.rightSelf = rightSelf;
    this.bottomSelf = bottomSelf;
    this.leftSelf = leftSelf;
  }

  public void setInitialBorders() {
    this.topSelf = null;
    this.rightSelf = null;
    this.bottomSelf = null;
    this.leftSelf = null;
  }

  public void setTopBorder(Border border) {
    top = border;
  }

  public void setRightBorder(Border border) {
    right = border;
  }

  public void setBottomBorder(Border border) {
    bottom = border;
  }

  public void setLeftBorder(Border border) {
    left = border;
  }

  public void removeTopBorder() {
    top = null;
  }

  public void removeBottomBorder() {
    bottom = null;
  }

  public Boolean hasBorder() {
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

  private String borderToHtmlString(String position, Border border) {
    if (border.width == null || border.type == null || border.color == null) return "";
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

  public String borderToHtmlString() {
    var sb = new StringBuilder();

    if (topSelf != null) {
      sb.append(borderToHtmlString("top", topSelf));
    } else if (top != null) {
      sb.append(borderToHtmlString("top", top));
    }

    if (rightSelf != null) {
      sb.append(borderToHtmlString("right", rightSelf));
    } else if (right != null) {
      sb.append(borderToHtmlString("right", right));
    }

    if (bottomSelf != null) {
      sb.append(borderToHtmlString("bottom", bottomSelf));
    } else if (bottom != null) {
      sb.append(borderToHtmlString("bottom", bottom));
    }

    if (leftSelf != null) {
      sb.append(borderToHtmlString("left", leftSelf));
    } else if (left != null) {
      sb.append(borderToHtmlString("left", left));
    }

    return sb.toString();
  }
}
