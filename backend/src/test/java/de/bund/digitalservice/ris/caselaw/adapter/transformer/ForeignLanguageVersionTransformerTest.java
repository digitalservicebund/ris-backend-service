package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ForeignLanguageVersionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LanguageCodeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ForeignLanguageVersionTransformerTest {

  @Test
  void transformToDomain_withNullInput_returnsNull() {
    assertNull(ForeignLanguageTransformer.transformToDomain(null));
  }

  @Test
  void transformToDomain_withValidDTO_transformsCorrectly() {
    // Arrange
    LanguageCodeDTO languageCodeDTO =
        LanguageCodeDTO.builder().id(UUID.randomUUID()).value("Englisch").isoCode("en").build();

    ForeignLanguageVersionDTO foreignLanguageVersionDTO =
        ForeignLanguageVersionDTO.builder()
            .id(UUID.randomUUID())
            .languageCode(languageCodeDTO)
            .url("https://example.com")
            .build();

    LanguageCode expectedLanguageCode =
        LanguageCode.builder()
            .id(languageCodeDTO.getId())
            .label(languageCodeDTO.getValue())
            .isoCode(languageCodeDTO.getIsoCode())
            .build();

    // Act
    ForeignLanguageVersion foreignLanguageVersion =
        ForeignLanguageTransformer.transformToDomain(foreignLanguageVersionDTO);

    // Assert
    assertNotNull(foreignLanguageVersion);
    assertEquals(foreignLanguageVersionDTO.getId(), foreignLanguageVersion.id());
    assertEquals(expectedLanguageCode.label(), foreignLanguageVersion.languageCode().label());
    assertEquals(expectedLanguageCode.isoCode(), foreignLanguageVersion.languageCode().isoCode());
    assertEquals(foreignLanguageVersionDTO.getUrl(), foreignLanguageVersion.link());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    DecisionDTO decision = new DecisionDTO();
    assertNull(ForeignLanguageTransformer.transformToDTO(null, 1));
  }

  @Test
  void transformToDTO_validDomain_transformsCorrectly() {
    // Arrange
    LanguageCode languageCode =
        LanguageCode.builder().id(UUID.randomUUID()).label("Englisch").isoCode("en").build();

    ForeignLanguageVersion foreignLanguageVersion =
        ForeignLanguageVersion.builder()
            .id(UUID.randomUUID())
            .languageCode(languageCode)
            .link("https://example.com")
            .build();

    LanguageCodeDTO expectedLanguageCodeDTO =
        LanguageCodeDTO.builder().id(languageCode.id()).value("Englisch").isoCode("en").build();

    // Act
    ForeignLanguageVersionDTO foreignLanguageVersionDTO =
        ForeignLanguageTransformer.transformToDTO(foreignLanguageVersion, 1);

    // Assert
    assertNotNull(foreignLanguageVersionDTO);
    assertEquals(foreignLanguageVersion.id(), foreignLanguageVersionDTO.getId());
    assertNotNull(foreignLanguageVersionDTO.getLanguageCode());
    assertEquals(
        expectedLanguageCodeDTO.getId(), foreignLanguageVersionDTO.getLanguageCode().getId());
    assertEquals(foreignLanguageVersion.link(), foreignLanguageVersionDTO.getUrl());
  }
}
