package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.KeywordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresKeywordRepositoryImpl implements KeywordRepository {

  DatabaseKeywordRepository databaseKeywordRepository;
  DatabaseDocumentUnitRepository databaseDocumentUnitRepository;

  public PostgresKeywordRepositoryImpl(
      DatabaseKeywordRepository databaseKeywordRepository,
      DatabaseDocumentUnitRepository databaseDocumentUnitRepository) {

    this.databaseKeywordRepository = databaseKeywordRepository;
    this.databaseDocumentUnitRepository = databaseDocumentUnitRepository;
  }

  @Override
  public Mono<List<String>> findAllByDocumentUnit(UUID documentUnitUuid) {
    return getAllKeywordsByDocumentUnit(documentUnitUuid).collectList();
  }

  @Override
  public Mono<List<String>> addKeywordToDocumentUnit(UUID documentUnitUuid, String keyword) {
    return getDocumentUnitId(documentUnitUuid)
        .flatMap(
            documentUnitId ->
                databaseKeywordRepository.findByDocumentUnitIdAndKeyword(documentUnitId, keyword))
        .switchIfEmpty(saveKeyword(documentUnitUuid, keyword))
        .then(getAllKeywordsByDocumentUnit(documentUnitUuid).collectList());
  }

  @Override
  public Mono<List<String>> deleteKeywordFromDocumentUnit(UUID documentUnitUuid, String keyword) {
    return getDocumentUnitId(documentUnitUuid)
        .flatMap(
            documentUnitId ->
                databaseKeywordRepository.findByDocumentUnitIdAndKeyword(documentUnitId, keyword))
        .flatMap(databaseKeywordRepository::delete)
        .then(getAllKeywordsByDocumentUnit(documentUnitUuid).collectList());
  }

  private Mono<KeywordDTO> saveKeyword(UUID documentUnitUuid, String keyword) {
    return getDocumentUnitId(documentUnitUuid)
        .flatMap(
            documentUnitId -> {
              KeywordDTO keywordDTO =
                  KeywordDTO.builder().documentUnitId(documentUnitId).keyword(keyword).build();
              return databaseKeywordRepository.save(keywordDTO);
            });
  }

  private Mono<Long> getDocumentUnitId(UUID documentUnitUuid) {
    return databaseDocumentUnitRepository.findByUuid(documentUnitUuid).map(DocumentUnitDTO::getId);
  }

  private Flux<String> getAllKeywordsByDocumentUnit(UUID documentUnitUuid) {
    return getDocumentUnitId(documentUnitUuid)
        .flatMapMany(databaseKeywordRepository::findAllByDocumentUnitId)
        .map(KeywordDTO::keyword);
  }
}
