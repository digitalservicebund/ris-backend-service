package de.bund.digitalservice.ris.domain.docx;

import java.util.Optional;

public abstract class BlockElement extends StyledElement {
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
    addStyle("background-color", backgroundColor);
  }

  public void removeTopBorder() {
    topBorder = null;
  }

  public void removeBottomBorder() {
    bottomBorder = null;
  }

  private void setBordersStyles() {
    if (topSelfBorder != null || topBorder != null)
      addStyle("border-top", Optional.ofNullable(topSelfBorder).orElse(topBorder).toString());
    if (rightSelfBorder != null || rightBorder != null)
      addStyle("border-right", Optional.ofNullable(rightSelfBorder).orElse(rightBorder).toString());
    if (bottomSelfBorder != null || bottomBorder != null)
      addStyle(
          "border-bottom", Optional.ofNullable(bottomSelfBorder).orElse(bottomBorder).toString());
    if (leftSelfBorder != null || leftBorder != null)
      addStyle("border-left", Optional.ofNullable(leftSelfBorder).orElse(leftBorder).toString());
  }

  @Override
  public String getStyleString() {
    setBordersStyles();
    return super.getStyleString();
  }
}
