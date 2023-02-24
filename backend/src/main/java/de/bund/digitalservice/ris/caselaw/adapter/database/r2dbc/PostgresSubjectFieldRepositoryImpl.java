package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseSubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.KeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.SubjectFieldTransformer;
import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresSubjectFieldRepositoryImpl implements SubjectFieldRepository {

  DatabaseSubjectFieldRepository databaseSubjectFieldRepository;
  KeywordRepository keywordRepository;
  NormRepository normRepository;
  FieldOfLawLinkRepository fieldOfLawLinkRepository;

  public PostgresSubjectFieldRepositoryImpl(
      DatabaseSubjectFieldRepository databaseSubjectFieldRepository,
      KeywordRepository keywordRepository,
      NormRepository normRepository,
      FieldOfLawLinkRepository fieldOfLawLinkRepository) {
    this.databaseSubjectFieldRepository = databaseSubjectFieldRepository;
    this.keywordRepository = keywordRepository;
    this.normRepository = normRepository;
    this.fieldOfLawLinkRepository = fieldOfLawLinkRepository;
  }

  @Override
  public Flux<FieldOfLaw> findAllByOrderBySubjectFieldNumberAsc() {
    return databaseSubjectFieldRepository
        .findAllByOrderBySubjectFieldNumberAsc()
        .flatMapSequential(this::injectKeywords)
        .flatMapSequential(this::injectNorms)
        .flatMapSequential(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Mono<FieldOfLaw> findBySubjectFieldNumber(String subjectFieldNumber) {
    return databaseSubjectFieldRepository
        .findBySubjectFieldNumber(subjectFieldNumber)
        .flatMap(this::injectKeywords)
        .flatMap(this::injectNorms)
        .flatMap(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Mono<FieldOfLaw> findParentByChild(FieldOfLaw child) {
    return databaseSubjectFieldRepository
        .findBySubjectFieldNumber(child.subjectFieldNumber())
        .flatMap(
            childDTO -> {
              if (childDTO.getParentId() != null) {
                return databaseSubjectFieldRepository.findById(childDTO.getParentId());
              }
              return Mono.just(childDTO);
            })
        .flatMap(this::injectKeywords)
        .flatMap(this::injectNorms)
        .flatMap(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<FieldOfLaw> getTopLevelNodes() {
    return databaseSubjectFieldRepository
        .findAllByParentIdOrderBySubjectFieldNumberAsc(null)
        .flatMapSequential(this::injectKeywords)
        .flatMapSequential(this::injectNorms)
        .flatMapSequential(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<FieldOfLaw> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber) {
    return databaseSubjectFieldRepository
        .findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(subjectFieldNumber)
        .flatMapSequential(this::injectKeywords)
        .flatMapSequential(this::injectNorms)
        .flatMapSequential(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<FieldOfLaw> findBySearchStr(String searchStr) {
    return databaseSubjectFieldRepository
        .findBySearchStr(searchStr)
        .flatMapSequential(this::injectKeywords)
        .flatMapSequential(this::injectNorms)
        .flatMapSequential(this::injectLinkedFields)
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

  private Mono<SubjectFieldDTO> injectLinkedFields(SubjectFieldDTO subjectFieldDTO) {
    return fieldOfLawLinkRepository
        .findAllByFieldId(subjectFieldDTO.getId())
        .map(FieldOfLawLinkDTO::getLinkedFieldId)
        .flatMap(linkedFieldId -> databaseSubjectFieldRepository.findById(linkedFieldId))
        .collectList()
        .map(
            subjectFieldDTOS -> {
              subjectFieldDTO.setLinkedFields(subjectFieldDTOS);
              return subjectFieldDTO;
            });
  }
}
