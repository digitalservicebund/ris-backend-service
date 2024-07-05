package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentationUnitPatchRepository
    extends JpaRepository<DocumentationUnitPatchDTO, UUID> {

  List<DocumentationUnitPatchDTO>
      findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
          UUID uuid, Long documentationUnitVersion);
}
