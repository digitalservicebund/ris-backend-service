package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IgnoredTextCheckWordRepository {

  IgnoredTextCheckWord addIgnoredTextCheckWord(String word, UUID documentationOfficeId);

  void deleteAllByWordAndDocumentationUnitId(String word, UUID documentationUnitId);

  /**
   * Returns all ignored word by documentation unit and search in global words by the list of words
   *
   * @param documentationUnitId optional documentation unit id to filter by
   * @param words list of words to search for
   * @return a list of ignored text check words matching the criteria
   */
  List<IgnoredTextCheckWord> findByDocumentationUnitIdOrByGlobalWords(
      List<String> words, UUID documentationUnitId);
}
