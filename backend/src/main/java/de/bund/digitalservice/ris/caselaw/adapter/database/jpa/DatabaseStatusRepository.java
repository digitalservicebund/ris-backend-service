package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseStatusRepository extends JpaRepository<StatusDTO, UUID> {

  List<StatusDTO> findAllByDocumentationUnitDTO_Id(UUID documentationUnit);

  StatusDTO findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(
      DocumentationUnitDTO documentationUnitDTO);

  Optional<StatusDTO> findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(
      UUID documentationUnitId);
}
