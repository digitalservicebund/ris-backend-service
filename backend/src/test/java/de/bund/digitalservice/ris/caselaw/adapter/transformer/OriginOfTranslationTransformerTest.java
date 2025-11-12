package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LanguageCodeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationBorderNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationTranslatorDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationUrlDTO;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.OriginOfTranslation;
import de.bund.digitalservice.ris.caselaw.domain.TranslationType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OriginOfTranslationTransformerTest {
  @Nested
  class TransformToDTO {
    @Test
    void transformToDTO_withNullInput_returnsNull() {
      DecisionDTO decision = new DecisionDTO();
      assertNull(OriginOfTranslationTransformer.transformToDTO(decision, null, 0));
    }

    @Test
    void transformToDTO_withFullValidDomain_transformsCorrectly() {
      // Arrange
      DecisionDTO decision = new DecisionDTO();
      LanguageCode languageCode =
          LanguageCode.builder().id(UUID.randomUUID()).label("Deutsch").isoCode("de").build();
      OriginOfTranslation domain =
          OriginOfTranslation.builder()
              .id(UUID.randomUUID())
              .languageCode(languageCode)
              .translationType(TranslationType.AMTLICH)
              .translators(List.of("Translator1", "Translator2"))
              .borderNumbers(List.of(13L, 24L))
              .urls(List.of("https://url1.com", "https://url2.com"))
              .build();

      // Act
      OriginOfTranslationDTO dto =
          OriginOfTranslationTransformer.transformToDTO(decision, domain, 0);

      // Assert
      Assertions.assertNotNull(dto);
      Assertions.assertEquals(domain.id(), dto.getId());
      Assertions.assertEquals(domain.translationType(), dto.getTranslationType());
      Assertions.assertNotNull(dto.getLanguageCode());
      Assertions.assertEquals(domain.languageCode().id(), dto.getLanguageCode().getId());
      Assertions.assertEquals(1L, dto.getRank());

      // Assert translators
      assertThat(dto.getTranslators())
          .hasSize(2)
          .extracting("translatorName", "rank")
          .containsExactly(tuple("Translator1", 1L), tuple("Translator2", 2L));

      // Assert border numbers
      assertThat(dto.getBorderNumbers())
          .hasSize(2)
          .extracting("borderNumber", "rank")
          .containsExactly(tuple(13L, 1L), tuple(24L, 2L));

      // Assert External Links
      assertThat(dto.getUrls())
          .hasSize(2)
          .extracting("url", "rank")
          .containsExactly(tuple("https://url1.com", 1L), tuple("https://url2.com", 2L));
    }

    @Test
    void transformToDTO_withoutSubLists_transformsToEmptyList() {
      // Arrange
      DecisionDTO decision = new DecisionDTO();

      OriginOfTranslation domain = OriginOfTranslation.builder().id(UUID.randomUUID()).build();

      // Act
      OriginOfTranslationDTO dto =
          OriginOfTranslationTransformer.transformToDTO(decision, domain, 0);

      // Assert
      Assertions.assertTrue(dto.getTranslators().isEmpty());
      Assertions.assertTrue(dto.getBorderNumbers().isEmpty());
      Assertions.assertTrue(dto.getUrls().isEmpty());
    }

    @Test
    void transformToDTO_withNullSubLists_transformsToEmptyList() {
      // Arrange
      DecisionDTO decision = new DecisionDTO();

      OriginOfTranslation domain =
          OriginOfTranslation.builder()
              .id(UUID.randomUUID())
              .translators(null)
              .borderNumbers(null)
              .urls(null)
              .build();

      // Act
      OriginOfTranslationDTO dto =
          OriginOfTranslationTransformer.transformToDTO(decision, domain, 0);

      // Assert
      Assertions.assertTrue(dto.getTranslators().isEmpty());
      Assertions.assertTrue(dto.getBorderNumbers().isEmpty());
      Assertions.assertTrue(dto.getUrls().isEmpty());
    }

    @Test
    void transformToDTO_withEmptySubLists_transformsToEmptyList() {
      // Arrange
      DecisionDTO decision = new DecisionDTO();

      OriginOfTranslation domain =
          OriginOfTranslation.builder()
              .id(UUID.randomUUID())
              .translators(List.of())
              .borderNumbers(List.of())
              .urls(List.of())
              .build();

      // Act
      OriginOfTranslationDTO dto =
          OriginOfTranslationTransformer.transformToDTO(decision, domain, 0);

      // Assert
      Assertions.assertTrue(dto.getTranslators().isEmpty());
      Assertions.assertTrue(dto.getBorderNumbers().isEmpty());
      Assertions.assertTrue(dto.getUrls().isEmpty());
    }
  }

  @Nested
  class TransformToDomain {
    @Test
    void transformToDomain_withNullInput_returnsNull() {
      assertNull(OriginOfTranslationTransformer.transformToDomain(null));
    }

    @Test
    void transformToDomain_withFullValidDTO_transformsCorrectly() {
      // Arrange
      UUID id = UUID.randomUUID();
      LanguageCodeDTO languageCodeDTO =
          LanguageCodeDTO.builder().id(UUID.randomUUID()).value("Deutsch").isoCode("de").build();

      OriginOfTranslationTranslatorDTO translatorDTO1 =
          OriginOfTranslationTranslatorDTO.builder().translatorName("Translator1").build();
      OriginOfTranslationTranslatorDTO translatorDTO2 =
          OriginOfTranslationTranslatorDTO.builder().translatorName("Translator2").build();

      OriginOfTranslationBorderNumberDTO borderNumberDTO1 =
          OriginOfTranslationBorderNumberDTO.builder().borderNumber(13L).build();
      OriginOfTranslationBorderNumberDTO borderNumberDTO2 =
          OriginOfTranslationBorderNumberDTO.builder().borderNumber(24L).build();

      OriginOfTranslationUrlDTO urlDTO1 =
          OriginOfTranslationUrlDTO.builder().url("https://url1.com").build();
      OriginOfTranslationUrlDTO urlDTO2 =
          OriginOfTranslationUrlDTO.builder().url("https://url2.com").build();

      OriginOfTranslationDTO dto =
          OriginOfTranslationDTO.builder()
              .id(id)
              .languageCode(languageCodeDTO)
              .translationType(TranslationType.AMTLICH)
              .translators(List.of(translatorDTO1, translatorDTO2))
              .borderNumbers(List.of(borderNumberDTO1, borderNumberDTO2))
              .urls(List.of(urlDTO1, urlDTO2))
              .build();

      // Act
      OriginOfTranslation domain = OriginOfTranslationTransformer.transformToDomain(dto);

      // Assert
      Assertions.assertNotNull(domain);
      Assertions.assertEquals(dto.getId(), domain.id());
      Assertions.assertEquals(TranslationType.AMTLICH, domain.translationType());
      Assertions.assertNotNull(domain.languageCode());
      Assertions.assertEquals(languageCodeDTO.getId(), domain.languageCode().id());

      // Assert translators
      assertThat(domain.translators()).hasSize(2).containsExactly("Translator1", "Translator2");

      // Assert border numbers
      assertThat(domain.borderNumbers()).hasSize(2).containsExactly(13L, 24L);

      // Assert External Links
      assertThat(domain.urls()).hasSize(2).containsExactly("https://url1.com", "https://url2.com");
    }

    @Test
    void transformToDomain_withoutSubLists_transformsToEmptyLists() {
      // Arrange
      OriginOfTranslationDTO dto = OriginOfTranslationDTO.builder().id(UUID.randomUUID()).build();

      // Act
      OriginOfTranslation domain = OriginOfTranslationTransformer.transformToDomain(dto);

      // Assert
      Assertions.assertTrue(domain.translators().isEmpty());
      Assertions.assertTrue(domain.borderNumbers().isEmpty());
      Assertions.assertTrue(domain.urls().isEmpty());
    }

    @Test
    void transformToDomain_withNullSubLists_transformsToEmptyLists() {
      // Arrange
      OriginOfTranslationDTO dto =
          OriginOfTranslationDTO.builder()
              .id(UUID.randomUUID())
              .translators(null)
              .borderNumbers(null)
              .urls(null)
              .build();

      // Act
      OriginOfTranslation domain = OriginOfTranslationTransformer.transformToDomain(dto);

      // Assert
      Assertions.assertTrue(domain.translators().isEmpty());
      Assertions.assertTrue(domain.borderNumbers().isEmpty());
      Assertions.assertTrue(domain.urls().isEmpty());
    }

    @Test
    void transformToDomain_withEmptySubLists_transformsToEmptyLists() {
      // Arrange
      OriginOfTranslationDTO dto =
          OriginOfTranslationDTO.builder()
              .id(UUID.randomUUID())
              .translators(List.of())
              .borderNumbers(List.of())
              .urls(List.of())
              .build();

      // Act
      OriginOfTranslation domain = OriginOfTranslationTransformer.transformToDomain(dto);

      // Assert
      Assertions.assertTrue(domain.translators().isEmpty());
      Assertions.assertTrue(domain.borderNumbers().isEmpty());
      Assertions.assertTrue(domain.urls().isEmpty());
    }
  }
}
