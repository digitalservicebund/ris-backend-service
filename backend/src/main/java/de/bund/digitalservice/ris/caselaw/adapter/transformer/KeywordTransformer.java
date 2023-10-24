package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.domain.Keyword;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KeywordTransformer {

  public static KeywordDTO transformToDTO(Keyword keyword, Long rank) {
    return KeywordDTO.builder().value(keyword.keyword()).rank(rank).build();
  }

  public static Keyword transformToDomain(KeywordDTO keywordDTO) {
    return Keyword.builder().keyword(keywordDTO.getValue()).build();
  }
}
