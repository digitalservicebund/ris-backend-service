package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl.returnTrueIfInText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostgresFieldOfLawRepositoryImplTest {
  private static FieldOfLawDTO generateFieldOfLawDto(String text) {
    return FieldOfLawDTO.builder().identifier("AB-31-21").text(text).build();
  }

  static void addNormToFieldOfLaw(FieldOfLawDTO parent) {
    var fieldOfLawNorm =
        FieldOfLawNormDTO.builder()
            .id(UUID.randomUUID())
            .abbreviation("AB-33")
            .singleNormDescription("Example single norm description")
            .fieldOfLaw(parent)
            .build();

    parent.getNorms().add(fieldOfLawNorm);
  }

  static void addChildToFieldOfLaw(FieldOfLawDTO parent) {
    var child =
        FieldOfLawDTO.builder()
            .parent(parent)
            .identifier("AB-33" + "-21")
            .text("child")
            .parent(parent)
            .build();

    parent.getChildren().add(child);
  }

  @Test
  void getWithNorms() {
    var parent = generateFieldOfLawDto("parent");
    addNormToFieldOfLaw(parent);
    addChildToFieldOfLaw(parent);

    var result = PostgresFieldOfLawRepositoryImpl.getWithNormsWithoutChildren(parent);

    assertEquals(Collections.emptyList(), result.children());
    assertEquals(1, result.norms().size());
  }

  @Test
  void testReturnTrueIfInText() {
    var fieldOfLaw = generateFieldOfLawDto("find me by text");
    assertTrue(returnTrueIfInText(fieldOfLaw, new String[] {fieldOfLaw.getText()}));
    assertTrue(returnTrueIfInText(fieldOfLaw, new String[] {"find", "text"}));
  }

  @Test
  void testReturnFalseIfInText() {
    var fieldOfLaw = generateFieldOfLawDto("find me by text");
    assertFalse(returnTrueIfInText(fieldOfLaw, null));
    assertFalse(returnTrueIfInText(fieldOfLaw, new String[] {}));
    assertFalse(returnTrueIfInText(fieldOfLaw, new String[] {"no matches"}));
    assertFalse(
        returnTrueIfInText(
            fieldOfLaw,
            new String[] {fieldOfLaw.getIdentifier(), fieldOfLaw.getText(), "no matches"}));
  }
}
