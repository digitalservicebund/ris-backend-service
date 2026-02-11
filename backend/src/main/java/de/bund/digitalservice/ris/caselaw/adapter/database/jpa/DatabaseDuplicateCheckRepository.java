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
    -- We filter file numbers that occur more than 50 times (i.e. "XX"):
    -- They lead to explosion of duplicate relationships
    WITH filtered_file_numbers AS (
        SELECT value FROM (
            SELECT upper(value) AS value
            FROM file_number
            WHERE upper(value) IN (:allFileNumbers)
            UNION ALL
            SELECT upper(value) AS value
            FROM deviating_file_number
            WHERE upper(value) IN (:allFileNumbers)) as regular_and_deviating_file_numbers
        GROUP BY value
        HAVING COUNT(*) <= 50
    )

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND documentationUnit.date IN (:allDates)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN deviating_date deviatingDate
        ON documentationUnit.id = deviatingDate.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND deviatingDate.value IN (:allDates)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND documentationUnit.date IN (:allDates)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN deviating_date deviatingDate
        ON documentationUnit.id = deviatingDate.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND deviatingDate.value IN (:allDates)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN court court
        ON documentationUnit.court_id = court.id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND court.id IN (:allCourtIds)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN court court
        ON documentationUnit.court_id = court.id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND court.id IN (:allCourtIds)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN file_number fileNumber
        ON documentationUnit.id = fileNumber.documentation_unit_id
      JOIN deviating_court deviatingCourt
        ON documentationUnit.id = deviatingCourt.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(fileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND upper(deviatingCourt.value) IN (:allDeviatingCourts)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
      LEFT JOIN status status ON status.id = documentationUnit.current_status_id
      JOIN deviating_file_number deviatingFileNumber
        ON documentationUnit.id = deviatingFileNumber.documentation_unit_id
      JOIN deviating_court deviatingCourt
        ON documentationUnit.id = deviatingCourt.documentation_unit_id
      JOIN document_type documentType
        ON documentationUnit.document_type_id = documentType.id
      INNER JOIN decision decision
        ON decision.id = documentationUnit.id
    WHERE upper(deviatingFileNumber.value) IN (SELECT value FROM filtered_file_numbers)
      AND upper(deviatingCourt.value) IN (:allDeviatingCourts)
      AND documentType.id IN (:allDocTypeIds)

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
    LEFT JOIN status status ON status.id = documentationUnit.current_status_id
    INNER JOIN decision decision
      ON decision.id = documentationUnit.id
    WHERE upper(decision.ecli) IN (:allEclis) AND decision.ecli != ''

    UNION

    SELECT documentationUnit.id, documentationUnit.duplicate_check AS isJdvDuplicateCheckActive, status.publication_status AS status
    FROM documentation_unit documentationUnit
    LEFT JOIN status status ON status.id = documentationUnit.current_status_id
    INNER JOIN decision decision
      ON decision.id = documentationUnit.id
    JOIN deviating_ecli deviatingEcli
      ON decision.id = deviatingEcli.documentation_unit_id
    WHERE upper(deviatingEcli.value) IN (:allEclis) AND deviatingEcli.value != ''
""")
  List<DocumentationUnitIdDuplicateCheckDTO> findDuplicates(
      @Param("allFileNumbers") List<String> allFileNumbers,
      @Param("allDates") List<LocalDate> allDates,
      @Param("allCourtIds") List<UUID> allCourtIds,
      @Param("allDeviatingCourts") List<String> allDeviatingCourts,
      @Param("allEclis") List<String> allEclis,
      @Param("allDocTypeIds") List<UUID> allDocTypeIds);
}
