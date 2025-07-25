package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LanguageCodeDTO;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LanguageCodeTransformerTest {

  @Test
  void transformToDomain_withNullInput_returnsNull() {
    assertNull(LanguageCodeTransformer.transformToDomain(null));
  }

  @Test
  void transformToDomain_withValidDTO_transformsCorrectly() {
    // Arrange
    UUID id = UUID.randomUUID();
    LanguageCodeDTO dto = LanguageCodeDTO.builder().id(id).value("Deutsch").isoCode("de").build();

    // Act
    LanguageCode result = LanguageCodeTransformer.transformToDomain(dto);

    // Assert
    assertNotNull(result);
    assertEquals(id, result.id());
    assertEquals("Deutsch", result.label());
    assertEquals("de", result.isoCode());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    assertNull(LanguageCodeTransformer.transformToDTO(null));
  }

  @Test
  void transformToDTO_withValidDomain_transformsCorrectly() {
    // Arrange
    UUID id = UUID.randomUUID();
    LanguageCode domain = LanguageCode.builder().id(id).label("Englisch").isoCode("en").build();

    // Act
    LanguageCodeDTO result = LanguageCodeTransformer.transformToDTO(domain);

    // Assert
    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals("Englisch", result.getValue());
    assertEquals("en", result.getIsoCode());
  }
}
