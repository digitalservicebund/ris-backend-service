package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
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
      ForeignLanguageVersion foreignLanguageVersion, DecisionDTO decisionDTO) {
    if (foreignLanguageVersion == null) {
      return null;
    }
    return ForeignLanguageVersionDTO.builder()
        .id(foreignLanguageVersion.getId())
        .documentationUnit(decisionDTO)
        .languageCode(
            LanguageCodeTransformer.transformToDTO(foreignLanguageVersion.getLanguageCode()))
        .url(foreignLanguageVersion.getLink())
        .build();
  }
}
