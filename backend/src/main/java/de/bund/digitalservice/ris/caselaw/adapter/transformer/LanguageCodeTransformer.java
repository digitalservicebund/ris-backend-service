package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LanguageCodeDTO;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;

public class LanguageCodeTransformer {

  private LanguageCodeTransformer() {}

  public static LanguageCode transformToDomain(LanguageCodeDTO languageCodeDTO) {
    if (languageCodeDTO == null) {
      return null;
    }
    return LanguageCode.builder()
        .id(languageCodeDTO.getId())
        .label(languageCodeDTO.getValue())
        .build();
  }

  public static LanguageCodeDTO transformToDTO(LanguageCode languageCode) {
    if (languageCode == null) {
      return null;
    }
    return LanguageCodeDTO.builder().id(languageCode.id()).value(languageCode.label()).build();
  }
}
