package de.bund.digitalservice.ris.domain.docx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StyledElementTest {
  @Test
  void testAddStyle() {
    var concreteBlock = new ParagraphElement();
    concreteBlock.addStyle("background-color", "red");
    assertEquals(" style=\"background-color: red;\"", concreteBlock.getStyleString());

    // overwrite property
    concreteBlock.addStyle("background-color", "blue");
    assertFalse(concreteBlock.getStyleString().contains("red"));
    assertEquals(" style=\"background-color: blue;\"", concreteBlock.getStyleString());

    // don't overwrite property for text-decoration
    concreteBlock.addStyle("text-decoration", "underline");
    concreteBlock.addStyle("text-decoration", "line-through");
    assertTrue(concreteBlock.getStyleString().contains("underline"));
    assertEquals(
        " style=\"background-color: blue;text-decoration: underline line-through;\"",
        concreteBlock.getStyleString());
  }
}
