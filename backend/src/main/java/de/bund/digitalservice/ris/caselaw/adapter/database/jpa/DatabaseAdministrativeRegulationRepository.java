package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseAdministrativeRegulationRepository
    extends JpaRepository<AdministrativeRegulationDTO, UUID> {
  Optional<AdministrativeRegulationDTO> findByDocumentNumber(String documentNumber);
}
