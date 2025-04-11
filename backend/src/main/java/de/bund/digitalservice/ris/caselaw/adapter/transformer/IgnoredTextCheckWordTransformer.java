package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.IgnoredTextCheckWordDTO;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IgnoredTextCheckWordTransformer {
  public static IgnoredTextCheckWord transformToDomain(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    return new IgnoredTextCheckWord(
        ignoredTextCheckWordDTO.getId(),
        getIgnoredTextCheckType(ignoredTextCheckWordDTO),
        ignoredTextCheckWordDTO.getDocumentationUnitId() != null, // TODO
        ignoredTextCheckWordDTO.getWord());
  }

  private static IgnoredTextCheckType getIgnoredTextCheckType(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    if (ignoredTextCheckWordDTO.getJurisId() != null) {
      return IgnoredTextCheckType.GLOBAL;
    }
    return IgnoredTextCheckType.DOCUMENTATION_UNIT;
  }
}
