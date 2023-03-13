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

  Flux<SubjectFieldDTO> findAllByOrderBySubjectFieldNumberAsc(Pageable pageable);

  Mono<SubjectFieldDTO> findBySubjectFieldNumber(String subjectFieldNumber);

  @Query(
      "SELECT * FROM lookuptable_subject_field WHERE parent_id = ( "
          + "    SELECT id FROM lookuptable_subject_field WHERE subject_field_number = :subjectFieldNumber "
          + ") ORDER BY subject_field_number")
  Flux<SubjectFieldDTO> findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
      String subjectFieldNumber);

  @Query(
      "WITH content_added AS (SELECT *, "
          + "     UPPER(CONCAT(subject_field_number, ' ', subject_field_text)) AS content "
          + "   FROM lookuptable_subject_field) "
          + "SELECT *, content, "
          + "       CASE "
          + "           WHEN content LIKE UPPER(:searchStr||'%') THEN 1 "
          + "           WHEN content LIKE UPPER('% '||:searchStr||'%') THEN 2 "
          + "           WHEN content LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
          + "           ELSE 3 "
          + "           END AS weight "
          + "FROM content_added "
          + "WHERE content LIKE UPPER('%'||:searchStr||'%') "
          + "ORDER BY weight, content LIMIT :limit OFFSET :offset")
  Flux<SubjectFieldDTO> findBySearchStr(String searchStr, long offset, int limit);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_subject_field "
          + "WHERE UPPER(CONCAT(subject_field_number, ' ', subject_field_text)) LIKE UPPER('%'||:searchStr||'%')")
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
      "WITH sf_content_added AS (SELECT *, "
          + "     UPPER(CONCAT(subject_field_number, ' ', subject_field_text)) AS content "
          + "   FROM lookuptable_subject_field) "
          + "SELECT sf.*, content, "
          + "       CASE "
          + "           WHEN content LIKE UPPER(:searchStr||'%') THEN 1 "
          + "           WHEN content LIKE UPPER('% '||:searchStr||'%') THEN 2 "
          + "           WHEN content LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
          + "           ELSE 3 "
          + "           END AS weight "
          + "FROM sf_content_added sf WHERE sf.id IN ( "
          + "           SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "           WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%')"
          + "        ) "
          + "        AND content LIKE UPPER('%'||:searchStr||'%') "
          + "        ORDER BY weight, content LIMIT :limit OFFSET :offset;")
  Flux<SubjectFieldDTO> findByNormsAndSearchStr(
      String normsStr, String searchStr, long offset, int limit);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_subject_field sf WHERE sf.id IN ( "
          + "SELECT n.subject_field_id FROM lookuptable_subject_field_norm n "
          + "WHERE UPPER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE UPPER('%'||:normsStr||'%')) "
          + "AND UPPER(CONCAT(sf.subject_field_number, ' ', sf.subject_field_text)) LIKE UPPER('%'||:searchStr||'%')")
  Mono<Long> countByNormsAndSearchStr(String normsStr, String searchStr);

  @Query(
      "WITH param_arrays(KEY, value) AS ( "
          + "    VALUES ('search', :searchTerms)), "
          + "     match_counts(id, contained_matches) AS "
          + "         (SELECT id, COUNT(id) AS contained_matches "
          + "          FROM "
          + "              (SELECT DISTINCT id, "
          + "                               lower(array_to_string(regexp_matches(CONCAT(subject_field_number, ' ', subject_field_text), array_to_string( "
          + "                                       (SELECT value "
          + "                                        FROM param_arrays "
          + "                                        WHERE KEY = 'search'), '|'), 'gi'), '')) "
          + "               FROM lookuptable_subject_field) AS t "
          + "          GROUP BY id) "
          + "SELECT t.* "
          + "FROM lookuptable_subject_field t "
          + "         JOIN match_counts mc ON t.id = mc.id "
          + "WHERE mc.contained_matches = array_length( "
          + "        (SELECT value "
          + "         FROM param_arrays "
          + "         WHERE KEY = 'search'), 1);")
  Flux<SubjectFieldDTO> findBySearchTerms(String[] searchTerms);
}
