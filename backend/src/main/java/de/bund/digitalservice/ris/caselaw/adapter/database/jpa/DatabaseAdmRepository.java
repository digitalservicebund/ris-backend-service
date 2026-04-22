package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseAdmRepository extends JpaRepository<AdmDTO, UUID> {
  Optional<AdmDTO> findByDocumentNumber(String documentNumber);

  List<AdmDTO> findAllByPublishedAtAfter(Instant lastCheck);
}
