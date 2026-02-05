package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DatabaseDocumentationUnitRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {
  Optional<DocumentationUnitDTO> findByDocumentNumber(String documentNumber);

  @Query("SELECT d.id FROM DocumentationUnitDTO d where d.documentNumber = :documentNumber")
  Optional<UUID> findIdByDocumentNumber(@Param("documentNumber") String documentNumber);

  Optional<DocumentationUnitListItemDTO> findDocumentationUnitListItemByDocumentNumber(
      String documentNumber);

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

  @Query(
      value =
"""
SELECT d.documentNumber FROM DocumentationUnitDTO d
  WHERE d.portalPublicationStatus = 'PUBLISHED'
            """)
  Set<String> findAllPublishedDocumentNumbers();

  @Query(
      value =
          "SELECT d FROM DocumentationUnitDTO d WHERE d.status.publicationStatus = :publicationStatus ORDER BY d.status.createdAt DESC")
  List<DocumentationUnitDTO> findAllByStatus(PublicationStatus publicationStatus, Pageable page);
}
