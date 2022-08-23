package de.bund.digitalservice.ris.domain.docx;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({BlockBorder.class})
public class BlockBorderTest {
  @Test
  void testSetBottom() {
    var blockBorder = new BlockBorder();
    blockBorder.setBottom("green", 1, "solid");

    assertEquals("border-bottom: 1px solid green;", blockBorder.toHtmlString());
  }

  @Test
  void testSetRight() {
    var blockBorder = new BlockBorder();
    blockBorder.setRight("green", 1, "solid");

    assertEquals("border-right: 1px solid green;", blockBorder.toHtmlString());
  }

  @Test
  void testSetLeft() {
    var blockBorder = new BlockBorder();
    blockBorder.setLeft("green", 1, "solid");

    assertEquals("border-left: 1px solid green;", blockBorder.toHtmlString());
  }

  @Test
  void testSetTop() {
    var blockBorder = new BlockBorder();
    blockBorder.setTop("green", 1, "solid");

    assertEquals("border-top: 1px solid green;", blockBorder.toHtmlString());
  }

  @Test
  void testRemoveTop() {
    var blockBorder = new BlockBorder();
    blockBorder.setBottom("green", 1, "solid");
    blockBorder.setTop("yellow", 2, "solid");
    blockBorder.removeTop();

    assertFalse(blockBorder.toHtmlString().contains("border-top: 2px solid yellow;"));
  }

  @Test
  void testRemoveBottom() {
    var blockBorder = new BlockBorder();
    blockBorder.setBottom("green", 1, "solid");
    blockBorder.setTop("yellow", 2, "solid");
    blockBorder.removeBottom();

    assertFalse(blockBorder.toHtmlString().contains("border-bottom: 1px solid green;"));
  }

  @Test
  void testIsSet() {
    var blockBorder = new BlockBorder();
    assertFalse(blockBorder.isSet());

    blockBorder.setTop("yellow", 2, "solid");
    assertTrue(blockBorder.isSet());

    blockBorder.removeTop();
    assertFalse(blockBorder.isSet());
  }
}
