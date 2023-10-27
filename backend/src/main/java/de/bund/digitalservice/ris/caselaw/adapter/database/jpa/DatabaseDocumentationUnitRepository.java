package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DatabaseDocumentationUnitRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {
  Optional<DocumentationUnitDTO> findByDocumentNumber(String documentNumber);

  Optional<DocumentationUnitMetadataDTO> findMetadataById(UUID documentUnitUuid);

  @Query(
      value =
          """
           SELECT du.id as id, du.document_number as documentNumber, du.decision_date as decisionDate
           FROM incremental_migration.documentation_unit AS du
           LEFT JOIN incremental_migration.court AS court ON du.court_id = court.id
           WHERE (
               (:documentNumberOrFileNumber IS NULL
                 OR ( du.document_number = :documentNumberOrFileNumber
                    OR EXISTS (
                       SELECT 1
                       FROM incremental_migration.file_number AS fn
                       WHERE fn.documentation_unit_id = du.id
                       AND fn.value = :documentNumberOrFileNumber
                    )
                    OR EXISTS (
                       SELECT 1
                       FROM incremental_migration.deviating_file_number AS dfn
                       WHERE dfn.documentation_unit_id = du.id
                       AND dfn.value = :documentNumberOrFileNumber
                    )))
               AND (:courtType IS NULL OR court.type = :courtType)
               AND (:courtLocation IS NULL OR court.location = :courtLocation)
               AND (:decisionDate IS NULL OR du.decision_date = :decisionDate)
               AND (:myDocOfficeOnly = FALSE OR (:myDocOfficeOnly = TRUE AND du.documentation_office_id = :documentationOfficeId))
           );

             """,
      //             AND (
      //                 (documentation_office_id = :documentationOfficeId) OR
      //                 (
      //                     NOT EXISTS (
      //                         SELECT 1
      //                         FROM public.status
      //                         WHERE document_unit_id = documentation_unit.id
      //                         AND status = :status
      //                     ) OR
      //                     :status IS NULL
      //                 )
      nativeQuery = true)
  Page<DocumentationUnitMetadataDTO> searchByDocumentUnitSearchInput(
      UUID documentationOfficeId,
      String documentNumberOrFileNumber,
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      //      PublicationStatus status,
      Boolean myDocOfficeOnly,
      Pageable pageable);
}
