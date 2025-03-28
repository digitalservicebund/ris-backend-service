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

  Optional<DocumentationUnitListItemDTO> findDocumentationUnitListItemByDocumentNumber(
      String documentNumber);

  String BASE_QUERY =
      """
  (:documentNumber IS NULL OR upper(documentationUnit.documentNumber) like concat('%', upper(cast(:documentNumber as text)), '%'))
   AND (:courtType IS NULL OR upper(court.type) like upper(cast(:courtType as text)))
   AND (:courtLocation IS NULL OR upper(court.location) like upper(cast(:courtLocation as text)))
   AND (cast(:decisionDate as date) IS NULL
       OR (cast(:decisionDateEnd as date) IS NULL AND documentationUnit.date = :decisionDate)
       OR (cast(:decisionDateEnd as date) IS NOT NULL AND documentationUnit.date BETWEEN :decisionDate AND :decisionDateEnd))
   AND (:myDocOfficeOnly = FALSE OR (:myDocOfficeOnly = TRUE AND documentationUnit.documentationOffice.id = :documentationOfficeId))
   AND (:scheduledOnly = FALSE OR cast(documentationUnit.scheduledPublicationDateTime as date) IS NOT NULL)
   AND (cast(:publicationDate as date) IS NULL
       OR (cast(documentationUnit.scheduledPublicationDateTime as date) = :publicationDate)
       OR (cast(documentationUnit.lastPublicationDateTime as date) = :publicationDate))
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
   AND (:withDuplicateWarning = FALSE
    OR (documentationUnit.documentationOffice.id = :documentationOfficeId
      AND (EXISTS (SELECT 1 FROM DuplicateRelationDTO duplicateRelation1
              WHERE documentationUnit.id = duplicateRelation1.documentationUnit1.id
              AND duplicateRelation1.relationStatus = 'PENDING')
      OR EXISTS (SELECT 1 FROM DuplicateRelationDTO duplicateRelation2
              WHERE documentationUnit.id = duplicateRelation2.documentationUnit2.id
              AND duplicateRelation2.relationStatus = 'PENDING'))))
   ORDER BY
     (CASE WHEN (:scheduledOnly = TRUE OR CAST(:publicationDate AS DATE) IS NOT NULL) THEN documentationUnit.scheduledPublicationDateTime END) DESC NULLS LAST,
     (CASE WHEN (:scheduledOnly = TRUE OR CAST(:publicationDate AS DATE) IS NOT NULL) THEN documentationUnit.lastPublicationDateTime END) DESC NULLS LAST,
     documentationUnit.date DESC NULLS LAST
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
      @Param("courtType") String courtType,
      @Param("courtLocation") String courtLocation,
      @Param("decisionDate") LocalDate decisionDate,
      @Param("decisionDateEnd") LocalDate decisionDateEnd,
      @Param("publicationDate") LocalDate publicationDate,
      @Param("scheduledOnly") Boolean scheduledOnly,
      @Param("status") PublicationStatus status,
      @Param("withErrorOnly") Boolean withErrorOnly,
      @Param("myDocOfficeOnly") Boolean myDocOfficeOnly,
      @Param("withDuplicateWarning") Boolean withDuplicateWarning,
      @Param("pageable") Pageable pageable);

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  LEFT JOIN documentationUnit.court court
  LEFT JOIN documentationUnit.fileNumbers fileNumber
  WHERE (upper(fileNumber.value) like upper(concat(:fileNumber,'%')))
  AND
  """
              + BASE_QUERY)
  @SuppressWarnings("java:S107")
  // We use JPA repository interface magic, so reducing parameter count is not possible.
  Slice<DocumentationUnitListItemDTO> searchByDocumentationUnitSearchInputFileNumber(
      @Param("documentationOfficeId") UUID documentationOfficeId,
      @Param("documentNumber") String documentNumber,
      @Param("fileNumber") String fileNumber,
      @Param("courtType") String courtType,
      @Param("courtLocation") String courtLocation,
      @Param("decisionDate") LocalDate decisionDate,
      @Param("decisionDateEnd") LocalDate decisionDateEnd,
      @Param("publicationDate") LocalDate publicationDate,
      @Param("scheduledOnly") Boolean scheduledOnly,
      @Param("status") PublicationStatus status,
      @Param("withErrorOnly") Boolean withErrorOnly,
      @Param("myDocOfficeOnly") Boolean myDocOfficeOnly,
      @Param("withDuplicateWarning") Boolean withDuplicateWarning,
      @Param("pageable") Pageable pageable);

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  LEFT JOIN documentationUnit.court court
  LEFT JOIN documentationUnit.deviatingFileNumbers deviatingFileNumber
  WHERE (upper(deviatingFileNumber.value) like upper(concat(:fileNumber,'%')))
  AND
  """
              + BASE_QUERY)
  @SuppressWarnings("java:S107")
  // We use JPA repository interface magic, so reducing parameter count is not possible.
  Slice<DocumentationUnitListItemDTO> searchByDocumentationUnitSearchInputDeviatingFileNumber(
      UUID documentationOfficeId,
      String documentNumber,
      String fileNumber,
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      LocalDate publicationDate,
      Boolean scheduledOnly,
      PublicationStatus status,
      Boolean withErrorOnly,
      Boolean myDocOfficeOnly,
      Boolean withDuplicateWarning,
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

  @Query(
      value =
          """
  SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
  WHERE documentationUnit.scheduledPublicationDateTime <= CURRENT_TIMESTAMP
""")
  List<DocumentationUnitDTO> getScheduledDocumentationUnitsDueNow();

  @Query(
      value =
          """
        SELECT d.documentNumber FROM DocumentationUnitDTO d
        JOIN d.court c
        JOIN d.status s
        JOIN d.documentType dt
        JOIN d.documentationOffice o
        WHERE cast(d.date as date) > cast('2009-12-31' as date)
        AND s.publicationStatus = 'PUBLISHED'
        AND dt.abbreviation <> 'Anh'
        AND (c.type = o.abbreviation AND c.type IN ('BSG', 'BAG', 'BGH', 'BFH', 'BVerfG', 'BVerwG', 'BPatG')
          OR (o.abbreviation = 'juris' AND c.type = 'BPatG'))
    """)
  List<String> getAllMatchingPublishCriteria();
}
