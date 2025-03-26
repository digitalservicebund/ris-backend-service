package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.IgnoredTextCheckWordTransformer;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresIgnoredTextCheckWordRepositoryImpl implements IgnoredTextCheckWordRepository {
  private final DatabaseIgnoredTextCheckWordRepository repository;

  public PostgresIgnoredTextCheckWordRepositoryImpl(
      DatabaseIgnoredTextCheckWordRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<IgnoredTextCheckWord> findAllByDocumentationOfficesOrUnitAndWords(
      List<UUID> documentationOfficeIds, UUID documentationUnitId, List<String> words) {
    return repository
        .findAllByDocumentationOfficesIdsOrUnitIdsAndWords(
            documentationOfficeIds, documentationUnitId, words)
        .stream()
        .map(IgnoredTextCheckWordTransformer::transformToDomain)
        .toList();
  }
}
