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

    ForeignLanguageVersionDTO dto =
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
    ForeignLanguageVersion result = ForeignLanguageTransformer.transformToDomain(dto);

    // Assert
    assertNotNull(result);
    assertEquals(dto.getId(), result.id());
    assertEquals(expectedLanguageCode.label(), result.languageCode().label());
    assertEquals(expectedLanguageCode.isoCode(), result.languageCode().isoCode());
    assertEquals(dto.getUrl(), result.link());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    DecisionDTO decision = new DecisionDTO();
    assertNull(ForeignLanguageTransformer.transformToDTO(null, decision));
  }

  @Test
  void transformToDTO_validDomain_transformsCorrectly() {
    // Arrange
    UUID languageCodeId = UUID.randomUUID();

    LanguageCode languageCode =
        LanguageCode.builder().id(languageCodeId).label("Englisch").isoCode("en").build();

    ForeignLanguageVersion domain =
        ForeignLanguageVersion.builder()
            .id(UUID.randomUUID())
            .languageCode(languageCode)
            .link("https://example.com")
            .build();

    LanguageCodeDTO expectedLanguageCodeDTO =
        LanguageCodeDTO.builder().id(languageCodeId).value("Englisch").isoCode("en").build();

    DecisionDTO decision = new DecisionDTO();

    // Act
    ForeignLanguageVersionDTO result = ForeignLanguageTransformer.transformToDTO(domain, decision);

    // Assert
    assertNotNull(result);
    assertEquals(domain.id(), result.getId());
    assertEquals(decision, result.getDocumentationUnit());
    assertNotNull(result.getLanguageCode());
    assertEquals(expectedLanguageCodeDTO.getId(), result.getLanguageCode().getId());
    assertEquals(domain.link(), result.getUrl());
  }
}
