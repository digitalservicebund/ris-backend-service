package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.IgnoredTextCheckWordDTO;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IgnoredTextCheckWordTransformer {
  public static IgnoredTextCheckWord transformToDomain(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    if (ignoredTextCheckWordDTO == null) {
      return null;
    }
    IgnoredTextCheckType ignoredTextCheckType = getIgnoredTextCheckType(ignoredTextCheckWordDTO);
    return new IgnoredTextCheckWord(
        ignoredTextCheckWordDTO.getId(), ignoredTextCheckType, ignoredTextCheckWordDTO.getWord());
  }

  private static IgnoredTextCheckType getIgnoredTextCheckType(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    if (ignoredTextCheckWordDTO.getJurisId() != null) {
      return IgnoredTextCheckType.GLOBAL_JDV;
    } else if (ignoredTextCheckWordDTO.getDocumentationUnitId() != null) {
      return IgnoredTextCheckType.DOCUMENTATION_UNIT;
    }
    return IgnoredTextCheckType.GLOBAL;
  }
}
