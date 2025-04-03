package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IgnoredTextCheckWordRepository {

  IgnoredTextCheckWord addIgnoredTextCheckWord(String word, UUID documentationOfficeId);

  void deleteAllByWordAndDocumentationUnitId(String word, UUID documentationUnitId);

  List<IgnoredTextCheckWord> findByWordAndDocumentationUnitIdAndExternal(
      String word, UUID documentationUnitId);
}
