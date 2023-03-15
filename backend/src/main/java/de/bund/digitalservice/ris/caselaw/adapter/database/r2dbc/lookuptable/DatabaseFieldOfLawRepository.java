package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseFieldOfLawRepository extends R2dbcRepository<FieldOfLawDTO, Long> {

  Flux<FieldOfLawDTO> findAllByParentIdOrderByIdentifierAsc(Long id);

  Flux<FieldOfLawDTO> findAllByOrderByIdentifierAsc(Pageable pageable);

  Mono<FieldOfLawDTO> findByIdentifier(String identifier);

  @Query(
      "SELECT * FROM lookuptable_field_of_law WHERE parent_id = ( "
          + "    SELECT id FROM lookuptable_field_of_law WHERE identifier = :identifier "
          + ") ORDER BY identifier")
  Flux<FieldOfLawDTO> findAllByParentIdentifierOrderByIdentifierAsc(String identifier);

  @Query(
      "WITH content_added AS (SELECT *, "
          + "     UPPER(CONCAT(identifier, ' ', text)) AS content "
          + "   FROM lookuptable_field_of_law) "
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
  Flux<FieldOfLawDTO> findBySearchStr(String searchStr, long offset, int limit);

  @Query(
      "SELECT COUNT(*) FROM lookuptable_field_of_law "
          + "WHERE UPPER(CONCAT(identifier, ' ', text)) LIKE UPPER('%'||:searchStr||'%')")
  Mono<Long> countBySearchStr(String searchStr);

  @Query(
      "WITH param_arrays(KEY, value) AS ( "
          + "    VALUES ('search', :searchTerms)), "
          + "     match_counts(id, contained_matches) AS "
          + "         (SELECT id, COUNT(id) AS contained_matches "
          + "          FROM "
          + "              (SELECT DISTINCT id, "
          + "                               LOWER(array_to_string(regexp_matches(CONCAT(identifier, ' ', text), array_to_string( "
          + "                                       (SELECT value "
          + "                                        FROM param_arrays "
          + "                                        WHERE KEY = 'search'), '|'), 'gi'), '')) "
          + "               FROM lookuptable_field_of_law) AS t "
          + "          GROUP BY id) "
          + "SELECT t.* "
          + "FROM lookuptable_field_of_law t "
          + "         JOIN match_counts mc ON t.id = mc.id "
          + "WHERE mc.contained_matches = array_length( "
          + "        (SELECT value "
          + "         FROM param_arrays "
          + "         WHERE KEY = 'search'), 1);")
  Flux<FieldOfLawDTO> findBySearchTerms(String[] searchTerms);

  @Query(
      "SELECT sf.* FROM lookuptable_field_of_law sf WHERE sf.id IN ( "
          + "SELECT n.field_of_law_id FROM lookuptable_field_of_law_norm n "
          + "WHERE LOWER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE LOWER('%'||:normStr||'%'));")
  Flux<FieldOfLawDTO> findByNormStr(String normStr);

  @Query(
      "WITH param_arrays(KEY, value) AS ( "
          + "    VALUES ('search', :searchTerms)), "
          + "     match_counts(id, contained_matches) AS "
          + "         (SELECT id, COUNT(id) AS contained_matches "
          + "          FROM "
          + "              (SELECT DISTINCT id, "
          + "                               LOWER(array_to_string(regexp_matches(CONCAT(identifier, ' ', text), array_to_string( "
          + "                                       (SELECT value "
          + "                                        FROM param_arrays "
          + "                                        WHERE KEY = 'search'), '|'), 'gi'), '')) "
          + "               FROM lookuptable_field_of_law) AS t "
          + "          GROUP BY id) "
          + "SELECT DISTINCT t.* "
          + "FROM lookuptable_field_of_law t "
          + "         JOIN match_counts mc ON t.id = mc.id "
          + "         JOIN lookuptable_field_of_law_norm n ON t.id = n.field_of_law_id "
          + "WHERE mc.contained_matches = array_length( "
          + "        (SELECT value "
          + "         FROM param_arrays "
          + "         WHERE KEY = 'search'), 1)"
          + "  AND LOWER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE LOWER('%'||:normStr||'%');")
  Flux<FieldOfLawDTO> findByNormStrAndSearchTerms(String normStr, String[] searchTerms);
}
