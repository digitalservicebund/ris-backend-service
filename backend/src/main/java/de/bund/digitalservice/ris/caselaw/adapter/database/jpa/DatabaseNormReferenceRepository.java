package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseNormReferenceRepository extends JpaRepository<NormReferenceDTO, UUID> {
  List<NormReferenceDTO> findAllByDocumentUnitIdOrderById(UUID documentationUnitId);
}
