package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseSubjectFieldRepository extends R2dbcRepository<SubjectFieldDTO, Long> {

  Flux<SubjectFieldDTO> findAllByParentIdOrderBySubjectFieldNumberAsc(Long id);

  @Query(
      "SELECT * FROM lookuptable_subject_field WHERE parent_id = ( "
          + "    SELECT id FROM lookuptable_subject_field WHERE subject_field_number = :subjectFieldNumber "
          + ") ORDER BY subject_field_number")
  Flux<SubjectFieldDTO> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber);

  @Query(
      "SELECT * FROM lookuptable_subject_field WHERE UPPER(CONCAT(subject_field_number, ' ', subject_field_text)) LIKE UPPER('%'||:searchStr||'%') ORDER BY subject_field_number LIMIT :limit OFFSET :offset")
  Flux<SubjectFieldDTO> findBySearchStr(String searchStr, long offset, int limit);

  Flux<SubjectFieldDTO> findAllByOrderBySubjectFieldNumberAsc(Pageable pageable);

  Mono<SubjectFieldDTO> findBySubjectFieldNumber(String subjectFieldNumber);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_subject_field WHERE UPPER(CONCAT(subject_field_number, ' ', subject_field_text)) LIKE UPPER('%'||:searchStr||'%')")
  Mono<Long> countBySearchStr(String searchStr);

  @Query(
      "SELECT sf.* FROM lookuptable_subject_field sf WHERE sf.id IN ( "
          + "SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%')) "
          + "ORDER BY subject_field_number LIMIT :limit OFFSET :offset")
  Flux<SubjectFieldDTO> findByNormsStr(String normsStr, long offset, int limit);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_subject_field sf WHERE sf.id IN ( "
          + "SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%'))")
  Mono<Long> countByNormsStr(String normsStr);

  @Query(
      "SELECT sf.* FROM lookuptable_subject_field sf WHERE sf.id IN ( "
          + "SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%')) "
          + "AND UPPER(CONCAT(sf.subject_field_number, ' ', sf.subject_field_text)) LIKE UPPER('%'||:searchStr||'%') "
          + "ORDER BY subject_field_number LIMIT :limit OFFSET :offset")
  Flux<SubjectFieldDTO> findByNormsAndSearchStr(
      String normsStr, String searchStr, long offset, int limit);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_subject_field sf WHERE sf.id IN ( "
          + "SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%')) "
          + "AND UPPER(CONCAT(sf.subject_field_number, ' ', sf.subject_field_text)) LIKE UPPER('%'||:searchStr||'%')")
  Mono<Long> countByNormsAndSearchStr(String normsStr, String searchStr);
}
