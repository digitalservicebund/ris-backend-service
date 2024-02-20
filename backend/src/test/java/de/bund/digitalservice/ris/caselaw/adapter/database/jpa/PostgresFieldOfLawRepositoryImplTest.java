package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresFieldOfLawRepositoryImpl.returnTrueIfInTextOrIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostgresFieldOfLawRepositoryImplTest {
  private static FieldOfLawDTO generateFieldOfLawDto(String text) {
    return FieldOfLawDTO.builder().identifier("AB-31-21").text(text).build();
  }

  static FieldOfLawNormDTO addFieldOfNormDto(FieldOfLawDTO parent) {
    var fieldOfLawNorm =
        FieldOfLawNormDTO.builder()
            .id(UUID.randomUUID())
            .abbreviation("AB" + "-33")
            .singleNormDescription("Example single norm description")
            .fieldOfLaw(parent)
            .build();

    parent.getNorms().add(fieldOfLawNorm);
    return fieldOfLawNorm;
  }

  static void addFieldOfChildDto(FieldOfLawDTO parent) {
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
    addFieldOfNormDto(parent);
    addFieldOfChildDto(parent);

    var result = PostgresFieldOfLawRepositoryImpl.getWithNormsWithoutChildren(parent);

    assertEquals(Collections.emptyList(), result.children());
    assertEquals(1, result.norms().size());
  }

  @Test
  void testReturnTrueIfInTextOrIdentifier() {
    var fieldOfLaw = generateFieldOfLawDto("find me by text");
    assertTrue(returnTrueIfInTextOrIdentifier(fieldOfLaw, new String[] {fieldOfLaw.getText()}));

    assertTrue(
        returnTrueIfInTextOrIdentifier(fieldOfLaw, new String[] {fieldOfLaw.getIdentifier()}));

    assertFalse(returnTrueIfInTextOrIdentifier(fieldOfLaw, new String[] {"no matches"}));
  }
}
