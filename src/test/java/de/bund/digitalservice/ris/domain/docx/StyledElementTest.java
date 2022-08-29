package de.bund.digitalservice.ris.domain.docx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
  }
}
