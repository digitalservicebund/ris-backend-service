package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.IgnoredTextCheckWordDTO;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IgnoredTextCheckWordTransformer {

  public static IgnoredTextCheckWordDTO transformToDTO(IgnoredTextCheckWord ignoredTextCheckWord) {
    return IgnoredTextCheckWordDTO.builder()
        .id(ignoredTextCheckWord.getId())
        .word(ignoredTextCheckWord.getWord())
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDTO(
                ignoredTextCheckWord.getDocumentationOffice()))
        .build();
  }

  public static IgnoredTextCheckWord transformToDomain(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    return IgnoredTextCheckWord.builder()
        .id(ignoredTextCheckWordDTO.getId())
        .word(ignoredTextCheckWordDTO.getWord())
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDomain(
                ignoredTextCheckWordDTO.getDocumentationOffice()))
        .build();
  }
}
