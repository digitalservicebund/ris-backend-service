package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseUliRepository extends JpaRepository<UliDTO, UUID> {
  Optional<UliDTO> findByDocumentNumber(String documentNumber);

  List<UliDTO> findAllByPublishedAtAfter(Instant lastCheck);
}
