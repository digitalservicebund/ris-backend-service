package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.IgnoredTextCheckWordTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
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
  public IgnoredTextCheckWord addWord(String word, UUID documentationUnitId) {
    return IgnoredTextCheckWordTransformer.transformToDomain(
        repository.save(
            IgnoredTextCheckWordDTO.builder()
                .word(word)
                .documentationUnitId(documentationUnitId)
                .build()));
  }

  @Override
  public void deleteWordIgnoredInDocumentationUnitWithId(String word, UUID documentationUnitId) {
    this.repository.deleteAllByWordAndDocumentationUnitId(word, documentationUnitId);
  }

  @Override
  public IgnoredTextCheckWord addWord(String word, DocumentationOffice documentationOffice) {
    return IgnoredTextCheckWordTransformer.transformToDomain(
        repository.save(
            IgnoredTextCheckWordDTO.builder()
                .word(word)
                .documentationOffice(
                    DocumentationOfficeTransformer.transformToDTO(documentationOffice))
                .build()));
  }

  @Override
  public boolean deleteWordGlobally(String word) {
    return repository.deleteByWordAndDocumentationUnitIdIsNullAndJurisIdIsNull(word) == 1;
  }

  @Override
  public List<IgnoredTextCheckWord> findAllByDocumentationUnitId(UUID documentationUnitId) {
    return this.repository.findAllByDocumentationUnitId(documentationUnitId).stream()
        .map(IgnoredTextCheckWordTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<IgnoredTextCheckWord> findByDocumentationUnitIdOrByGlobalWords(
      List<String> words, UUID documentationUnitId) {
    return repository.findByDocumentationUnitIdOrByGlobalWords(documentationUnitId, words).stream()
        .map(IgnoredTextCheckWordTransformer::transformToDomain)
        .distinct()
        .toList();
  }

  @Override
  public IgnoredTextCheckWord getGloballyIgnoreWord(String word) {
    return IgnoredTextCheckWordTransformer.transformToDomain(
        repository.findByDocumentationUnitIdIsNullAndWord(word));
  }
}
