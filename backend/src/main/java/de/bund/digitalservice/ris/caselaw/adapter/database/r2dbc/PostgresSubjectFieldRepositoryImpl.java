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
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresSubjectFieldRepositoryImpl implements SubjectFieldRepository {

  DatabaseSubjectFieldRepository databaseSubjectFieldRepository;
  KeywordRepository keywordRepository;
  NormRepository normRepository;
  FieldOfLawLinkRepository fieldOfLawLinkRepository;
  DatabaseDocumentUnitRepository databaseDocumentUnitRepository;
  DatabaseDocumentUnitFieldsOfLawRepository databaseDocumentUnitFieldsOfLawRepository;

  public PostgresSubjectFieldRepositoryImpl(
      DatabaseSubjectFieldRepository databaseSubjectFieldRepository,
      KeywordRepository keywordRepository,
      NormRepository normRepository,
      FieldOfLawLinkRepository fieldOfLawLinkRepository,
      DatabaseDocumentUnitRepository databaseDocumentUnitRepository,
      DatabaseDocumentUnitFieldsOfLawRepository databaseDocumentUnitFieldsOfLawRepository) {

    this.databaseSubjectFieldRepository = databaseSubjectFieldRepository;
    this.keywordRepository = keywordRepository;
    this.normRepository = normRepository;
    this.fieldOfLawLinkRepository = fieldOfLawLinkRepository;
    this.databaseDocumentUnitRepository = databaseDocumentUnitRepository;
    this.databaseDocumentUnitFieldsOfLawRepository = databaseDocumentUnitFieldsOfLawRepository;
  }

  @Override
  public Flux<FieldOfLaw> findAllByOrderBySubjectFieldNumberAsc(Pageable pageable) {
    return databaseSubjectFieldRepository
        .findAllByOrderBySubjectFieldNumberAsc(pageable)
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
        .findBySubjectFieldNumber(child.identifier())
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
  public Flux<FieldOfLaw> findBySearchStr(String searchStr, Pageable pageable) {
    return databaseSubjectFieldRepository
        .findBySearchStr(searchStr, pageable.getOffset(), pageable.getPageSize())
        .flatMapSequential(this::injectKeywords)
        .flatMapSequential(this::injectNorms)
        .flatMapSequential(this::injectLinkedFields)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Mono<Long> countBySearchStr(String searchStr) {
    return databaseSubjectFieldRepository.countBySearchStr(searchStr);
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

  @Override
  public Flux<FieldOfLaw> findAllForDocumentUnit(UUID documentUnitUuid) {
    return databaseDocumentUnitRepository
        .findByUuid(documentUnitUuid)
        .map(DocumentUnitDTO::getId)
        .flatMapMany(databaseDocumentUnitFieldsOfLawRepository::findAllByDocumentUnitId)
        .map(DocumentUnitFieldsOfLawDTO::fieldOfLawId)
        .flatMap(databaseSubjectFieldRepository::findById)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  @Override
  public Flux<FieldOfLaw> addFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier) {
    Mono<Long> documentUnitDTOId =
        databaseDocumentUnitRepository.findByUuid(documentUnitUuid).map(DocumentUnitDTO::getId);

    Mono<Long> fieldOfLawDTOId =
        databaseSubjectFieldRepository
            .findBySubjectFieldNumber(identifier)
            .mapNotNull(SubjectFieldDTO::getId)
            .defaultIfEmpty(-1L);

    return documentUnitDTOId
        .zipWith(fieldOfLawDTOId)
        .flatMapMany(
            t -> {
              if (t.getT2() == -1L) {
                return getLinkedFieldsOfLaw(t.getT1());
              }

              return databaseDocumentUnitFieldsOfLawRepository
                  .findByDocumentUnitIdAndFieldOfLawId(t.getT1(), t.getT2())
                  .switchIfEmpty(linkFieldOfLawToDocumentUnit(t.getT1(), t.getT2()))
                  .map(DocumentUnitFieldsOfLawDTO::documentUnitId)
                  .thenMany(getLinkedFieldsOfLaw(t.getT1()));
            });
  }

  private Flux<FieldOfLaw> getLinkedFieldsOfLaw(Long documentUnitId) {
    return databaseDocumentUnitFieldsOfLawRepository
        .findAllByDocumentUnitId(documentUnitId)
        .map(DocumentUnitFieldsOfLawDTO::fieldOfLawId)
        .flatMap(databaseSubjectFieldRepository::findById)
        .map(SubjectFieldTransformer::transformToDomain);
  }

  private Mono<DocumentUnitFieldsOfLawDTO> linkFieldOfLawToDocumentUnit(
      Long documentUnitId, Long fieldOfLawId) {

    DocumentUnitFieldsOfLawDTO documentUnitFieldOfLaw =
        DocumentUnitFieldsOfLawDTO.builder()
            .documentUnitId(documentUnitId)
            .fieldOfLawId(fieldOfLawId)
            .build();
    return databaseDocumentUnitFieldsOfLawRepository.save(documentUnitFieldOfLaw);
  }

  @Override
  public Flux<FieldOfLaw> removeFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier) {
    Mono<Long> documentUnitDTOId =
        databaseDocumentUnitRepository.findByUuid(documentUnitUuid).map(DocumentUnitDTO::getId);

    Mono<Long> fieldOfLawDTOId =
        databaseSubjectFieldRepository
            .findBySubjectFieldNumber(identifier)
            .mapNotNull(SubjectFieldDTO::getId)
            .defaultIfEmpty(-1L);

    return documentUnitDTOId
        .zipWith(fieldOfLawDTOId)
        .flatMapMany(
            t ->
                databaseDocumentUnitFieldsOfLawRepository
                    .findByDocumentUnitIdAndFieldOfLawId(t.getT1(), t.getT2())
                    .flatMap(dto -> databaseDocumentUnitFieldsOfLawRepository.delete(dto))
                    .thenMany(getLinkedFieldsOfLaw(t.getT1())));
  }

  @Override
  public Mono<Long> count() {
    return databaseSubjectFieldRepository.count();
  }
}
