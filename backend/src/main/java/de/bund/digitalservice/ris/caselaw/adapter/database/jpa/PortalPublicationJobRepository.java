package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalPublicationJobRepository
    extends JpaRepository<PortalPublicationJobDTO, PortalPublicationJobDTO> {

  @Query(
      value =
          """
SELECT portalPublicationJob
FROM PortalPublicationJobDTO portalPublicationJob
WHERE portalPublicationJob.publicationStatus = 'PENDING'
""")
  List<PortalPublicationJobDTO> findPendingJobs();

  // TODO: set status bulk vs individual?
}
