package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalPublicationJobRepository
    extends JpaRepository<PortalPublicationJobDTO, UUID> {

  @Query(
      """
    SELECT p.documentNumber
    FROM PortalPublicationJobDTO p
    WHERE p.createdAt = (
        SELECT MAX(p2.createdAt)
        FROM PortalPublicationJobDTO p2
        WHERE p2.documentNumber = p.documentNumber
    )
    AND p.publicationType = 'PUBLISH'
      """)
  List<String> findAllDocumentNumbersPublishJobs();

  @Query(
      value =
          """
     SELECT DISTINCT ON (document_number) * FROM incremental_migration.portal_publication_job
        WHERE portal_publication_status = 'PENDING'
        ORDER BY document_number, created_at DESC
        LIMIT 500
    """,
      nativeQuery = true)
  List<PortalPublicationJobDTO> findLatestPendingJobs();

  @Modifying
  @Query(
      """
    UPDATE PortalPublicationJobDTO portalPublicationJob
    SET portalPublicationJob.publicationStatus = 'SKIPPED'
    WHERE portalPublicationJob.documentNumber = :documentNumber
    AND portalPublicationJob.publicationStatus = 'PENDING'
""")
  void ignoreOlderJobsByDocumentNumber(String documentNumber);
}
