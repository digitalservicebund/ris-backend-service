package de.bund.digitalservice.ris.domain.docx;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class BlockElementTest {
  @Test
  void testSetTop() {
    var concreteBlock = generateConcreteBlockElement();

    concreteBlock.setInitialBorders(new Border("green", 1, "solid"), null, null, null);
    assertEquals("border-top: 1px solid green;", concreteBlock.borderToHtmlString());

    // should not overwrite self
    concreteBlock.setTopBorder(new Border("yellow", 1, "solid"));
    assertEquals("border-top: 1px solid green;", concreteBlock.borderToHtmlString());
  }

  @Test
  void testSetBottom() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setInitialBorders(null, null, new Border("green", 1, "solid"), null);
    assertEquals("border-bottom: 1px solid green;", concreteBlock.borderToHtmlString());

    // should not overwrite self
    concreteBlock.setBottomBorder(new Border("yellow", 1, "solid"));
    assertEquals("border-bottom: 1px solid green;", concreteBlock.borderToHtmlString());
  }

  @Test
  void testSetRight() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setInitialBorders(null, new Border("green", 1, "solid"), null, null);
    assertEquals("border-right: 1px solid green;", concreteBlock.borderToHtmlString());

    // should not overwrite self
    concreteBlock.setRightBorder(new Border("yellow", 1, "solid"));
    assertEquals("border-right: 1px solid green;", concreteBlock.borderToHtmlString());
  }

  @Test
  void testSetLeft() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setInitialBorders(null, null, null, new Border("green", 1, "solid"));
    assertEquals("border-left: 1px solid green;", concreteBlock.borderToHtmlString());

    // should not overwrite
    concreteBlock.setLeftBorder(new Border("yellow", 1, "solid"));
    assertEquals("border-left: 1px solid green;", concreteBlock.borderToHtmlString());
  }

  @Test
  void testSetBackgroundColor() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setBackgroundColor("#fff");
    assertEquals("background-color: #fff;", concreteBlock.backgroundColorToHtmlString());
  }

  @Test
  void testRemoveTop() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setBottomBorder(new Border("green", 1, "solid"));
    concreteBlock.setTopBorder(new Border("yellow", 2, "solid"));
    concreteBlock.removeTopBorder();

    assertFalse(concreteBlock.borderToHtmlString().contains("border-top: 2px solid yellow;"));
  }

  @Test
  void testRemoveBottom() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setBottomBorder(new Border("green", 1, "solid"));
    concreteBlock.setTopBorder(new Border("yellow", 2, "solid"));
    concreteBlock.removeBottomBorder();

    assertFalse(concreteBlock.borderToHtmlString().contains("border-bottom: 1px solid green;"));
  }

  @Test
  void testHasBorder() {
    var concreteBlock = generateConcreteBlockElement();
    assertFalse(concreteBlock.hasBorder());

    concreteBlock.setTopBorder(new Border("yellow", 2, "solid"));
    assertTrue(concreteBlock.hasBorder());

    concreteBlock.removeTopBorder();
    assertFalse(concreteBlock.hasBorder());
  }

  @Test
  void testHasBackgroundColor() {
    var concreteBlock = generateConcreteBlockElement();
    assertFalse(concreteBlock.hasBackgroundColor());

    concreteBlock.setBackgroundColor("fff");
    assertTrue(concreteBlock.hasBackgroundColor());
  }

  @Test
  void testIsSetWithSelfs() {
    var concreteBlock = generateConcreteBlockElement();
    concreteBlock.setInitialBorders(new Border("green", 1, "solid"), null, null, null);
    assertTrue(concreteBlock.hasBorder());
  }

  private BlockElement generateConcreteBlockElement() {
    var someElements = new ArrayList<DocUnitDocx>();
    someElements.add(new DocUnitNumberingList());
    return new DocUnitTableCellElement(someElements);
  }
}
