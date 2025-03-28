package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
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
SELECT portalPublicationJob
FROM PortalPublicationJobDTO portalPublicationJob
WHERE portalPublicationJob.publicationStatus = 'PENDING'
ORDER BY portalPublicationJob.createdAt ASC LIMIT 500
""")
  List<PortalPublicationJobDTO> findPendingJobsOrderedByCreationDate();
}
