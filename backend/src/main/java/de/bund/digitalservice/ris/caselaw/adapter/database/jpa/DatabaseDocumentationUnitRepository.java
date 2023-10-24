package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseDocumentationUnitRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {
  Optional<DocumentationUnitDTO> findByDocumentNumber(String documentNumber);

  Optional<DocumentationUnitMetadataDTO> findMetadataById(UUID documentUnitUuid);
}
