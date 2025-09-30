package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DuplicateRelationRepository
    extends JpaRepository<DuplicateRelationDTO, DuplicateRelationDTO.DuplicateRelationId> {

  @Query(
      value =
"""
SELECT duplicateRelation
FROM DuplicateRelationDTO duplicateRelation
WHERE duplicateRelation.id.documentationUnitId1 = :docUnitId
      OR duplicateRelation.id.documentationUnitId2 = :docUnitId
""")
  List<DuplicateRelationDTO> findAllByDocUnitId(UUID docUnitId);

  @Modifying
  @Query(
      value =
"""
WITH
    -- 1) Collect for each relevant criterion a full list of pairs of doc-unit-id and value, e.g. ("38e..34f", 2020-01-11)
    all_dates as(
        SELECT documentation_unit_id as id, value
        FROM incremental_migration.deviating_date
        UNION ALL
        SELECT id, date as value
        FROM incremental_migration.documentation_unit),
     all_courts as(
        SELECT documentation_unit_id as id, court.id as value
        FROM incremental_migration.deviating_court
        INNER JOIN incremental_migration.court
            ON UPPER(value) = (
                CASE
                    WHEN court.is_superior_court = true THEN UPPER( type )
                    ELSE UPPER( CONCAT( type, ' ', location ))
                END
            )
        UNION ALL
        SELECT id, court_id as value
        FROM incremental_migration.documentation_unit
        WHERE court_id IS NOT NULL),
     all_file_numbers as (
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.file_number
        -- File numbers as "XX" lead to explosion of duplicate relationships
        WHERE value NOT IN (SELECT value
                            FROM incremental_migration.file_number
                            GROUP BY value
                            HAVING count(*) > 50)
        UNION ALL
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.deviating_file_number),

    -- 2) Find all pairs of doc-unit-ids that have an identical value per criterion,
    -- e.g. ("38e..34f", 2020-01-11) + ("785..eb9", 2020-01-11) -> ("38e..34f", "785..eb9")
    -- We step-by-step narrow down the number of doc-unit-id pairs by only considering pairs for the next check that have matched by previously checked criteria.
    -- The order of the criteria is important: We need to start with the most specific criterion that has the least matches.
    -- If we started with doc-type instead, we would get an explosion of matches as doc units having the same doc-type is very common.

    -- 2A) Combined criteria: Aktenzeichen + Entscheidungsdatum + Dok-Typ // Aktenzeichen + Gericht + Dok-Typ
     file_number_matches as (
        SELECT DISTINCT t1.id AS id_a, t2.id AS id_b
        FROM all_file_numbers t1
        JOIN all_file_numbers t2 ON t1.value = t2.value
        WHERE t1.id < t2.id),
     date_file_number_matches as (
        SELECT id_a, id_b
        FROM file_number_matches
        JOIN all_dates d1 ON d1.id = id_a
        JOIN all_dates d2 ON d2.id = id_b
        WHERE d1.value = d2.value),
     date_file_number_doc_type_matches as (
        SELECT matches.id_a, matches.id_b
        FROM date_file_number_matches matches
        JOIN incremental_migration.documentation_unit d1 ON d1.id = matches.id_a
        JOIN incremental_migration.documentation_unit d2 ON d2.id = matches.id_b
        WHERE d1.document_type_id = d2.document_type_id),
     court_file_number_matches as (
        SELECT fnm.id_a, fnm.id_b
        FROM file_number_matches fnm
        JOIN all_courts d1 ON d1.id = fnm.id_a
        JOIN all_courts d2 ON d2.id = fnm.id_b
        WHERE d1.value = d2.value),
     court_file_number_doc_type_matches as (
        SELECT matches.id_a, matches.id_b
        FROM court_file_number_matches matches
        JOIN incremental_migration.documentation_unit d1 ON d1.id = matches.id_a
        JOIN incremental_migration.documentation_unit d2 ON d2.id = matches.id_b
        WHERE d1.document_type_id = d2.document_type_id),

    -- 2B) ECLI matches
     all_eclis as (
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.deviating_ecli
        UNION ALL
        SELECT id, UPPER(ecli) as value
        FROM incremental_migration.decision),
     ecli_matches as (
        SELECT t1.id AS id_a, t2.id AS id_b
        FROM all_eclis t1
        JOIN all_eclis t2 ON t1.value = t2.value
        WHERE t1.id < t2.id),

    -- 3) Combine all matches and reasons for the matches
     duplicate_relations_view as (
        SELECT id_a, id_b
        FROM (
            SELECT *
            FROM date_file_number_doc_type_matches
            UNION ALL
            SELECT *
            FROM court_file_number_doc_type_matches
            UNION ALL
            SELECT *
            FROM ecli_matches
        ) as all_matches
      GROUP BY id_a, id_b)
DELETE
FROM incremental_migration.duplicate_relation
WHERE (documentation_unit_id1, documentation_unit_id2) NOT IN
      (SELECT id_a as documentation_unit_id1, id_b as documentation_unit_id2
       FROM duplicate_relations_view);
""",
      nativeQuery = true)
  int removeObsoleteDuplicateRelations();

  @Modifying
  @Query(
      value =
"""
WITH
    all_dates as(
        SELECT documentation_unit_id as id, value
        FROM incremental_migration.deviating_date
        UNION ALL
        SELECT id, date as value
        FROM incremental_migration.documentation_unit),
     all_courts as(
        SELECT documentation_unit_id as id, court.id as value
        FROM incremental_migration.deviating_court
        INNER JOIN incremental_migration.court
            ON UPPER(value) = (
                CASE
                    WHEN court.is_superior_court = true THEN UPPER( type )
                    ELSE UPPER( CONCAT( type, ' ', location ))
                END
            )
        UNION ALL
        SELECT id, court_id as value
        FROM incremental_migration.documentation_unit
        WHERE court_id IS NOT NULL),
     all_file_numbers as (
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.file_number
        -- File numbers as "XX" lead to explosion of duplicate relationships
        WHERE value NOT IN (SELECT value
                            FROM incremental_migration.file_number
                            GROUP BY value
                            HAVING count(*) > 50)
        UNION ALL
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.deviating_file_number),
     file_number_matches as (
        SELECT DISTINCT t1.id AS id_a, t2.id AS id_b
        FROM all_file_numbers t1
        JOIN all_file_numbers t2 ON t1.value = t2.value
        WHERE t1.id < t2.id),
     date_file_number_matches as (
        SELECT id_a, id_b
        FROM file_number_matches
        JOIN all_dates d1 ON d1.id = id_a
        JOIN all_dates d2 ON d2.id = id_b
        WHERE d1.value = d2.value),
     date_file_number_doc_type_matches as (
        SELECT matches.id_a, matches.id_b
        FROM date_file_number_matches matches
        JOIN incremental_migration.documentation_unit d1 ON d1.id = matches.id_a
        JOIN incremental_migration.documentation_unit d2 ON d2.id = matches.id_b
        WHERE d1.document_type_id = d2.document_type_id),
     court_file_number_matches as (
        SELECT fnm.id_a, fnm.id_b
        FROM file_number_matches fnm
        JOIN all_courts d1 ON d1.id = fnm.id_a
        JOIN all_courts d2 ON d2.id = fnm.id_b
        WHERE d1.value = d2.value),
     court_file_number_doc_type_matches as (
        SELECT matches.id_a, matches.id_b
        FROM court_file_number_matches matches
        JOIN incremental_migration.documentation_unit d1 ON d1.id = matches.id_a
        JOIN incremental_migration.documentation_unit d2 ON d2.id = matches.id_b
        WHERE d1.document_type_id = d2.document_type_id),

     all_eclis as (
        SELECT documentation_unit_id as id, UPPER(value) as value
        FROM incremental_migration.deviating_ecli
        UNION ALL
        SELECT id, UPPER(ecli) as value
        FROM incremental_migration.decision),
     ecli_matches as (
        SELECT t1.id AS id_a, t2.id AS id_b
        FROM all_eclis t1
        JOIN all_eclis t2 ON t1.value = t2.value
        WHERE t1.id < t2.id),
     duplicate_relations_view as (
        SELECT id_a, id_b
        FROM (
            SELECT *
            FROM date_file_number_doc_type_matches
            UNION ALL
            SELECT *
            FROM court_file_number_doc_type_matches
            UNION ALL
            SELECT *
            FROM ecli_matches
        ) as all_matches
      GROUP BY id_a, id_b)
INSERT INTO incremental_migration.duplicate_relation (documentation_unit_id1, documentation_unit_id2, status)
SELECT drel.id_a, drel.id_b, 'PENDING'
FROM duplicate_relations_view drel
         LEFT JOIN incremental_migration.duplicate_relation ON drel.id_a = duplicate_relation.documentation_unit_id1 AND
                                                               drel.id_b = duplicate_relation.documentation_unit_id2
         -- proceeding decisions need to be filtered out -> only consider decisions
         INNER JOIN incremental_migration.decision dec1 ON dec1.id = drel.id_a
         INNER JOIN incremental_migration.decision dec2 ON dec2.id = drel.id_b
WHERE duplicate_relation.documentation_unit_id1 IS NULL;
""",
      nativeQuery = true)
  int addMissingDuplicateRelations();

  @Modifying
  @Query(
      value =
"""
UPDATE incremental_migration.duplicate_relation drel
SET status = 'IGNORED'
FROM incremental_migration.documentation_unit d1
        LEFT JOIN incremental_migration.status status1 ON d1.current_status_id = status1.id,
    incremental_migration.documentation_unit d2
        LEFT JOIN incremental_migration.status status2 ON d2.current_status_id = status2.id
WHERE drel.status = 'PENDING'
  AND drel.documentation_unit_id1 = d1.id AND drel.documentation_unit_id2 = d2.id
  AND (d1.duplicate_check = FALSE OR d2.duplicate_check = FALSE
        OR status1.publication_status = 'DUPLICATED' OR status2.publication_status = 'DUPLICATED'
        OR status1.publication_status = 'LOCKED' OR status2.publication_status = 'LOCKED');
""",
      nativeQuery = true)
  int ignoreDuplicateRelationsWhenJdvDupCheckDisabled();
}
