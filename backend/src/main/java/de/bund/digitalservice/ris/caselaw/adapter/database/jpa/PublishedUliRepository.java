package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishedUliRepository extends JpaRepository<PublishedUli, UUID> {
  Optional<PublishedUli> findByDocumentNumber(String documentNumber);

  List<PublishedUli> findAllByPublishedAtAfter(Instant lastCheck);
}
