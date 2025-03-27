package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IgnoredTextCheckWordRepository {

  IgnoredTextCheckWord addIgnoredTextCheckWord(IgnoredTextCheckWord ignoredTextCheckWord);

  List<IgnoredTextCheckWord> findAllByDocumentationOfficesOrUnitAndWords(
      List<UUID> documentationOfficeIds, @Nullable UUID documentationUnitId, List<String> words);
}
