package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.IgnoredTextCheckWordDTO;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IgnoredTextCheckWordTransformer {

  public static IgnoredTextCheckWord transformToDomain(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    return IgnoredTextCheckWord.builder()
        .uuid(ignoredTextCheckWordDTO.getId())
        .word(ignoredTextCheckWordDTO.getWord())
        .build();
  }
}
