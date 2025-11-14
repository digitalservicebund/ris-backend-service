package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ForeignLanguageVersionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;

public class ForeignLanguageTransformer {

  private ForeignLanguageTransformer() {}

  public static ForeignLanguageVersion transformToDomain(
      ForeignLanguageVersionDTO foreignLanguageVersionDTO) {
    if (foreignLanguageVersionDTO == null) {
      return null;
    }
    return ForeignLanguageVersion.builder()
        .id(foreignLanguageVersionDTO.getId())
        .languageCode(
            LanguageCodeTransformer.transformToDomain(foreignLanguageVersionDTO.getLanguageCode()))
        .link(foreignLanguageVersionDTO.getUrl())
        .build();
  }

  public static ForeignLanguageVersionDTO transformToDTO(
      ForeignLanguageVersion foreignLanguageVersion, int index) {
    if (foreignLanguageVersion == null) {
      return null;
    }
    return ForeignLanguageVersionDTO.builder()
        .id(foreignLanguageVersion.newEntry() ? null : foreignLanguageVersion.id())
        .url(foreignLanguageVersion.link())
        .languageCode(LanguageCodeTransformer.transformToDTO(foreignLanguageVersion.languageCode()))
        .rank(index + 1L)
        .build();
  }
}
