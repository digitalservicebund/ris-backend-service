package de.bund.digitalservice.ris.domain.docx;

public abstract class BlockElement {
  private Border topSelfBorder;
  private Border rightSelfBorder;
  private Border bottomSelfBorder;
  private Border leftSelfBorder;
  private Border topBorder;
  private Border rightBorder;
  private Border bottomBorder;
  private Border leftBorder;

  private String backgroundColor;

  public void setInitialBorders(
      Border topSelf, Border rightSelf, Border bottomSelf, Border leftSelf) {
    this.topSelfBorder = topSelf;
    this.rightSelfBorder = rightSelf;
    this.bottomSelfBorder = bottomSelf;
    this.leftSelfBorder = leftSelf;
  }

  public void setInitialBorders() {
    this.topSelfBorder = null;
    this.rightSelfBorder = null;
    this.bottomSelfBorder = null;
    this.leftSelfBorder = null;
  }

  public void setTopBorder(Border border) {
    topBorder = border;
  }

  public void setRightBorder(Border border) {
    rightBorder = border;
  }

  public void setBottomBorder(Border border) {
    bottomBorder = border;
  }

  public void setLeftBorder(Border border) {
    leftBorder = border;
  }

  public void setBackgroundColor(String color) {
    this.backgroundColor = color;
  }

  public void removeTopBorder() {
    topBorder = null;
  }

  public void removeBottomBorder() {
    bottomBorder = null;
  }

  public Boolean hasBorder() {
    var isSet = topSelfBorder != null;
    isSet |= rightSelfBorder != null;
    isSet |= bottomSelfBorder != null;
    isSet |= leftSelfBorder != null;
    isSet |= topBorder != null;
    isSet |= rightBorder != null;
    isSet |= bottomBorder != null;
    isSet |= leftBorder != null;
    return isSet;
  }

  public Boolean hasBackgroundColor() {
    return backgroundColor != null;
  }

  private String bordersToHtmlString(String position, Border border) {
    if (border.width() == null || border.type() == null || border.color() == null) return "";
    return "border-"
        + position
        + ": "
        + border.width()
        + "px "
        + border.type()
        + " "
        + border.color()
        + ";";
  }

  public String bordersToHtmlString() {
    var sb = new StringBuilder();

    if (topSelfBorder != null) {
      sb.append(bordersToHtmlString("top", topSelfBorder));
    } else if (topBorder != null) {
      sb.append(bordersToHtmlString("top", topBorder));
    }

    if (rightSelfBorder != null) {
      sb.append(bordersToHtmlString("right", rightSelfBorder));
    } else if (rightBorder != null) {
      sb.append(bordersToHtmlString("right", rightBorder));
    }

    if (bottomSelfBorder != null) {
      sb.append(bordersToHtmlString("bottom", bottomSelfBorder));
    } else if (bottomBorder != null) {
      sb.append(bordersToHtmlString("bottom", bottomBorder));
    }

    if (leftSelfBorder != null) {
      sb.append(bordersToHtmlString("left", leftSelfBorder));
    } else if (leftBorder != null) {
      sb.append(bordersToHtmlString("left", leftBorder));
    }

    return sb.toString();
  }

  public String backgroundColorToHtmlString() {
    return backgroundColor != null ? "background-color: " + backgroundColor + ";" : "";
  }
}
