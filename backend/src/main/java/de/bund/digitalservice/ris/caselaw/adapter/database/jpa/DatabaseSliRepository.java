package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseSliRepository extends JpaRepository<SliDTO, UUID> {
  Optional<SliDTO> findByDocumentNumber(String documentNumber);

  List<SliDTO> findAllByPublishedAtAfter(Instant lastCheck);
}
