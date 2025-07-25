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
    LanguageCodeDTO languageCodeDTO =
        LanguageCodeDTO.builder().id(UUID.randomUUID()).value("Deutsch").isoCode("de").build();

    // Act
    LanguageCode result = LanguageCodeTransformer.transformToDomain(languageCodeDTO);

    // Assert
    assertNotNull(result);
    assertEquals(languageCodeDTO.getId(), result.id());
    assertEquals(languageCodeDTO.getValue(), result.label());
    assertEquals(languageCodeDTO.getIsoCode(), result.isoCode());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    assertNull(LanguageCodeTransformer.transformToDTO(null));
  }

  @Test
  void transformToDTO_withValidDomain_transformsCorrectly() {
    // Arrange
    LanguageCode languageCode =
        LanguageCode.builder().id(UUID.randomUUID()).label("Englisch").isoCode("en").build();

    // Act
    LanguageCodeDTO languageCodeDTO = LanguageCodeTransformer.transformToDTO(languageCode);

    // Assert
    assertNotNull(languageCodeDTO);
    assertEquals(languageCode.id(), languageCodeDTO.getId());
    assertEquals(languageCode.label(), languageCodeDTO.getValue());
    assertEquals(languageCode.isoCode(), languageCodeDTO.getIsoCode());
  }
}
