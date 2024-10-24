package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatabaseDocumentationUnitRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {
  Optional<DocumentationUnitDTO> findByDocumentNumber(String documentNumber);

  String BASE_QUERY =
      """
  (:documentNumber IS NULL OR upper(documentationUnit.documentNumber) like concat('%', upper(cast(:documentNumber as text)), '%'))
   AND (:documentNumberToExclude IS NULL OR documentationUnit.documentNumber != :documentNumberToExclude)
   AND (:courtType IS NULL OR upper(court.type) like upper(cast(:courtType as text)))
   AND (:courtLocation IS NULL OR upper(court.location) like upper(cast(:courtLocation as text)))
   AND (cast(:decisionDate as date) IS NULL
       OR (cast(:decisionDateEnd as date) IS NULL AND documentationUnit.decisionDate = :decisionDate)
       OR (cast(:decisionDateEnd as date) IS NOT NULL AND documentationUnit.decisionDate BETWEEN :decisionDate AND :decisionDateEnd))
   AND (:myDocOfficeOnly = FALSE OR (:myDocOfficeOnly = TRUE AND documentationUnit.documentationOffice.id = :documentationOfficeId))
   AND (cast(:documentType as uuid) IS NULL OR documentationUnit.documentType = :documentType)
   AND
     (
        (:status IS NULL AND (
          documentationUnit.documentationOffice.id = :documentationOfficeId OR
          status.publicationStatus IN (de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED, de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING)
          )
        )
     OR
        (:status IS NOT NULL AND (
          status.publicationStatus = :status
          AND (:status IN ('PUBLISHED', 'PUBLISHING')
            OR documentationUnit.documentationOffice.id = :documentationOfficeId
          )
        )
      )
    )
   AND (:withErrorOnly = FALSE OR documentationUnit.documentationOffice.id = :documentationOfficeId AND documentationUnit.status.withError = TRUE)
ORDER BY documentationUnit.decisionDate DESC NULLS LAST
""";

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  LEFT JOIN documentationUnit.court court
  LEFT JOIN documentationUnit.status status
  WHERE
  """
              + BASE_QUERY)
  @SuppressWarnings("java:S107")
  // We use JPA repository interface magic, so reducing parameter count is not possible.
  Slice<DocumentationUnitListItemDTO> searchByDocumentationUnitSearchInput(
      @Param("documentationOfficeId") UUID documentationOfficeId,
      @Param("documentNumber") String documentNumber,
      @Param("documentNumberToExclude") String documentNumberToExclude,
      @Param("courtType") String courtType,
      @Param("courtLocation") String courtLocation,
      @Param("decisionDate") LocalDate decisionDate,
      @Param("decisionDateEnd") LocalDate decisionDateEnd,
      @Param("status") PublicationStatus status,
      @Param("withErrorOnly") Boolean withErrorOnly,
      @Param("myDocOfficeOnly") Boolean myDocOfficeOnly,
      @Param("documentType") DocumentTypeDTO documentType,
      @Param("pageable") Pageable pageable);

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  LEFT JOIN documentationUnit.court court
  LEFT JOIN documentationUnit.fileNumbers fileNumber
  WHERE (upper(fileNumber.value) like upper(concat('%', :fileNumber,'%')))
  AND
  """
              + BASE_QUERY)
  @SuppressWarnings("java:S107")
  // We use JPA repository interface magic, so reducing parameter count is not possible.
  Slice<DocumentationUnitListItemDTO> searchByDocumentationUnitSearchInputFileNumber(
      @Param("documentationOfficeId") UUID documentationOfficeId,
      @Param("documentNumber") String documentNumber,
      @Param("documentNumberToExclude") String documentNumberToExclude,
      @Param("fileNumber") String fileNumber,
      @Param("courtType") String courtType,
      @Param("courtLocation") String courtLocation,
      @Param("decisionDate") LocalDate decisionDate,
      @Param("decisionDateEnd") LocalDate decisionDateEnd,
      @Param("status") PublicationStatus status,
      @Param("withErrorOnly") Boolean withErrorOnly,
      @Param("myDocOfficeOnly") Boolean myDocOfficeOnly,
      @Param("documentType") DocumentTypeDTO documentType,
      @Param("pageable") Pageable pageable);

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  LEFT JOIN documentationUnit.court court
  LEFT JOIN documentationUnit.deviatingFileNumbers deviatingFileNumber
  WHERE (upper(deviatingFileNumber.value) like upper(concat('%', :fileNumber,'%')))
  AND
  """
              + BASE_QUERY)
  @SuppressWarnings("java:S107")
  // We use JPA repository interface magic, so reducing parameter count is not possible.
  Slice<DocumentationUnitListItemDTO> searchByDocumentationUnitSearchInputDeviatingFileNumber(
      UUID documentationOfficeId,
      String documentNumber,
      String documentNumberToExclude,
      String fileNumber,
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      PublicationStatus status,
      Boolean withErrorOnly,
      Boolean myDocOfficeOnly,
      DocumentTypeDTO documentType,
      Pageable pageable);

  // temporarily needed for the ldml handover phase, can be removed once we integrate ldml
  // generation into the doc-unit lifecycle
  @Query(
      value =
          """
          SELECT DISTINCT d.id FROM incremental_migration.documentation_unit d
          JOIN incremental_migration.status s ON d.id = s.documentation_unit_id
          where s.publication_status = 'PUBLISHED'
          LIMIT 100
          """,
      nativeQuery = true)
  List<UUID> getRandomDocumentationUnitIds();
}
