package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseStatusRepository extends JpaRepository<StatusDTO, UUID> {
  List<StatusDTO> findAllByPublicationStatus(PublicationStatus publicationStatus, Pageable page);
}
