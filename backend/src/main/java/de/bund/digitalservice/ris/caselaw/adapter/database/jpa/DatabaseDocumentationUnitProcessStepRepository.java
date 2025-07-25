package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseDocumentationUnitProcessStepRepository
    extends JpaRepository<DocumentationUnitProcessStepDTO, UUID> {

  Optional<DocumentationUnitProcessStepDTO> findTopByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);

  List<DocumentationUnitProcessStepDTO> findByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);
}
