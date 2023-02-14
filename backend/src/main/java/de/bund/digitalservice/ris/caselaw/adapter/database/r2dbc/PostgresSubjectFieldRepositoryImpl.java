package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseSubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.SubjectFieldTransformer;
import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresSubjectFieldRepositoryImpl implements SubjectFieldRepository {

  DatabaseSubjectFieldRepository databaseSubjectFieldRepository;
  KeywordRepository keywordRepository;
  NormRepository normRepository;

  public PostgresSubjectFieldRepositoryImpl(
      DatabaseSubjectFieldRepository databaseSubjectFieldRepository,
      KeywordRepository keywordRepository,
      NormRepository normRepository) {
    this.databaseSubjectFieldRepository = databaseSubjectFieldRepository;
    this.keywordRepository = keywordRepository;
    this.normRepository = normRepository;
  }

  @Override
  public Mono<SubjectField> findById(Long id) {
    return databaseSubjectFieldRepository
        .findById(id)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Mono<SubjectField> findBySubjectFieldNumber(String subjectFieldNumber) {
    return databaseSubjectFieldRepository
        .findBySubjectFieldNumber(subjectFieldNumber)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Mono<SubjectField> findParentByChild(SubjectField child) {
    return databaseSubjectFieldRepository
        .findBySubjectFieldNumber(child.subjectFieldNumber())
        .flatMap(
            childDTO -> {
              if (childDTO.getParentId() != null) {
                return databaseSubjectFieldRepository.findById(childDTO.getParentId());
              }
              return Mono.just(childDTO);
            })
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<SubjectField> findAllByParentIdOrderBySubjectFieldNumberAsc(Long id) {
    return databaseSubjectFieldRepository
        .findAllByParentIdOrderBySubjectFieldNumberAsc(id)
        .flatMap(this::injectKeywords)
        .flatMap(this::injectNorms)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<SubjectField> findBySearchStr(String searchStr) {
    return databaseSubjectFieldRepository
        .findBySearchStr(searchStr)
        .flatMap(this::injectKeywords)
        .flatMap(this::injectNorms)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  private Mono<SubjectFieldDTO> injectKeywords(SubjectFieldDTO subjectFieldDTO) {
    return keywordRepository
        .findAllBySubjectFieldIdOrderByValueAsc(subjectFieldDTO.getId())
        .collectList()
        .map(
            keywords -> {
              subjectFieldDTO.setKeywords(keywords);
              return subjectFieldDTO;
            });
  }

  private Mono<SubjectFieldDTO> injectNorms(SubjectFieldDTO subjectFieldDTO) {
    return normRepository
        .findAllBySubjectFieldIdOrderByAbbreviationAscSingleNormDescriptionAsc(
            subjectFieldDTO.getId())
        .collectList()
        .map(
            norms -> {
              subjectFieldDTO.setNorms(norms);
              return subjectFieldDTO;
            });
  }
}
