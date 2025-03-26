package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Nullable;

@NoRepositoryBean
public interface IgnoredTextCheckWordRepository {

  List<IgnoredTextCheckWord> findAllByDocumentationOfficesOrUnitAndWords(
      List<UUID> documentationOfficeIds, @Nullable UUID documentationUnitId, List<String> words);
}
