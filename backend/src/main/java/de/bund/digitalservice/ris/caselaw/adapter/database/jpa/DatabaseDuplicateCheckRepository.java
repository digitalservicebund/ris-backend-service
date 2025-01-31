package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDuplicateCheckRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {

  @Query(
      nativeQuery = true,
      value =
          """
    WITH filtered_file_numbers AS (
        SELECT upper(trim(value)) AS value
        FROM incremental_migration.file_number
        WHERE upper(trim(value)) IN (:allFileNumbers)
        GROUP BY upper(trim(value))
        HAVING COUNT(*) <= 50
    )

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND documentationUnit.decision_date IN (:allDates)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN incremental_migration.deviating_date deviatingDate
        ON documentationUnit.id = deviatingDate.documentation_unit_id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND deviatingDate.value IN (:allDates)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND documentationUnit.decision_date IN (:allDates)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN incremental_migration.deviating_date deviatingDate
        ON documentationUnit.id = deviatingDate.documentation_unit_id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND deviatingDate.value IN (:allDates)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN incremental_migration.court court
        ON documentationUnit.court_id = court.id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND court.id IN (:allCourtIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN incremental_migration.court court
        ON documentationUnit.court_id = court.id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND court.id IN (:allCourtIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN incremental_migration.deviating_court deviatingCourt
        ON documentationUnit.id = deviatingCourt.documentation_unit_id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND upper(deviatingCourt.value) IN (:allDeviatingCourts)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN incremental_migration.deviating_court deviatingCourt
        ON documentationUnit.id = deviatingCourt.documentation_unit_id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND upper(deviatingCourt.value) IN (:allDeviatingCourts)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
    WHERE upper(documentationUnit.ecli) IN (:allEclis) AND documentationUnit.ecli != ''

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive
    FROM incremental_migration.documentation_unit documentationUnit
      JOIN incremental_migration.deviating_ecli deviatingEcli
        ON documentationUnit.id = deviatingEcli.documentation_unit_id
    WHERE upper(deviatingEcli.value) IN (:allEclis) AND deviatingEcli.value != ''
""")
  List<DocumentationUnitIdDuplicateCheckDTO> findDuplicates(
      @Param("allFileNumbers") List<String> allFileNumbers,
      @Param("allDates") List<LocalDate> allDates,
      @Param("allCourtIds") List<UUID> allCourtIds,
      @Param("allDeviatingCourts") List<String> allDeviatingCourts,
      @Param("allEclis") List<String> allEclis);
}
