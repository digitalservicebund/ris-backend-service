package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseAdministrativeRegulationRepository
    extends JpaRepository<AdministrativeRegulationDTO, UUID> {
  Optional<AdministrativeRegulationDTO> findByDocumentNumber(String documentNumber);

  List<AdministrativeRegulationDTO> findAllByPublishedAtAfter(Instant lastCheck);
}
