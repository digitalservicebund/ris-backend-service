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
          + "WHERE LOWER(CONCAT(n.abbreviation, ' ', n.single_norm_description)) LIKE LOWER('%'||:normStr||'%') "
          + "OR LOWER(CONCAT(n.single_norm_description, ' ', n.abbreviation)) LIKE LOWER('%'||:normStr||'%'));")
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
          + "  AND LOWER(CONCAT(n.single_norm_description, ' ', n.abbreviation)) LIKE LOWER('%'||:normStr||'%');")
  Flux<FieldOfLawDTO> findByNormStrAndSearchTerms(String normStr, String[] searchTerms);

  @Query(
      "SELECT * FROM lookuptable_field_of_law "
          + "ORDER BY LENGTH(identifier), identifier LIMIT 50;")
  Flux<FieldOfLawDTO> getAllLimitedOrderByIdentifierLength();

  @Query(
      "SELECT *, "
          + "     CASE "
          + "         WHEN UPPER(identifier) LIKE UPPER(:searchStr||'%') THEN 1 "
          + "         ELSE 2 "
          + "         END AS weight "
          + "FROM lookuptable_field_of_law "
          + "WHERE UPPER(identifier) LIKE UPPER('%'||:searchStr||'%') "
          + "ORDER BY weight, LENGTH(identifier), identifier LIMIT 50;")
  Flux<FieldOfLawDTO> findByIdentifierSearch(String searchStr);
}
