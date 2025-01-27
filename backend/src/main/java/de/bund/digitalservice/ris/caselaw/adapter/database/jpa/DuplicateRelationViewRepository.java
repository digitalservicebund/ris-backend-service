package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DuplicateRelationViewRepository
    extends JpaRepository<DuplicateRelationViewDTO, DuplicateRelationViewDTO.DuplicateRelationId> {

  @Modifying
  @Query(
      value = "refresh materialized view incremental_migration.duplicate_relations_view;",
      nativeQuery = true)
  void refreshDuplicateRelationsView();

  @Modifying
  @Query(
      value =
          """
DELETE
FROM incremental_migration.duplicate_relation
WHERE (documentation_unit_id1, documentation_unit_id2) NOT IN
      (SELECT id_a as documentation_unit_id1, id_b as documentation_unit_id2
       FROM incremental_migration.duplicate_relations_view);
""",
      nativeQuery = true)
  int removeObsoleteDuplicateRelations();

  @Modifying
  @Query(
      value =
          """
INSERT INTO incremental_migration.duplicate_relation (documentation_unit_id1, documentation_unit_id2, status)
SELECT drel.id_a,
       drel.id_b,
       CAST(CASE
                WHEN d1.duplicate_check = FALSE OR d2.duplicate_check = FALSE THEN 'IGNORED'
                ELSE 'PENDING' END AS incremental_migration.duplicate_relation_status)
FROM incremental_migration.duplicate_relations_view drel
         LEFT JOIN incremental_migration.duplicate_relation ON drel.id_a = duplicate_relation.documentation_unit_id1 AND
                                         drel.id_b = duplicate_relation.documentation_unit_id2
         LEFT JOIN incremental_migration.documentation_unit d1 ON drel.id_a = d1.id
         LEFT JOIN incremental_migration.documentation_unit d2 ON drel.id_b = d2.id
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
FROM incremental_migration.documentation_unit d1, incremental_migration.documentation_unit d2
WHERE drel.status = 'PENDING'
  AND drel.documentation_unit_id1 = d1.id AND drel.documentation_unit_id2 = d2.id
  AND (d1.duplicate_check = FALSE OR d2.duplicate_check = FALSE);
                  """,
      nativeQuery = true)
  int ignoreDuplicateRelationsWhenJdvDupCheckDisabled();
}
