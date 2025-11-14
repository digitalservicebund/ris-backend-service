package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationBorderNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationTranslatorDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginOfTranslationUrlDTO;
import de.bund.digitalservice.ris.caselaw.domain.OriginOfTranslation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for transforming OriginOfTranslation objects between DTOs (Data Transfer Objects)
 * and domain objects.
 */
public class OriginOfTranslationTransformer {

  private OriginOfTranslationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms an OriginOfTranslation domain object into it's database representation, a
   * OriginOfTranslationDTO (Data Transfer Object).
   *
   * @param originOfTranslation The Origin of the translation as a domain object.
   * @param index position of the current origin of translation
   * @return A database representation of a domain object.
   */
  public static OriginOfTranslationDTO transformToDTO(
      OriginOfTranslation originOfTranslation, int index) {
    if (originOfTranslation == null) {
      return null;
    }

    OriginOfTranslationDTO originOfTranslationDTO =
        OriginOfTranslationDTO.builder()
            .id(originOfTranslation.newEntry() ? null : originOfTranslation.id())
            .translationType(originOfTranslation.translationType())
            .languageCode(
                LanguageCodeTransformer.transformToDTO(originOfTranslation.languageCode()))
            .rank(index + 1L)
            .build();

    List<OriginOfTranslationTranslatorDTO> translatorDTOs = new ArrayList<>();
    if (originOfTranslation.translators() != null && !originOfTranslation.translators().isEmpty()) {
      for (int i = 0; i < originOfTranslation.translators().size(); i++) {
        var translator = originOfTranslation.translators().get(i);
        OriginOfTranslationTranslatorDTO dto =
            OriginOfTranslationTranslatorDTO.builder()
                .originOfTranslation(originOfTranslationDTO)
                .translatorName(translator)
                .rank(i + 1L)
                .build();
        dto.setOriginOfTranslation(originOfTranslationDTO);
        translatorDTOs.add(dto);
      }
    }
    originOfTranslationDTO.setTranslators(translatorDTOs);

    List<OriginOfTranslationBorderNumberDTO> borderNumberDTOS = new ArrayList<>();
    if (originOfTranslation.borderNumbers() != null
        && !originOfTranslation.borderNumbers().isEmpty()) {
      for (int i = 0; i < originOfTranslation.borderNumbers().size(); i++) {
        var borderNumber = originOfTranslation.borderNumbers().get(i);
        OriginOfTranslationBorderNumberDTO dto =
            OriginOfTranslationBorderNumberDTO.builder()
                .originOfTranslation(originOfTranslationDTO)
                .borderNumber(borderNumber)
                .rank(i + 1L)
                .build();
        borderNumberDTOS.add(dto);
      }
    }
    originOfTranslationDTO.setBorderNumbers(borderNumberDTOS);

    List<OriginOfTranslationUrlDTO> urlDTOS = new ArrayList<>();
    if (originOfTranslation.urls() != null && !originOfTranslation.urls().isEmpty()) {
      for (int i = 0; i < originOfTranslation.urls().size(); i++) {
        var url = originOfTranslation.urls().get(i);
        OriginOfTranslationUrlDTO dto =
            OriginOfTranslationUrlDTO.builder()
                .originOfTranslation(originOfTranslationDTO)
                .url(url)
                .rank(i + 1L)
                .build();
        dto.setOriginOfTranslation(originOfTranslationDTO);
        urlDTOS.add(dto);
      }
    }
    originOfTranslationDTO.setUrls(urlDTOS);

    return originOfTranslationDTO;
  }

  /**
   * Transforms a OriginOfTranslationDTO (Data Transfer Object) into a OriginOfTranslation domain
   * object.
   *
   * @param dto A database representation of the origin of translation domain object, to be
   *     transformed.
   * @return The domain representation of the origin of translation.
   */
  public static OriginOfTranslation transformToDomain(OriginOfTranslationDTO dto) {
    if (dto == null) {
      return null;
    }

    return OriginOfTranslation.builder()
        .id(dto.getId())
        .languageCode(LanguageCodeTransformer.transformToDomain(dto.getLanguageCode()))
        .translationType(dto.getTranslationType())
        .translators(
            Optional.ofNullable(dto.getTranslators()).orElse(new ArrayList<>()).stream()
                .map(OriginOfTranslationTranslatorDTO::getTranslatorName)
                .toList())
        .borderNumbers(
            Optional.ofNullable(dto.getBorderNumbers()).orElse(new ArrayList<>()).stream()
                .map(OriginOfTranslationBorderNumberDTO::getBorderNumber)
                .toList())
        .urls(
            Optional.ofNullable(dto.getUrls()).orElse(new ArrayList<>()).stream()
                .map(OriginOfTranslationUrlDTO::getUrl)
                .toList())
        .build();
  }
}
