package de.bund.digitalservice.ris.domain.docx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class StyledElementTest {
  @Test
  void testAddStyle() {
    var concreteBlock = new ParagraphElement();
    concreteBlock.addStyle("background-color", "red");

    assertThat(concreteBlock.getStyleString()).isEqualTo(" style=\"background-color: red;\"");
  }

  @Test
  void testAddStyle_withOverridingStyle() {
    var concreteBlock = new ParagraphElement();
    concreteBlock.addStyle("background-color", "red");
    concreteBlock.addStyle("background-color", "blue");

    assertThat(concreteBlock.getStyleString()).isEqualTo(" style=\"background-color: blue;\"");
  }

  @Test
  void testAddStyle_withNonOverridingStyles() {
    var concreteBlock = new ParagraphElement();
    concreteBlock.addStyle("text-decoration", "underline");
    concreteBlock.addStyle("text-decoration", "line-through");
    assertThat(concreteBlock.getStyleString())
        .isEqualTo(" style=\"text-decoration: underline line-through;\"");
  }
}
